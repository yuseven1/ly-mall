package com.leyou.upload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsConfiguration {

    @Bean
    public CorsFilter corsFilter() {
        //初始化cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许跨域的域名，如果要携带cookies，不能设置 "*.*" (代表所有域名都可以跨域)
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");
        // 设置允许携带cookies
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedMethod("*"); // * 代表所有请求方法 get post delete put
        corsConfiguration.addAllowedHeader("*"); // 允许携带任何头信息

        // 初始化cors配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);
        // 返回corsFilter对象，参数是cors配置源对象
        return new CorsFilter(configurationSource);
    }
}
