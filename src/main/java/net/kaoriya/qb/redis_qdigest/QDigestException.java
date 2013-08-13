package net.kaoriya.qb.redis_qdigest;

public class QDigestException extends Exception
{
    public QDigestException(String message) {
        super(message);
    }

    public QDigestException(String message, Throwable cause) {
        super(message, cause);
    }

    public QDigestException(Throwable cause) {
        super(cause);
    }
}
