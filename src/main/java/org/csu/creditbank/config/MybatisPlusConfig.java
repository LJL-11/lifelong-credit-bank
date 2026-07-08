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

    /** 跨机构共享的表（不加 institution_id 过滤） */
    private static final Set<String> SHARED_TABLES = Set.of(
            "credit_product", "credit_order", "credit_order_detail",
            "credit_account", "credit_transaction", "cart",
            "flash_sale", "flash_sale_record", "institution"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 多租户插件：自动给机构专属表追加 WHERE institution_id = ?
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long id = InstitutionContext.getInstitutionId();
                return new LongValue(id != null ? id : 0);
            }

            @Override
            public String getTenantIdColumn() {
                return "institution_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return SHARED_TABLES.contains(tableName)
                        || "flyway_schema_history".equals(tableName);
            }
        });
        interceptor.addInnerInterceptor(tenantInterceptor);

        return interceptor;
    }
}
