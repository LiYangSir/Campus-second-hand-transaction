package com.quguai.campustransaction.product.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.quguai.campustransaction.product.dao")
public class MyBatisPlusConfig {

//    @Bean
//    public PaginationInnerInterceptor paginationInterceptor() {
//
//        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
//        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
//        paginationInnerInterceptor.setOverflow(true);
//        // 设置最大单页限制数量，默认 500 条，-1 不受限制
//         paginationInnerInterceptor.setMaxLimit(1000L);
//        // 开启 count 的 join 优化,只针对部分 left join
//        return paginationInnerInterceptor;
//    }
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
//        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return mybatisPlusInterceptor;
    }
}
