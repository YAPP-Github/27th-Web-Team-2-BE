package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 요약 정보")
data class MeetingSummaryResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val id: MeetingId,

    @Schema(description = "모임 제목", example = "팀 회식")
    val title: String,

    @Schema(description = "주최자 이름", example = "이파이")
    val hostName: String?,

    @Schema(description = "모임 상태", example = "VOTING")
    val status: MeetingStatus,

    @Schema(description = "최종 확정 날짜 (확정 전 null)", example = "2026-02-20")
    val finalizedDate: LocalDate?,
)

fun Meeting.toSummaryResponse(): MeetingSummaryResponse = MeetingSummaryResponse(
    id = this.id,
    title = this.title,
    hostName = this.hostName,
    status = this.status,
    finalizedDate = this.finalizedDate,
)
