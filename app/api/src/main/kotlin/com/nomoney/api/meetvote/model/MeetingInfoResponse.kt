package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 정보 응답")
data class MeetingInfoResponse(
    @Schema(description = "모임 ID", example = "asdfqwer")
    val id: MeetingId,

    @Schema(description = "모임 제목", example = "팀 회식")
    val title: String,

    @Schema(description = "모임 가능한 날짜 목록, 정렬 되어 있음", example = "[\"2025-01-15\", \"2025-01-16\", \"2025-01-17\"]")
    val dates: List<LocalDate>,
)

fun Meeting.toResponse(): MeetingInfoResponse = MeetingInfoResponse(
    id = this.id,
    title = this.title,
    dates = this.dates.toList().sorted()
)
