package com.nomoney.auth.adapter

import com.nomoney.auth.domain.RefreshToken
import com.nomoney.auth.domain.RefreshTokenId
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.RefreshTokenJpaEntity
import com.nomoney.auth.repository.RefreshTokenJpaRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class RefreshTokenAdapterTest : DescribeSpec({

    val refreshTokenJpaRepository = mockk<RefreshTokenJpaRepository>()
    val refreshTokenAdapter = RefreshTokenAdapter(refreshTokenJpaRepository)

    describe("RefreshTokenAdapter") {

        describe("save") {
            it("RefreshToken을 저장하고 도메인 객체를 반환한다") {
                // given
                val now = LocalDateTime.now()
                val expiresAt = now.plusDays(90)
                val refreshToken = RefreshToken(
                    id = RefreshTokenId(0L),
                    tokenValue = "test-refresh-token-value",
                    userId = UserId(100L),
                    expiresAt = expiresAt,
                    used = false,
                    createdAt = now,
                )

                val savedEntity = RefreshTokenJpaEntity.of(
                    refreshTokenId = 1L,
                    tokenValue = "test-refresh-token-value",
                    userId = 100L,
                    expiresAt = expiresAt,
                    used = false,
                )

                every { refreshTokenJpaRepository.save(any()) } returns savedEntity

                // when
                val result = refreshTokenAdapter.save(refreshToken)

                // then
                result shouldNotBe null
                result.tokenValue shouldBe "test-refresh-token-value"
                result.userId.value shouldBe 100L
                result.expiresAt shouldBe expiresAt
                result.used shouldBe false

                verify(exactly = 1) { refreshTokenJpaRepository.save(any()) }
            }
        }

        describe("findByTokenValue") {
            it("토큰 값으로 조회하면 도메인 객체를 반환한다") {
                // given
                val tokenValue = "existing-refresh-token"
                val now = LocalDateTime.now()
                val expiresAt = now.plusDays(90)

                val entity = RefreshTokenJpaEntity.of(
                    refreshTokenId = 1L,
                    tokenValue = tokenValue,
                    userId = 100L,
                    expiresAt = expiresAt,
                    used = false,
                )

                every { refreshTokenJpaRepository.findByTokenValue(tokenValue) } returns entity

                // when
                val result = refreshTokenAdapter.findByTokenValue(tokenValue)

                // then
                result shouldNotBe null
                result!!.id.value shouldBe 1L
                result.tokenValue shouldBe tokenValue
                result.userId.value shouldBe 100L
                result.expiresAt shouldBe expiresAt
                result.used shouldBe false

                verify(exactly = 1) { refreshTokenJpaRepository.findByTokenValue(tokenValue) }
            }

            it("존재하지 않는 토큰 값으로 조회하면 null을 반환한다") {
                // given
                val tokenValue = "non-existing-token"

                every { refreshTokenJpaRepository.findByTokenValue(tokenValue) } returns null

                // when
                val result = refreshTokenAdapter.findByTokenValue(tokenValue)

                // then
                result shouldBe null

                verify(exactly = 1) { refreshTokenJpaRepository.findByTokenValue(tokenValue) }
            }
        }

        describe("markAsUsed") {
            it("토큰을 사용됨으로 마킹한다") {
                // given
                val tokenValue = "token-to-mark-as-used"
                val now = LocalDateTime.now()
                val expiresAt = now.plusDays(90)

                val entity = RefreshTokenJpaEntity.of(
                    refreshTokenId = 1L,
                    tokenValue = tokenValue,
                    userId = 100L,
                    expiresAt = expiresAt,
                    used = false,
                )

                val usedEntity = RefreshTokenJpaEntity.of(
                    refreshTokenId = 1L,
                    tokenValue = tokenValue,
                    userId = 100L,
                    expiresAt = expiresAt,
                    used = true,
                )

                every { refreshTokenJpaRepository.findByTokenValue(tokenValue) } returns entity
                every { refreshTokenJpaRepository.save(any()) } returns usedEntity

                // when
                refreshTokenAdapter.markAsUsed(tokenValue)

                // then
                verify(exactly = 1) { refreshTokenJpaRepository.findByTokenValue(tokenValue) }
                verify(exactly = 1) { refreshTokenJpaRepository.save(match { it.used }) }
            }
        }
    }
},)
