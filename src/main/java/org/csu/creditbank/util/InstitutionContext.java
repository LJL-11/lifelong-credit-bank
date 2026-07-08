package org.csu.creditbank.util;

public class InstitutionContext {
    private static final ThreadLocal<Long> INSTITUTION_ID = new ThreadLocal<>();
    public static void set(Long id) { INSTITUTION_ID.set(id); }
    public static Long get() { return INSTITUTION_ID.get(); }
    public static void clear() { INSTITUTION_ID.remove(); }
}
