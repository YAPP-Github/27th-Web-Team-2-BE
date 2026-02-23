package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 수정 요청")
data class UpdateMeetingRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(description = "수정할 모임 제목", example = "수정된 팀 회식", required = true)
    val title: String,

    @Schema(description = "수정할 최대 참여 인원(제한 없으면 null)", example = "10")
    val maxParticipantCount: Int?,

    @Schema(description = "수정할 모임 후보 날짜 목록", example = "[\"2026-02-20\", \"2026-02-21\"]", required = true)
    val dates: List<LocalDate>,

    @Schema(description = "삭제할 참여자 이름 목록", example = "[\"참여자A\", \"참여자B\"]")
    val removedParticipantNames: List<String> = emptyList(),
)

@Schema(description = "모임 수정 응답")
data class UpdateMeetingResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val meetingId: MeetingId,

    @Schema(description = "모임 제목", example = "수정된 팀 회식")
    val title: String,

    @Schema(description = "최대 참여 인원", example = "10")
    val maxParticipantCount: Int?,

    @Schema(description = "모임 후보 날짜 목록", example = "[\"2026-02-20\", \"2026-02-21\"]")
    val dates: List<LocalDate>,

    @Schema(description = "참여자 수", example = "4")
    val participantCount: Int,
)

fun Meeting.toUpdateResponse(): UpdateMeetingResponse = UpdateMeetingResponse(
    meetingId = this.id,
    title = this.title,
    maxParticipantCount = this.maxParticipantCount,
    dates = this.dates.sorted(),
    participantCount = this.participants.size,
)
