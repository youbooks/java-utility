package com.lingdonge.redis.ratelimit;

import com.lingdonge.core.dates.SystemClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 收集的一些限流用法
 */
@Slf4j
public class RedisRateLimitUtil {

    private RedisTemplate redisTemplate;

    /**
     * 构造函数
     *
     * @param redisTemplate
     */
    public RedisRateLimitUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 判断时间范围内是否可以继续请求
     * 逻辑：利用自增，判断是否存在。
     * 用途：一般用作频率限制，比如短信：60秒只能发1次
     *
     * @param uuid          要限制的ID
     * @param expireSeconds 过期时间，过期之后将不再限制
     * @return
     */
    public boolean acquireByRate(String uuid, Long expireSeconds) {
        String key = "REDIS_LIMITER_" + uuid.trim();
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS); // 设置过期时间
            return true;
        }
        if (count > 1) {
            return false;
        }
        return true;
    }

    /**
     * 单位时间范围内限制。比如：1天最多5次这种限制
     * 核心逻辑：使用redis的list做为验证记录，永久只保存2个记录，根据上下次的时间来计算请求的时间频率
     * 返回true代表可以使用，返回false代表不能使用
     *
     * @param uuid           ID标记
     * @param limitSeconds   时间限制，单位秒
     * @param limitFrequency 频率限制，单位次数
     * @return
     */
    public boolean acquireByDuration(String uuid, Long limitSeconds, Long limitFrequency) {
        String redisKey = "REDIS_LIMITER_RATEBY_" + uuid.trim() + "_" + limitSeconds;
        Long listLength = redisTemplate.opsForList().size(redisKey);
        if (listLength < limitFrequency) { // 在次数范围内，直接插入列表左边
//            System.out.println("当前已经请求次数为：" + listLength);
            redisTemplate.opsForList().leftPush(redisKey, SystemClock.now()); // 当前时间Push到左边
        } else {
            Long lastTime = (Long) redisTemplate.opsForList().index(redisKey, 0);// 取出左边上一次时间
//            System.out.println("上次请求时间：" + lastTime);
            if ((SystemClock.now() - lastTime) < (limitSeconds * 1000)) { // 当前时间-上次时间，小于时间限制，代表 请求过多，需要休息一段时间
//                System.out.println("limitSeconds范围内，请求过多。");
                return false;
            } else {
                redisTemplate.opsForList().leftPush(redisKey, SystemClock.now());// 没到限制，继续在左侧压入当前时间
                redisTemplate.opsForList().trim(redisKey, 0, 2);//只保留2条记录，减少资源消耗
            }
        }
        return true;
    }


    /**
     * @param redisKey
     * @param seconds
     * @param times
     * @return
     */
    public boolean freLimit(String redisKey, Integer seconds, Integer times) {
        boolean result = false;
//定义redis操作
        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();

        Long endTime = System.currentTimeMillis() / 1000;
//设置最后一次发送
        Long startTime = endTime - seconds;
//移出之前已无效的记录
//当前redis中有效总条数
        Long count = opsForZSet.count(redisKey, startTime, endTime);
//如果条数大于或等于条数限制，则抛出异常，发送太多次
        if (count >= times) {
            log.info("规定时间：{}秒内，请求次数：{}过多", seconds, count);
            return result;
        }
        String value = UUID.randomUUID().toString().replaceAll("-", "");
//向set中添加新纪录
        result = opsForZSet.add(redisKey, value, endTime);
        if (!result) {
            log.info("add redis set error, key:{}, score:{}", opsForZSet, endTime);
            return result;
        }
//当前有效的总条数
        count = opsForZSet.count(redisKey, startTime, endTime);
        if (count >= times) {
            opsForZSet.removeRangeByScore(redisKey, 0, startTime);
            Long rank = opsForZSet.rank(redisKey, value);
            if (rank < times) {
                return true;
            } else {
                opsForZSet.remove(redisKey, value);
                return false;
            }
        }
        return result;
    }

}
