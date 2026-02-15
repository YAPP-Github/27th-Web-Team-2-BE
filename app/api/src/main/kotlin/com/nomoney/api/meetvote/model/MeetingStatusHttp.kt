package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 마감 요청")
data class CloseMeetingRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,
)

@Schema(description = "모임 마감 응답")
data class CloseMeetingResponse(
    @Schema(description = "변경된 모임 상태", example = "CLOSED")
    val status: MeetingStatus,
)

@Schema(description = "모임 확정 요청")
data class FinalizeMeetingRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(
        description = "최종 확정 날짜 (공동 1위인 경우 필수)",
        example = "2026-02-20",
        required = false,
    )
    val finalizedDate: LocalDate? = null,
)

@Schema(description = "모임 확정 응답")
data class FinalizeMeetingResponse(
    @Schema(description = "변경된 모임 상태", example = "CONFIRMED")
    val status: MeetingStatus,

    @Schema(description = "최종 확정 날짜", example = "2026-02-20")
    val finalizedDate: LocalDate,
)
