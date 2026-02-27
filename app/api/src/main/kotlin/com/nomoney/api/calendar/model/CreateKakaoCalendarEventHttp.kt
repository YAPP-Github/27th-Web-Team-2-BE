package com.nomoney.api.calendar.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "카카오 캘린더 이벤트 생성 요청")
data class CreateKakaoCalendarEventRequest(
    @field:Schema(description = "모임 고유 ID", example = "aBcDeFgHiJ", required = true)
    val meetId: String,
    @field:Schema(description = "등록할 일정 날짜(종일)", example = "2026-03-15", required = true)
    val eventDate: LocalDate,
    @field:Schema(description = "카카오 캘린더 이벤트 제목", example = "예식장 방문 일정", required = true)
    val title: String,
)

@Schema(description = "카카오 캘린더 이벤트 생성 응답")
data class CreateKakaoCalendarEventResponse(
    @field:Schema(description = "카카오 캘린더 이벤트 ID", example = "e4d7c2f1")
    val eventId: String,
)
