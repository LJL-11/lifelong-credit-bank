package org.csu.creditbank.util;

/** ThreadLocal 存储当前请求的机构ID，供 MyBatis-Plus 多租户插件读取 */
public class InstitutionContext {

    private static final ThreadLocal<Long> INSTITUTION_ID = new ThreadLocal<>();

    public static void setInstitutionId(Long id) { INSTITUTION_ID.set(id); }
    public static Long getInstitutionId() { return INSTITUTION_ID.get(); }
    public static void clear() { INSTITUTION_ID.remove(); }
}
