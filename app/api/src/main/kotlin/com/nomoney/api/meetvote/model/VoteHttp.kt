package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "투표 생성 요청")
data class VoteRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(description = "투표자 이름", example = "홍길동", required = true)
    val name: String,

    @Schema(description = "투표한 날짜 목록", example = "[\"2025-01-15\", \"2025-01-16\"]", required = true)
    val voteDates: List<LocalDate>,
)

@Schema(description = "투표 응답")
data class VoteResponse(
    @Schema(description = "성공 여부", example = "true")
    val success: Boolean,
)
