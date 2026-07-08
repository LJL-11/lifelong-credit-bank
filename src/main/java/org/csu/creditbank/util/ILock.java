package org.csu.creditbank.util;

public interface ILock {
    boolean tryLock(long timeoutSec);
    void unLock();
}
