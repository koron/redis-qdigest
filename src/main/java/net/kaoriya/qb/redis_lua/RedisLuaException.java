package net.kaoriya.qb.redis_lua;

public final class RedisLuaException extends Exception
{
    public RedisLuaException(String message) {
        super(message);
    }

    public RedisLuaException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisLuaException(Throwable cause) {
        super(cause);
    }
}
