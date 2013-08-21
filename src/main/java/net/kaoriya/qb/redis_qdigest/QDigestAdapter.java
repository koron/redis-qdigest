package net.kaoriya.qb.redis_qdigest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import net.kaoriya.qb.redis_lua.LuaScript;
import net.kaoriya.qb.redis_lua.RedisLuaException;

public final class QDigestAdapter
{
    static final String ERR_WRONG_KIND_OF_VALUE =
        "ERR Operation against a key holding the wrong kind of value";

    static final LuaScript createScript;
    static final LuaScript checkScript;
    static final LuaScript offerScript;
    static final LuaScript quantileScript;
    static final LuaScript dropScript;

    static {
        try {
            createScript = LuaScript.newFromResource(QDigestAdapter.class,
                    "qd_create.lua");
            checkScript = LuaScript.newFromResource(QDigestAdapter.class,
                    "qd_check.lua");
            offerScript = LuaScript.newFromResource(QDigestAdapter.class,
                    "qd_offer.lua");
            quantileScript = LuaScript.newFromResource(QDigestAdapter.class,
                    "qd_quantile.lua");
            dropScript = LuaScript.newFromResource(QDigestAdapter.class,
                    "qd_drop.lua");
        } catch (RedisLuaException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean create(Jedis jedis, String key, long factor)
        throws QDigestException
    {
        try {
            Long r = (Long)createScript.eval(jedis, 1, key,
                    Long.toString(factor));
            return r != 0L ? true : false;
        } catch (RedisLuaException e) {
            throw new QDigestException(e);
        }
    }

    public static long check(Jedis jedis, String key)
        throws QDigestException
    {
        try {
            Long r = (Long)checkScript.eval(jedis, 1, key);
            return r != null ? r.longValue() : 0L;
        } catch (JedisDataException e) {
            if (e.getMessage().indexOf(ERR_WRONG_KIND_OF_VALUE) >= 0) {
                return 0L;
            }
            throw e;
        } catch (RedisLuaException e) {
            throw new QDigestException(e);
        }

    }

    public static boolean offer(Jedis jedis, String key, long... values)
        throws QDigestException
    {
        // build arguments.
        String[] args = new String[values.length + 1];
        args[0] = key;
        for (int i = 0; i < values.length; ++i) {
            args[i + 1] = Long.toString(values[i]);
        }
        // invoke lua script on Redis.
        try {
            Long r = (Long)offerScript.eval(jedis, 1, args);
            return r != 0L ? true : false;
        } catch (RedisLuaException e) {
            throw new QDigestException(e);
        }
    }

    public static long quantile(Jedis jedis, String key, float q)
        throws QDigestException
    {
        try {
            Long r = (Long)quantileScript.eval(jedis, 1, key,
                    Float.toString(q));
            return r.longValue();
        } catch (RedisLuaException e) {
            throw new QDigestException(e);
        }
    }

    public static void drop(Jedis jedis, String key)
        throws QDigestException
    {
        try {
            dropScript.eval(jedis, 1, key);
        } catch (RedisLuaException e) {
            throw new QDigestException(e);
        }
    }
}
