package com.nomoney.exception

sealed class NoMoneyException(
    val code: String,
    message: String,
    val messageForDev: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

sealed class ClientException(code: String, message: String, messageForDev: String? = null, cause: Throwable? = null) : NoMoneyException(code, message, messageForDev, cause)

class UnauthorizedException(messageForDev: String) : ClientException("Auth401", "인증을 실패했습니다.", messageForDev)
class InvalidRefreshTokenException(messageForDev: String) : ClientException("E003", "토큰이 유효하지 않습니다.", messageForDev)

class NotFoundException(message: String, messageForDev: String?) : ClientException("E001", message, messageForDev)
class DuplicateContentException(message: String, messageForDev: String?) : ClientException("E002", message, messageForDev)
class InvalidRequestException(message: String, messageForDev: String?, cause: Throwable? = null) :
    ClientException("E003", message, messageForDev, cause)
