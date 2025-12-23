package com.nomoney.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
