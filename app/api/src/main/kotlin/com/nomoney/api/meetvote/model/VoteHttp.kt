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

    @Schema(description = "투표한 날짜 목록 (날짜 전용 모드)", example = "[\"2025-01-15\", \"2025-01-16\"]")
    val voteDates: List<LocalDate> = emptyList(),

    @Schema(description = "시간 슬롯 투표 (시간 모드): [날짜 수][범위 내 슬롯 수]. voteTimeSlots[i][j] = dates[i]의 j번째 슬롯 가능 여부")
    val voteTimeSlots: List<List<Boolean>>? = null,
)

@Schema(description = "투표 응답")
data class VoteResponse(
    @Schema(description = "성공 여부", example = "true")
    val success: Boolean,
)
