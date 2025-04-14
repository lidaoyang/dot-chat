package com.dot.comm.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类（String类型）
 *
 * @author: daoyang
 * @create: 2022-06-23 16:02
 */
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    private final RedisScript<Long> rateLimitScript;

    /**
     * 获取分布式锁脚本
     */
    private static final String RATE_LIMIT_SCRIPT =
            """
                          local key = KEYS[1]
                          local limit = tonumber(ARGV[1])
                          local expireTime = tonumber(ARGV[2])
                          local current = tonumber(redis.call('GET', key) or "0")
                    
                          if current + 1 > limit then
                              return 0
                          else
                              redis.call('INCR', key)
                              if current == 0 then
                                  redis.call('EXPIRE', key, expireTime)
                              end
                              return 1
                          end
                    """;


    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key        锁的key
     * @param limit      获取锁的次数
     * @param expireTime 过期时间(单位:秒)
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, int limit, long expireTime) {
        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(expireTime)
        );
        return result != null && result == 1;
    }

    /**
     * 写入缓存
     *
     * @param key   redis键
     * @param value redis值
     * @return 是否成功
     */
    public boolean set(final String key, String value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key        redis键
     * @param value      redis值
     * @param expireTime 有效期(秒)
     * @return 是否成功
     */
    public boolean set(final String key, String value, Long expireTime) {
        return set(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key   redis键
     * @param value redis值
     * @return 是否成功
     */
    public boolean set(final String key, String value, Long expireTime, TimeUnit timeUnit) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, timeUnit);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 集合设置过期时间
     */
    public void expire(String key, Integer time, TimeUnit unit) {
        redisTemplate.expire(key, time, unit);
    }

    /**
     * 批量删除对应的键值对
     *
     * @param keys Redis键名数组
     */
    public void removeByKeys(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除Redis key
     *
     * @param pattern 键名包含字符串（如：myKey*）
     */
    public void removePattern(final String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除key,也删除对应的value
     *
     * @param key Redis键名
     * @return 是否成功, true-成功, false-失败/不存在
     */
    public boolean remove(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key Redis键名
     * @return 是否存在
     */
    public Boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     *
     * @param key Redis键名
     * @return 是否存在
     */
    public String get(final String key) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        return operations.get(key);
    }

    /**
     * multiSet 批量添加(少量数据批量添加时,建议使用)
     *
     * @param kvMap key-value kvMap
     */
    public void batchSet(Map<String, String> kvMap) {
        redisTemplate.opsForValue().multiSet(kvMap);
    }

    /**
     * 利用pipeline批量添加,支持设置失效时间
     *
     * @param map     key-value map
     * @param seconds 过期时间(秒)
     */
    public void batchSetOrExpire(Map<String, String> map, Long seconds) {
        RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
        redisTemplate.executePipelined((RedisCallback<String>) connection -> {
            map.forEach((key, value) -> {
                connection.set(Objects.requireNonNull(serializer.serialize(key)), Objects.requireNonNull(serializer.serialize(value)),
                        Expiration.seconds(seconds), RedisStringCommands.SetOption.UPSERT);
            });
            return null;
        }, serializer);
    }

    /**
     * 批量获取
     *
     * @param keys key列表
     * @return 值列表
     */
    public List<String> batchGet(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * Redis批量Delete
     *
     * @param keys key列表
     */
    public void batchDelete(List<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 哈希 添加
     *
     * @param key     Redis键
     * @param hashKey 哈希键
     * @param value   哈希值
     */
    public void hmSet(String key, String hashKey, String value) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        hash.put(key, hashKey, value);
    }

    /**
     * 哈希获取数据
     *
     * @param key     Redis键
     * @param hashKey 哈希键
     * @return 哈希值
     */
    public String hmGet(String key, String hashKey) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        return hash.get(key, hashKey);
    }

    /**
     * 判断hash是否存在键
     *
     * @param key     Redis键
     * @param hashKey 哈希键
     * @return 是否存在
     */
    public boolean hmHasKey(String key, String hashKey) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        return hash.hasKey(key, hashKey);
    }

    /**
     * 删除hash中一条或多条数据
     *
     * @param key      Redis键
     * @param hashKeys 哈希键名数组
     * @return 删除数量
     */
    public long hmRemove(String key, Object... hashKeys) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        return hash.delete(key, hashKeys);
    }

    /**
     * 获取所有哈希键值对
     *
     * @param key Redis键名
     * @return 哈希Map
     */
    public Map<String, String> hashMapGet(String key) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        return hash.entries(key);
    }

    /**
     * 保存Map到哈希
     *
     * @param key Redis键名
     * @param map 哈希Map
     */
    public void hashMapSet(String key, Map<String, String> map) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        hash.putAll(key, map);
    }

    /**
     * 列表-追加值
     *
     * @param key   Redis键名
     * @param value 列表值
     */
    public void lPush(String key, String value) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(key, value);
    }

    /**
     * 列表-删除值
     *
     * @param key   Redis键名
     * @param value 列表值
     */
    public void lRemove(String key, String value) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.remove(key, 0, value);
    }

    /**
     * 列表-获取指定范围数据
     *
     * @param key   Redis键名
     * @param start 开始行号（start:0，end:-1查询所有值）
     * @param end   结束行号
     * @return 列表
     */
    public List<String> lRange(String key, long start, long end) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        return list.range(key, start, end);
    }

    /**
     * 列表-获取所有数据
     *
     * @param key Redis键名
     * @return 列表
     */
    public List<String> lRangeAll(String key) {
        return lRange(key, 0, -1);
    }

    /**
     * 集合添加
     *
     * @param key   Redis键名
     * @param value 值
     */
    public void add(String key, String value) {
        SetOperations<String, String> set = redisTemplate.opsForSet();
        set.add(key, value);
    }

    /**
     * 集合获取
     *
     * @param key Redis键名
     * @return 集合
     */
    public Set<String> setMembers(String key) {
        SetOperations<String, String> set = redisTemplate.opsForSet();
        return set.members(key);
    }

    /**
     * 判断集合中是否存在某个值
     *
     * @param key   Redis键名
     * @param value 值
     * @return 存在与否
     */
    public Boolean isMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 有序集合添加
     *
     * @param key   Redis键名
     * @param value 值
     * @param score 排序号
     */
    public void zAdd(String key, String value, double score) {
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
        zSet.add(key, value, score);
    }

    /**
     * 有序集合-获取指定范围
     *
     * @param key        Redis键
     * @param startScore 开始序号
     * @param endScore   结束序号
     * @return 集合
     */
    public Set<String> rangeByScore(String key, double startScore, double endScore) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        return zset.rangeByScore(key, startScore, endScore);
    }

    /**
     * 模糊查询Redis键名
     *
     * @param pattern 键名包含字符串（如：myKey*）
     * @return 集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 获取多个hashMap
     *
     * @param keySet
     * @return List<Map < String, String>> hashMap列表
     */
    public List hashMapList(Collection<String> keySet) {
        return redisTemplate.executePipelined(new SessionCallback<String>() {
            @Override
            public <K, V> String execute(RedisOperations<K, V> operations) throws DataAccessException {
                HashOperations hashOperations = operations.opsForHash();
                for (String key : keySet) {
                    hashOperations.entries(key);
                }
                return null;
            }
        });
    }

    /**
     * 保存多个哈希表（HashMap）(Redis键名不可以重复)
     *
     * @param dataMap Map<Redis键名,Map<哈希键,哈希值>>
     */
    public void batchHashMapSet(Map<String, Map<String, String>> dataMap) {
        // 设置5秒超时时间
        redisTemplate.expire("max", 25, TimeUnit.SECONDS);
        redisTemplate.executePipelined(new RedisCallback<List<Map<String, String>>>() {

            @Override
            public List<Map<String, String>> doInRedis(RedisConnection connection) throws DataAccessException {
                for (Map.Entry<String, Map<String, String>> hash : dataMap.entrySet()) {
                    // 哈希名,即表名
                    byte[] hashName = redisTemplate.getStringSerializer().serialize(hash.getKey());
                    Map<String, String> hashValues = hash.getValue();
                    Iterator<Map.Entry<String, String>> it = hashValues.entrySet().iterator();
                    // 将元素序列化后缓存，即表的多条哈希记录
                    Map<byte[], byte[]> hashes = new HashMap<>();
                    while (it.hasNext()) {
                        // hash中一条key-value记录
                        Map.Entry<String, String> entry = it.next();
                        byte[] key = redisTemplate.getStringSerializer().serialize(entry.getKey());
                        byte[] value = redisTemplate.getStringSerializer().serialize(entry.getValue());
                        hashes.put(key, value);
                    }
                    // 批量保存
                    connection.hMSet(hashName, hashes);
                }
                return null;
            }
        });
    }

    /**
     * 保存多个哈希表（HashMap）列表（哈希map的Redis键名不能重复）
     *
     * @param list Map<Redis键名,Map<哈希键,哈希值>>
     */
    public void batchHashMapListSet(List<Map<String, Map<String, String>>> list) {
        // 设置5秒超时时间
        redisTemplate.expire("max", 25, TimeUnit.SECONDS);
        redisTemplate.executePipelined((RedisCallback<List<Map<String, String>>>) connection -> {
            for (Map<String, Map<String, String>> dataMap : list) {
                Iterator<Map.Entry<String, Map<String, String>>> iterator = dataMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Map<String, String>> hash = iterator.next();
                    // 哈希名,即表名
                    byte[] hashName = redisTemplate.getStringSerializer().serialize(hash.getKey());
                    Map<String, String> hashValues = hash.getValue();
                    Iterator<Map.Entry<String, String>> it = hashValues.entrySet().iterator();
                    // 将元素序列化后缓存，即表的多条哈希记录
                    Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
                    while (it.hasNext()) {
                        // hash中一条key-value记录
                        Map.Entry<String, String> entry = it.next();
                        byte[] key = redisTemplate.getStringSerializer().serialize(entry.getKey());
                        byte[] value = redisTemplate.getStringSerializer().serialize(entry.getValue());
                        hashes.put(key, value);
                    }
                    // 批量保存
                    connection.hMSet(hashName, hashes);
                }
            }
            return null;
        });
    }

    /**
     * 只有不存在的时候才设置键值对。
     *
     * @param key        string key
     * @param value      string value
     * @param expireTime Long 过期时间
     * @param timeUnit   TimeUnit 时间格式
     * @return true:设置成功;false:设置失败
     */
    public boolean setnx(String key, String value, Long expireTime, TimeUnit timeUnit) {
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            return Boolean.TRUE.equals(operations.setIfAbsent(key, value, expireTime, timeUnit));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 尝试上锁,获取锁
     *
     * @param key 上锁key
     * @return true:获锁成功;false:获锁失败
     */
    public boolean tryLock(String key, Long expireTime, TimeUnit timeUnit) {
        return this.setnx(key, "1", expireTime, timeUnit);
    }

    /**
     * 上锁
     *
     * @param key 上锁key
     * @return true/false
     */
    public boolean tryLock(String key) {
        return this.tryLock(key, 2L, TimeUnit.SECONDS);
    }

    /**
     * 解锁
     *
     * @param key 解锁key
     * @return true/false
     */
    public void unlock(String key) {
        remove(key);
    }

    /**
     * 值递增
     *
     * @param key string key
     */
    public void increment(String key) {
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.increment(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置过期时间
     */
    public void setExpire(String key, Integer time, TimeUnit unit) {
        redisTemplate.expire(key, time, unit);
    }
}
