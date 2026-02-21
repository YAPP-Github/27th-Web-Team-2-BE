package com.nomoney.api.exception

import com.nomoney.exception.ClientException
import com.nomoney.exception.NoMoneyException
import com.nomoney.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = ex.code,
            message = ex.message ?: "인증을 실패했습니다.",
            messageForDev = ex.messageForDev,
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val errorResponse = ErrorResponse(
            code = "E400",
            message = "잘못된 요청입니다.",
            messageForDev = "검증 실패: $errors",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E400",
            message = "잘못된 요청입니다.",
            messageForDev = "요청 본문을 읽을 수 없습니다: ${ex.message}",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E400",
            message = "잘못된 요청입니다.",
            messageForDev = "필수 파라미터 누락: ${ex.parameterName} (타입: ${ex.parameterType})",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E400",
            message = "잘못된 요청입니다.",
            messageForDev = "파라미터 타입 불일치 '${ex.name}': 기대값 ${ex.requiredType?.simpleName}, 입력값 ${ex.value}",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E405",
            message = "지원하지 않는 HTTP 메서드입니다.",
            messageForDev = "메서드 '${ex.method}'는 지원되지 않습니다. 지원되는 메서드: ${ex.supportedHttpMethods?.joinToString(", ")}",
        )
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E415",
            message = "지원하지 않는 미디어 타입입니다.",
            messageForDev = "Content type '${ex.contentType}'는 지원되지 않습니다. 지원되는 타입: ${ex.supportedMediaTypes.joinToString(", ")}",
        )
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E404",
            message = "요청한 리소스를 찾을 수 없습니다.",
            messageForDev = "핸들러를 찾을 수 없습니다: ${ex.httpMethod} ${ex.requestURL}",
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
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
