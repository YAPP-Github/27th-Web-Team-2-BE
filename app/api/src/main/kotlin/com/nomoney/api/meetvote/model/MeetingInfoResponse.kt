package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
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

    @Schema(description = "최대 모임 참여자 수, 정해지지 않았으면 null")
    val maxParticipantCount: Int?,

    @Schema(description = "투표에 참여한 참여자 정보")
    val participants: List<ParticipantResponse>,
)

data class ParticipantResponse(
    val id: ParticipantId,
    val name: String,
    val voteDates: List<LocalDate>,
    val hasVoted: Boolean,
)

fun Meeting.toResponse(): MeetingInfoResponse = MeetingInfoResponse(
    id = this.id,
    title = this.title,
    dates = this.dates.toList().sorted(),
    maxParticipantCount = this.maxParticipantCount,
    participants = this.participants.map { it.toResponse() },
)

fun Participant.toResponse(): ParticipantResponse = ParticipantResponse(
    id = id,
    name = this.name,
    voteDates = this.voteDates.toList().sorted(),
    hasVoted = this.hasVoted,
)
