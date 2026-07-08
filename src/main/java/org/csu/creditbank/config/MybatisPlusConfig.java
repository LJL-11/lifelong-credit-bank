package org.csu.creditbank.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.csu.creditbank.util.InstitutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class MybatisPlusConfig {

    /** 跨机构共享表（不加 institution_id 过滤） */
    private static final Set<String> SHARED_TABLES = Set.of(
            "credit_product", "credit_order", "credit_order_detail",
            "credit_account", "credit_transaction", "cart",
            "flash_sale", "flash_sale_record", "institution",
            "forum_post_like"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        TenantLineInnerInterceptor tenant = new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long id = InstitutionContext.get();
                return new LongValue(id != null ? id : 0);
            }
            @Override
            public String getTenantIdColumn() {
                return "institution_id";
            }
            @Override
            public boolean ignoreTable(String tableName) {
                // 未登录时不启用多租户过滤（登录接口需要查用户）
                if (InstitutionContext.get() == null) return true;
                return SHARED_TABLES.contains(tableName) || "flyway_schema_history".equals(tableName);
            }
        });
        interceptor.addInnerInterceptor(tenant);

        return interceptor;
    }
}
