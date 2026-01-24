package com.nomoney.api

import com.nomoney.api.swagger.SwaggerApiTag
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RestController

@Profile("local", "sandbox")
@Tag(name = SwaggerApiTag.TEST)
@RestController
class TestController()
