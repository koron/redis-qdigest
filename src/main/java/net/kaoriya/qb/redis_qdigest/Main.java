package net.kaoriya.qb.redis_qdigest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import net.kaoriya.qb.redis_lua.LuaScript;

public final class Main
{
    public static void main(String[] args)
    {
        runOnHost("127.0.0.1");
    }

    public static void runOnHost(String host)
    {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), host);
        Jedis jedis = pool.getResource();
        try {
            run(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.returnResource(jedis);
        }
        pool.destroy();
    }

    public static void run(Jedis jedis)
        throws Exception
    {
        // Drop QDigest instance from redis server.
        QDigest.dropInstance(jedis, "foo");

        // Get a new created instance of QDigest on redis server.
        QDigest qd = QDigest.getInstance(jedis, "foo", 20);
        // Offer values to QDigest.
        qd.offer(10);
        qd.offer(20);
        qd.offer(30);
        qd.offer(40);
        qd.offer(50);
        // Get quantiles: 0%, 50%, 100%
        System.out.println("qd(0.0f)=" + qd.quantile(0.0f));
        System.out.println("qd(0.5f)=" + qd.quantile(0.5f));
        System.out.println("qd(1.0f)=" + qd.quantile(1.0f));

        // Get 2nd instance, it has same data as 1st one.
        QDigest qd2 = QDigest.getInstance(jedis, "foo", 20);
        System.out.println("qd2(0.0f)=" + qd2.quantile(0.0f));
        System.out.println("qd2(0.5f)=" + qd2.quantile(0.5f));
        System.out.println("qd2(1.0f)=" + qd2.quantile(1.0f));

        // When factor is wrong, getInstance() will return null.
        QDigest qd3 = QDigest.getInstance(jedis, "foo", 30);
        System.out.println("qd3=" + qd3);

        qd.drop();
    }
}
