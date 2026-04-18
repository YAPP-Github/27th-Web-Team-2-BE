package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "모임 생성 요청")
data class CreateMeetingRequest(
    @Schema(description = "모임 제목", example = "팀 회식", required = true)
    val title: String,

    @Schema(description = "주최자 이름", example = "이파이", required = true)
    val hostName: String,

    @Schema(description = "최대 참여 인원 (제한 없으면 null)", example = "10")
    val maxParticipantCount: Int?,

    @Schema(description = "모임 가능한 날짜 목록", example = "[\"2025-01-15\", \"2025-01-16\", \"2025-01-17\"]", required = true)
    val dates: List<LocalDate>,

    @Schema(description = "시간 투표 범위 (null이면 날짜 전용 모드)")
    val timeRange: TimeRangeRequest? = null,
)

@Schema(description = "시간 범위 요청")
data class TimeRangeRequest(
    @Schema(description = "시작 시간", example = "09:00")
    val startTime: LocalTime,

    @Schema(description = "종료 시간", example = "18:00")
    val endTime: LocalTime,
)

@Schema(description = "모임 생성 응답")
data class CreateMeetingResponse(
    @Schema(description = "생성된 모임 ID", example = "aBcDeFgHiJ")
    val id: MeetingId,
)
