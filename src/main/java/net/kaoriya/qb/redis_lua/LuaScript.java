package net.kaoriya.qb.redis_lua;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

public final class LuaScript
{
    static char[] HEXCHARS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
    };

    private final String sha1Hex;
    private final String script;

    private LuaScript(byte[] bytes)
        throws RedisLuaException
    {
        try {
            this.sha1Hex = getSHA1Hex(bytes);
            this.script = new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new RedisLuaException(e);
        }
    }

    public LuaScript(String script)
        throws RedisLuaException
    {
        this(toUTF8(script));
    }

    public String getSHA1Hex() {
        return this.sha1Hex;
    }

    public String getScript() {
        return this.script;
    }

    public static LuaScript newFromResource(Class c, String name)
        throws RedisLuaException
    {
        InputStream istream = c.getResourceAsStream(name);
        if (istream == null) {
            return null;
        }
        byte[] bytes = null;
        try {
            bytes = loadAsBytes(istream);
        } catch (IOException e) {
            throw new RedisLuaException(
                    "Failed to load from resource: " + name, e);
        } finally {
            try { istream.close(); } catch (IOException e) {}
        }
        if (bytes == null) {
            return null;
        }
        return new LuaScript(bytes);
    }

    static byte[] loadAsBytes(InputStream istream)
        throws IOException
    {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        while (true) {
            int len = istream.read(buf);
            if (len <= 0) {
                break;
            }
            ostream.write(buf, 0, len);
        }
        return ostream.toByteArray();
    }

    static String getSHA1Hex(byte[] text)
        throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text);
        byte[] digest = md.digest();
        return toHex(digest);
    }

    static String toHex(byte[] bytes)
    {
        StringBuffer s = new StringBuffer();
        for (byte b : bytes) {
            s.append(HEXCHARS[(b >> 4) & 0xf]);
            s.append(HEXCHARS[(b >> 0) & 0xf]);
        }
        return s.toString();
    }

    static byte[] toUTF8(String s)
        throws RedisLuaException
    {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RedisLuaException(e);
        }
    }

    public Object eval(Jedis jedis, int keyCount, String... params)
        throws RedisLuaException
    {
        try {
            return jedis.evalsha(this.sha1Hex, keyCount, params);
        } catch (JedisDataException e) {
            if (!e.getMessage().startsWith("NOSCRIPT")) {
                throw e;
            }
        }
        // script is not loaded yet.
        Object key = jedis.scriptLoad(this.script);
        if (!this.sha1Hex.equals(key.toString())) {
            return new RedisLuaException(
                    "SCRIPT LOAD result is not match:"
                    + " actually=" + key
                    + " expected=" + this.sha1Hex);
        }
        return jedis.evalsha(this.sha1Hex, keyCount, params);
    }
}
