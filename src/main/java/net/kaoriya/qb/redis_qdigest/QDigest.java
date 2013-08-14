package net.kaoriya.qb.redis_qdigest;

import java.lang.ref.WeakReference;

import redis.clients.jedis.Jedis;

/**
 * QDigest implementation which using Redis as backend.
 */
public final class QDigest
{
    /**
     * Get QDigest instance by key and factor.
     *
     * When there are no exsiting QDigest by key, this create new one and
     * return it.  When there is an existing QDigest by key and it have same
     * factor, this return it.
     *
     * Otherwise return null.
     *
     * @param jedis Connection to Redis.
     * @param key Name of QDigest.  Which is used as a key in Redis.
     * @param factor Memory usage factor.
     */
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

    /**
     * Drop QDigest instance from Redis.
     *
     * @param jedis Connection to Redis.
     * @param key Name of QDigest.
     */
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

    /**
     * Offer a value.
     *
     * @param value Value to offer.
     */
    public boolean offer(long value)
        throws QDigestException
    {
        return QDigestAdapter.offer(this.jedis.get(), this.key, value);
    }

    /**
     * Get quantile of values.
     *
     * @param q Fraction of quantile.
     */
    public long quantile(float q)
        throws QDigestException
    {
        return QDigestAdapter.quantile(this.jedis.get(), this.key, q);
    }

    /**
     * Drop data related to QDigest from Redis.
     */
    public void drop()
        throws QDigestException
    {
        QDigestAdapter.drop(this.jedis.get(), this.key);
        this.jedis.clear();
    }
}
