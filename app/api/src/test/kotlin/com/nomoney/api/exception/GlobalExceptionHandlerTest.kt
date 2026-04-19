package com.nomoney.api.exception

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.resource.NoResourceFoundException

class GlobalExceptionHandlerTest : DescribeSpec({
    val handler = GlobalExceptionHandler()

    describe("GlobalExceptionHandler") {
        it("정적 리소스 미존재 예외는 404로 응답한다") {
            val exception = NoResourceFoundException(
                HttpMethod.GET,
                "/api/vendor/phpunit/phpunit/src/Util/PHP/eval-stdin.php",
            )

            val response = handler.handleNoResourceFoundException(exception)

            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body shouldBe ErrorResponse(
                code = "E404",
                message = "요청한 리소스를 찾을 수 없습니다.",
                messageForDev = "정적 리소스를 찾을 수 없습니다: GET /api/vendor/phpunit/phpunit/src/Util/PHP/eval-stdin.php",
            )
        }
    }
},)
