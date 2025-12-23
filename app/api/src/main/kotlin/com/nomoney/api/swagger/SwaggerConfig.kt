package com.nomoney.api.swagger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("NoMoney API")
                    .description("NoMoney API 문서")
                    .version("v1.0.0")
            )
            .servers(
                listOf(
                    Server().url("/").description("Current Server")
                )
            )
    }
}
