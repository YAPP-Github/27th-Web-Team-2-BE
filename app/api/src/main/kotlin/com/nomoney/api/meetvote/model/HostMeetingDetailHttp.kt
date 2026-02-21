package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingHostDetail
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "주최자용 모임 상세 조회 응답")
data class HostMeetingDetailResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val id: MeetingId,

    @Schema(description = "모임 제목", example = "팀 회식")
    val title: String,

    @Schema(description = "모임 가능한 날짜 목록, 정렬 되어 있음", example = "[\"2026-02-20\", \"2026-02-21\"]")
    val dates: List<LocalDate>,

    @Schema(description = "모임 상태", example = "VOTING")
    val status: MeetingStatus,

    @Schema(description = "최종 확정 날짜 (확정 전 null)", example = "2026-02-20")
    val finalizedDate: LocalDate?,

    @Schema(description = "전체 투표 참여 인원(제한 없으면 null)", example = "10")
    val maxParticipantCount: Int?,

    @Schema(description = "투표 참여자 정보")
    val participants: List<ParticipantResponse>,

    @Schema(description = "모임 주최자 이름")
    val hostName: String?,

    @Schema(description = "주최자 메모", nullable = true, example = "회의실 예약 완료")
    val memo: String?,

    @Schema(description = "현재 미투표 인원 수", example = "3")
    val notVotedParticipantCount: Int,
)

fun MeetingHostDetail.toHostDetailResponse(): HostMeetingDetailResponse = HostMeetingDetailResponse(
    id = this.meeting.id,
    title = this.meeting.title,
    dates = this.meeting.dates.toList().sorted(),
    status = this.meeting.status,
    finalizedDate = this.meeting.finalizedDate,
    maxParticipantCount = this.meeting.maxParticipantCount,
    participants = this.meeting.participants.map { it.toResponse() },
    hostName = this.meeting.hostName,
    memo = this.meeting.memo,
    notVotedParticipantCount = this.notVotedParticipantCount,
)
