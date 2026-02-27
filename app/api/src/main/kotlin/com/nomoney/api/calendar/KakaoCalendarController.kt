package com.nomoney.api.calendar

import com.nomoney.api.auth.getSecurityUserIdOrThrow
import com.nomoney.api.calendar.model.CreateKakaoCalendarEventRequest
import com.nomoney.api.calendar.model.CreateKakaoCalendarEventResponse
import com.nomoney.api.swagger.SwaggerApiTag
import com.nomoney.calendar.service.KakaoCalendarService
import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody as SpringRequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerApiTag.CALENDAR, description = "카카오 캘린더 관련 API")
@RestController
class KakaoCalendarController(
    private val kakaoCalendarService: KakaoCalendarService,
) {

    @Operation(summary = "카카오 캘린더 이벤트 생성", description = "인증된 사용자의 카카오 계정으로 종일 캘린더 이벤트를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "404", description = "모임 또는 카카오 토큰 없음"),
        ],
    )
    @PostMapping("/api/v1/calendar/kakao/events")
    fun createKakaoCalendarEvent(
        @RequestBody(
            required = true,
            content = [Content(schema = Schema(implementation = CreateKakaoCalendarEventRequest::class))],
        )
        @SpringRequestBody
        request: CreateKakaoCalendarEventRequest,
    ): CreateKakaoCalendarEventResponse {
        val userId = getSecurityUserIdOrThrow()
        val createdEvent = kakaoCalendarService.createMeetingEvent(
            userId = userId,
            meetingId = MeetingId(request.meetId),
            eventDate = request.eventDate,
            title = request.title,
        )

        return CreateKakaoCalendarEventResponse(eventId = createdEvent.eventId)
    }
}
