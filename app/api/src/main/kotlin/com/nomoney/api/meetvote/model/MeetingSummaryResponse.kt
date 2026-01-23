package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "모임 요약 정보")
data class MeetingSummaryResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val id: MeetingId,

    @Schema(description = "모임 제목", example = "팀 회식")
    val title: String,

    @Schema(description = "주최자 이름", example = "이파이")
    val hostName: String?,
)

fun Meeting.toSummaryResponse(): MeetingSummaryResponse = MeetingSummaryResponse(
    id = this.id,
    title = this.title,
    hostName = this.hostName,
)
