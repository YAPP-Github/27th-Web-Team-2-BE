package com.nomoney.oauth.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.oauth.config.KakaoOAuthProperties
import com.nomoney.oauth.dto.KakaoTokenResponse
import com.nomoney.oauth.dto.KakaoUserInfoResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class KakaoOAuthClientTest : DescribeSpec({

    val restTemplate = mockk<RestTemplate>()
    val properties = KakaoOAuthProperties(
        clientId = "test-client-id",
        clientSecret = "test-client-secret",
        redirectUri = "https://example.com/api/v1/auth/oauth/kakao",
    )
    val objectMapper = ObjectMapper()
    val kakaoOAuthClient = KakaoOAuthClient(restTemplate, properties, objectMapper)

    describe("KakaoOAuthClient") {

        describe("supports") {

            it("KAKAO provider를 지원한다") {
                kakaoOAuthClient.supports(SocialProvider.KAKAO) shouldBe true
            }

            it("GOOGLE provider를 지원하지 않는다") {
                kakaoOAuthClient.supports(SocialProvider.GOOGLE) shouldBe false
            }
        }

        describe("getAccessToken") {

            it("인증 코드로 카카오 액세스 토큰을 발급한다") {
                // given
                val authorizationCode = "test-authorization-code"
                val kakaoTokenResponse = KakaoTokenResponse(
                    accessToken = "test-kakao-access-token",
                    expiresIn = 21599,
                    tokenType = "bearer",
                )

                every {
                    restTemplate.postForObject(
                        "https://kauth.kakao.com/oauth/token",
                        any<HttpEntity<*>>(),
                        KakaoTokenResponse::class.java,
                    )
                } returns kakaoTokenResponse

                // when
                val accessToken = kakaoOAuthClient.getAccessToken(authorizationCode, null)

                // then
                accessToken shouldBe "test-kakao-access-token"
            }

            it("state 값이 있을 때 요청 body에 state를 포함하여 액세스 토큰을 발급한다") {
                // given
                val authorizationCode = "test-authorization-code"
                val state = "random-csrf-state"
                val kakaoTokenResponse = KakaoTokenResponse(
                    accessToken = "test-kakao-access-token",
                    expiresIn = 21599,
                    tokenType = "bearer",
                )

                every {
                    restTemplate.postForObject(
                        "https://kauth.kakao.com/oauth/token",
                        any<HttpEntity<*>>(),
                        KakaoTokenResponse::class.java,
                    )
                } returns kakaoTokenResponse

                // when
                val accessToken = kakaoOAuthClient.getAccessToken(authorizationCode, state)

                // then
                accessToken shouldBe "test-kakao-access-token"
            }

            it("RestTemplate 호출 실패 시 RuntimeException이 발생한다") {
                // given
                val authorizationCode = "invalid-code"

                every {
                    restTemplate.postForObject(
                        "https://kauth.kakao.com/oauth/token",
                        any<HttpEntity<*>>(),
                        KakaoTokenResponse::class.java,
                    )
                } throws RuntimeException("카카오 API 오류")

                // when & then
                shouldThrow<RuntimeException> {
                    kakaoOAuthClient.getAccessToken(authorizationCode, null)
                }
            }

            it("응답이 null이면 RuntimeException이 발생한다") {
                // given
                val authorizationCode = "test-code"

                every {
                    restTemplate.postForObject(
                        "https://kauth.kakao.com/oauth/token",
                        any<HttpEntity<*>>(),
                        KakaoTokenResponse::class.java,
                    )
                } returns null

                // when & then
                shouldThrow<RuntimeException> {
                    kakaoOAuthClient.getAccessToken(authorizationCode, null)
                }
            }
        }

        describe("getUserInfo") {

            it("액세스 토큰으로 카카오 사용자 정보를 조회한다") {
                // given
                val accessToken = "test-kakao-access-token"
                val kakaoUserInfoResponse = KakaoUserInfoResponse(
                    id = 12345678L,
                    kakaoAccount = KakaoUserInfoResponse.KakaoAccount(
                        email = "test@kakao.com",
                        profile = KakaoUserInfoResponse.KakaoAccount.Profile(
                            nickname = "홍길동",
                            profileImageUrl = "https://profile.kakaocdn.net/test.jpg",
                        ),
                    ),
                )

                every {
                    restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        any<HttpEntity<*>>(),
                        KakaoUserInfoResponse::class.java,
                    )
                } returns ResponseEntity.ok(kakaoUserInfoResponse)

                // when
                val userInfo = kakaoOAuthClient.getUserInfo(accessToken)

                // then
                userInfo.socialId shouldBe "12345678"
                userInfo.provider shouldBe SocialProvider.KAKAO
                userInfo.email shouldBe "test@kakao.com"
                userInfo.name shouldBe "홍길동"
                userInfo.profileImageUrl shouldBe "https://profile.kakaocdn.net/test.jpg"
            }

            it("kakaoAccount가 null인 경우 email, name, profileImageUrl이 null로 반환된다") {
                // given
                val accessToken = "test-kakao-access-token"
                val kakaoUserInfoResponse = KakaoUserInfoResponse(
                    id = 12345678L,
                    kakaoAccount = null,
                )

                every {
                    restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        any<HttpEntity<*>>(),
                        KakaoUserInfoResponse::class.java,
                    )
                } returns ResponseEntity.ok(kakaoUserInfoResponse)

                // when
                val userInfo = kakaoOAuthClient.getUserInfo(accessToken)

                // then
                userInfo.socialId shouldBe "12345678"
                userInfo.provider shouldBe SocialProvider.KAKAO
                userInfo.email shouldBe null
                userInfo.name shouldBe null
                userInfo.profileImageUrl shouldBe null
            }

            it("profile이 null인 경우 name, profileImageUrl이 null로 반환된다") {
                // given
                val accessToken = "test-kakao-access-token"
                val kakaoUserInfoResponse = KakaoUserInfoResponse(
                    id = 12345678L,
                    kakaoAccount = KakaoUserInfoResponse.KakaoAccount(
                        email = "test@kakao.com",
                        profile = null,
                    ),
                )

                every {
                    restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        any<HttpEntity<*>>(),
                        KakaoUserInfoResponse::class.java,
                    )
                } returns ResponseEntity.ok(kakaoUserInfoResponse)

                // when
                val userInfo = kakaoOAuthClient.getUserInfo(accessToken)

                // then
                userInfo.socialId shouldBe "12345678"
                userInfo.email shouldBe "test@kakao.com"
                userInfo.name shouldBe null
                userInfo.profileImageUrl shouldBe null
            }

            it("RestTemplate 호출 실패 시 RuntimeException이 발생한다") {
                // given
                val accessToken = "invalid-token"

                every {
                    restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        any<HttpEntity<*>>(),
                        KakaoUserInfoResponse::class.java,
                    )
                } throws RuntimeException("카카오 API 오류")

                // when & then
                shouldThrow<RuntimeException> {
                    kakaoOAuthClient.getUserInfo(accessToken)
                }
            }

            it("응답 body가 null이면 RuntimeException이 발생한다") {
                // given
                val accessToken = "test-kakao-access-token"

                every {
                    restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        any<HttpEntity<*>>(),
                        KakaoUserInfoResponse::class.java,
                    )
                } returns ResponseEntity.ok(null)

                // when & then
                shouldThrow<RuntimeException> {
                    kakaoOAuthClient.getUserInfo(accessToken)
                }
            }
        }
    }
},)
