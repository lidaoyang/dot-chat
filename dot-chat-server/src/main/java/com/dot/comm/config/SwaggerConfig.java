package com.dot.comm.config;

import com.dot.comm.em.ExceptionCodeEm;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;

/**
 * Swagger配置组件
 */
@Slf4j
@Data
@Configuration
public class SwaggerConfig {

    /**
     * 首页标题
     */
    @Value("${swagger.home.title}")
    private String title;

    /**
     * 首页简介
     */
    @Value("${swagger.home.description}")
    private String description;

    /**
     * 接口版本
     */
    @Value("${swagger.home.version}")
    private String version;

    /**
     * API服务条款
     */
    @Value("${swagger.home.terms-of-service}")
    private String termsOfService;

    /**
     * license
     */
    @Value("${swagger.home.license-name}")
    private String licenseName;

    @Value("${swagger.home.license-url}")
    private String licenseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .termsOfService(termsOfService)
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION))
                .components(new Components().addSecuritySchemes(HttpHeaders.AUTHORIZATION,
                        new SecurityScheme().name(HttpHeaders.AUTHORIZATION).type(SecurityScheme.Type.HTTP).scheme("bearer ")));
    }


    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            // 全局添加鉴权参数
            if (openApi.getPaths() != null) {
                // 为所有接口添加鉴权
                openApi.getPaths().forEach((s, pathItem) -> {
                    // 为所有接口添加鉴权
                    pathItem.readOperations().forEach(operation -> {
                        operation.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
                    });
                });
                // 自定义全局返回码
                openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                    // 自定义全局返回码
                    Arrays.stream(ExceptionCodeEm.values()).forEach(errEm -> {
                        ApiResponses responses = operation.getResponses();
                        String resKey = String.valueOf(errEm.getCode());
                        if (responses.containsKey(resKey)) {
                            ApiResponse apiResponse = responses.get(resKey);
                            apiResponse.description(errEm.getMessage());
                        } else {
                            responses.put(resKey, new ApiResponse().description(errEm.getMessage()));
                        }
                    });
                }));
            }
        };
    }
}
