package com.nomoney.api.exception

import com.nomoney.exception.ClientException
import com.nomoney.exception.NoMoneyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ClientException::class)
    fun handleClientException(ex: ClientException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = ex.code,
            message = ex.message ?: "클라이언트 오류가 발생했습니다.",
            messageForDev = ex.messageForDev,
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(NoMoneyException::class)
    fun handleNoMoneyException(ex: NoMoneyException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = ex.code,
            message = ex.message ?: "서버 오류가 발생했습니다.",
            messageForDev = ex.messageForDev,
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E999",
            message = "알 수 없는 오류가 발생했습니다.",
            messageForDev = ex.message,
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
