package com.nomoney.exception

import java.lang.RuntimeException

sealed class NoMoneyException(
    val code: String,
    message: String,
    val messageForDev: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

sealed class ClientException(code: String, message: String, messageForDev: String? = null, cause: Throwable? = null) : NoMoneyException(code, message, messageForDev, cause)

class NotFoundException(message: String, messageForDev: String?) : ClientException("E001", message, messageForDev)
class DuplicateContentException(message: String, messageForDev: String?) : ClientException("E002", message, messageForDev)
