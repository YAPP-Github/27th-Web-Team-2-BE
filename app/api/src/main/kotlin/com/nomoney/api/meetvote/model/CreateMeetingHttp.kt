package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 생성 요청")
data class CreateMeetingRequest(
    @Schema(description = "모임 제목", example = "팀 회식", required = true)
    val title: String,

    @Schema(description = "주최자 이름", example = "이파이", required = true)
    val hostName: String,

//    @Schema(description = "최대 참여 인원 (제한 없으면 null)", example = "10")
//    val maxParticipantCount: Int?,

    @Schema(description = "모임 가능한 날짜 목록", example = "[\"2025-01-15\", \"2025-01-16\", \"2025-01-17\"]", required = true)
    val dates: List<LocalDate>,
)

@Schema(description = "모임 생성 응답")
data class CreateMeetingResponse(
    @Schema(description = "생성된 모임 ID", example = "aBcDeFgHiJ")
    val id: MeetingId,
)
