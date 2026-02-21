package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 확정 날짜 충돌 확인 요청")
data class FinalizeMeetingConflictCheckRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(description = "확정할 날짜", example = "2026-02-20", required = true)
    val finalizedDate: LocalDate,
)

@Schema(description = "모임 확정 날짜 충돌 확인 응답")
data class FinalizeMeetingConflictCheckResponse(
    @Schema(description = "겹치는 확정 일정 존재 여부", example = "false")
    val isConflict: Boolean,
)
