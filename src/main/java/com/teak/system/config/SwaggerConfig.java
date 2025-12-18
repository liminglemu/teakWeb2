package com.teak.system.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI teakWebAPI() {
        return new OpenAPI()
                .openapi("3.1.0")
                .servers(
                        List.of(
                                new Server().url("http://localhost:8001").description("本地开发服务器"),
                                new Server().url("http://localhost:8080").description("默认服务器")
                        )
                )
                .info(new Info().title("Teak Web API")
                        .description("Teak Web应用程序API文档")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("外部文档")
                        .url("https://springshop.wiki.github.org/docs"));
    }
}