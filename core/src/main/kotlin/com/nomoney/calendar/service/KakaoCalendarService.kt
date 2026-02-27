package com.nomoney.calendar.service

import com.nomoney.auth.domain.SocialOAuthToken
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.KakaoOAuthRepository
import com.nomoney.auth.port.SocialOAuthTokenRepository
import com.nomoney.calendar.domain.KakaoCalendarEvent
import com.nomoney.calendar.domain.KakaoCalendarEventCreateCommand
import com.nomoney.calendar.port.KakaoCalendarRepository
import com.nomoney.exception.InvalidRequestException
import com.nomoney.exception.NotFoundException
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.port.MeetingRepository
import java.time.LocalDate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException

@Service
class KakaoCalendarService(
    private val meetingRepository: MeetingRepository,
    private val socialOAuthTokenRepository: SocialOAuthTokenRepository,
    private val kakaoOAuthRepository: KakaoOAuthRepository,
    private val kakaoCalendarRepository: KakaoCalendarRepository,
) {

    @Transactional
    fun createMeetingEvent(
        userId: UserId,
        meetingId: MeetingId,
        eventDate: LocalDate,
        title: String,
    ): KakaoCalendarEvent {
        val meeting = meetingRepository.findByMeetingId(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "meetingId=${meetingId.value}")

        if (!meeting.dates.contains(eventDate)) {
            throw InvalidRequestException(
                "모임에서 선택 가능한 날짜가 아닙니다.",
                "meetingId=${meetingId.value}, eventDate=$eventDate",
            )
        }

        val savedToken = socialOAuthTokenRepository.findByUserIdAndProvider(userId, SocialProvider.KAKAO)
            ?: throw NotFoundException("카카오 OAuth 토큰을 찾을 수 없습니다.", "userId=${userId.value}")

        val command = KakaoCalendarEventCreateCommand(
            title = title,
            description = null,
            location = null,
            startDate = eventDate,
            endDate = eventDate,
        )

        return tryCreateEvent(command, savedToken)
    }

    private fun tryCreateEvent(
        command: KakaoCalendarEventCreateCommand,
        savedToken: SocialOAuthToken,
    ): KakaoCalendarEvent {
        return try {
            kakaoCalendarRepository.createEvent(savedToken.accessToken, command)
        } catch (e: HttpClientErrorException) {
            if (e.statusCode != HttpStatus.UNAUTHORIZED) {
                throw InvalidRequestException(
                    "카카오 캘린더 이벤트 생성에 실패했습니다.",
                    "status=${e.statusCode}, body=${e.responseBodyAsString}",
                    e,
                )
            }

            val refreshedToken = refreshKakaoAccessToken(savedToken)

            try {
                kakaoCalendarRepository.createEvent(refreshedToken.accessToken, command)
            } catch (retryException: Exception) {
                throw InvalidRequestException(
                    "카카오 캘린더 이벤트 생성에 실패했습니다.",
                    retryException.message,
                    retryException,
                )
            }
        } catch (e: Exception) {
            throw InvalidRequestException(
                "카카오 캘린더 이벤트 생성에 실패했습니다.",
                e.message,
                e,
            )
        }
    }

    private fun refreshKakaoAccessToken(savedToken: SocialOAuthToken): SocialOAuthToken {
        val refreshToken = savedToken.refreshToken
            ?: throw InvalidRequestException(
                "카카오 리프레시 토큰이 없습니다.",
                "userId=${savedToken.userId.value}",
            )

        val refreshed = try {
            kakaoOAuthRepository.refreshOAuthToken(refreshToken)
        } catch (e: Exception) {
            throw InvalidRequestException(
                "카카오 토큰 갱신에 실패했습니다.",
                e.message,
                e,
            )
        }

        val updatedToken = savedToken.copy(
            accessToken = refreshed.accessToken,
            refreshToken = refreshed.refreshToken ?: savedToken.refreshToken,
            accessTokenExpiresAt = refreshed.accessTokenExpiresAt,
            refreshTokenExpiresAt = refreshed.refreshTokenExpiresAt ?: savedToken.refreshTokenExpiresAt,
            scope = refreshed.scope ?: savedToken.scope,
        )

        return socialOAuthTokenRepository.upsert(updatedToken)
    }
}
