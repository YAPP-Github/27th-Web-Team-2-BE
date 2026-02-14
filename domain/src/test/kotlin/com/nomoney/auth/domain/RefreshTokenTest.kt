package com.nomoney.auth.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class RefreshTokenTest : DescribeSpec({

    describe("RefreshToken") {

        describe("생성") {
            it("RefreshToken이 올바르게 생성된다") {
                val now = LocalDateTime.now()
                val expiresAt = now.plusDays(90)

                val refreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = "test-refresh-token-value",
                    userId = UserId(100L),
                    expiresAt = expiresAt,
                    used = false,
                    createdAt = now,
                )

                refreshToken.id.value shouldBe 1L
                refreshToken.tokenValue shouldBe "test-refresh-token-value"
                refreshToken.userId.value shouldBe 100L
                refreshToken.expiresAt shouldBe expiresAt
                refreshToken.used shouldBe false
                refreshToken.createdAt shouldBe now
            }
        }

        describe("isExpired") {
            it("만료되지 않은 토큰은 false를 반환한다") {
                val refreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = "test-token",
                    userId = UserId(1L),
                    expiresAt = LocalDateTime.now().plusDays(1),
                    used = false,
                    createdAt = LocalDateTime.now(),
                )

                refreshToken.isExpired() shouldBe false
            }

            it("만료된 토큰은 true를 반환한다") {
                val refreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = "test-token",
                    userId = UserId(1L),
                    expiresAt = LocalDateTime.now().minusDays(1),
                    used = false,
                    createdAt = LocalDateTime.now().minusDays(2),
                )

                refreshToken.isExpired() shouldBe true
            }
        }

        describe("used 플래그") {
            it("사용되지 않은 토큰은 used가 false이다") {
                val refreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = "test-token",
                    userId = UserId(1L),
                    expiresAt = LocalDateTime.now().plusDays(90),
                    used = false,
                    createdAt = LocalDateTime.now(),
                )

                refreshToken.used shouldBe false
            }

            it("사용된 토큰은 used가 true이다") {
                val refreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = "test-token",
                    userId = UserId(1L),
                    expiresAt = LocalDateTime.now().plusDays(90),
                    used = true,
                    createdAt = LocalDateTime.now(),
                )

                refreshToken.used shouldBe true
            }
        }
    }
},)
