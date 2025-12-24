package com.nomoney.api

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
class HealthCheckController {
    @GetMapping("/ping")
    fun healthCheck() = "pong!"
}
