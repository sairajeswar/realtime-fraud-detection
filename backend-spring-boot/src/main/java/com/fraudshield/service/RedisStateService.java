package com.fraudshield.service;

import com.fraudshield.model.entity.UserProfileEntity;
import com.fraudshield.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStateService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserProfileRepository userProfileRepository;

    // Resilient local fallback cache in case Redis is not reachable in local dev
    private final ConcurrentHashMap<String, UserProfileEntity> localProfileCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> localVelocityCounter = new ConcurrentHashMap<>();

    private static final double EARTH_RADIUS_KM = 6371.0;

    public UserProfileEntity getUserProfile(String userId) {
        try {
            String cacheKey = "user:profile:" + userId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                // If present in Redis
                return localProfileCache.computeIfAbsent(userId, k -> fetchAndStoreFallback(userId));
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for profile lookup ({}), using local repository fallback", e.getMessage());
        }

        return localProfileCache.computeIfAbsent(userId, k -> fetchAndStoreFallback(userId));
    }

    private UserProfileEntity fetchAndStoreFallback(String userId) {
        return userProfileRepository.findByUserId(userId).orElseGet(() -> {
            UserProfileEntity defaultProfile = UserProfileEntity.builder()
                    .userId(userId)
                    .avgAmount(BigDecimal.valueOf(2500.00))
                    .stdDevAmount(BigDecimal.valueOf(1200.00))
                    .primaryDeviceId("DEV_DEFAULT_" + userId)
                    .primaryLocation("Mumbai")
                    .primaryLatitude(19.0760)
                    .primaryLongitude(72.8777)
                    .accountAgeDays(120)
                    .totalTransactions(10)
                    .lastTxnTimestamp(LocalDateTime.now().minusHours(4))
                    .lastTxnLatitude(19.0760)
                    .lastTxnLongitude(72.8777)
                    .build();
            return userProfileRepository.save(defaultProfile);
        });
    }

    public int incrementAndGet5MinVelocity(String userId) {
        try {
            String key = "user:velocity:5m:" + userId;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(5));
            }
            return count != null ? count.intValue() : 1;
        } catch (Exception e) {
            return localVelocityCounter.compute(userId, (k, v) -> (v == null) ? 1 : v + 1);
        }
    }

    public double calculateGeoVelocity(UserProfileEntity profile, Double currentLat, Double currentLon, LocalDateTime currentTimestamp) {
        if (currentLat == null || currentLon == null || profile.getLastTxnLatitude() == null || profile.getLastTxnLongitude() == null) {
            return 0.0;
        }

        LocalDateTime lastTime = profile.getLastTxnTimestamp() != null ? profile.getLastTxnTimestamp() : LocalDateTime.now().minusHours(2);
        long secondsElapsed = Math.max(1, Duration.between(lastTime, currentTimestamp).getSeconds());
        double hoursElapsed = secondsElapsed / 3600.0;

        double distanceKm = calculateHaversineDistance(
                profile.getLastTxnLatitude(), profile.getLastTxnLongitude(),
                currentLat, currentLon
        );

        if (hoursElapsed <= 0.001) {
            return distanceKm > 5.0 ? 1200.0 : 0.0; // Instantaneous jump anomaly
        }

        return Math.min(2500.0, distanceKm / hoursElapsed);
    }

    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public void updateLastKnownState(String userId, Double lat, Double lon, String deviceId, String location, LocalDateTime timestamp) {
        UserProfileEntity profile = getUserProfile(userId);
        if (lat != null && lon != null) {
            profile.setLastTxnLatitude(lat);
            profile.setLastTxnLongitude(lon);
        }
        profile.setLastTxnTimestamp(timestamp);
        profile.setTotalTransactions(profile.getTotalTransactions() + 1);
        localProfileCache.put(userId, profile);
        userProfileRepository.save(profile);
    }
}
