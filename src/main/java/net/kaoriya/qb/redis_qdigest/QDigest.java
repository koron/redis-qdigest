package net.kaoriya.qb.redis_qdigest;

import java.lang.ref.WeakReference;

import redis.clients.jedis.Jedis;

public final class QDigest
{
    public static QDigest getInstance(Jedis jedis, String key, long factor)
        throws QDigestException
    {
        if (factor < 0L) {
            throw new QDigestException("factor must be greater than zero");
        } else if (QDigestAdapter.create(jedis, key, factor)) {
            return new QDigest(jedis, key);
        } else if (factor == QDigestAdapter.check(jedis, key)) {
            return new QDigest(jedis, key);
        } else {
            return null;
        }
    }

    public static void dropInstance(Jedis jedis, String key)
        throws QDigestException
    {
        QDigestAdapter.drop(jedis, key);
    }

    private final WeakReference<Jedis> jedis;
    private final String key;

    private QDigest(Jedis jedis, String key) {
        this.jedis = new WeakReference<Jedis>(jedis);
        this.key = key;
    }

    public boolean offer(long value)
        throws QDigestException
    {
        return QDigestAdapter.offer(this.jedis.get(), this.key, value);
    }

    public long quantile(float q)
        throws QDigestException
    {
        return QDigestAdapter.quantile(this.jedis.get(), this.key, q);
    }

    public void drop()
        throws QDigestException
    {
        QDigestAdapter.drop(this.jedis.get(), this.key);
        this.jedis.clear();
    }
}
