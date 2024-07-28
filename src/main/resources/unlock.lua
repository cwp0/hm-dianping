-- 比较线程标识与锁中的标识是否一致
-- KEYS[1]：锁的key
-- ARGV[1]：线程标识
if (redis.call('get', KEYS[1]) == ARGV[1])
then
    -- 一致则删除锁
    return redis.call('del', KEYS[1])
end

return 0
