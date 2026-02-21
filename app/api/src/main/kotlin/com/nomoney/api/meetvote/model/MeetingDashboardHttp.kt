package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingDashboard
import com.nomoney.meeting.service.MeetingDashboardCard
import com.nomoney.meeting.service.MeetingDashboardSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "주최자 진행 중 모임 대시보드 조회 응답")
data class InProgressMeetingDashboardResponse(
    @Schema(description = "주최자 이름", example = "이파이")
    val hostName: String,

    @Schema(description = "상태별 모임 요약")
    val summary: MeetingDashboardSummaryResponse,

    @Schema(description = "진행 중 모임 목록(VOTING)")
    val meetings: List<MeetingDashboardCardResponse>,
)

@Schema(description = "주최자 확정 모임 대시보드 조회 응답")
data class ConfirmedMeetingDashboardResponse(
    @Schema(description = "주최자 이름", example = "이파이")
    val hostName: String,

    @Schema(description = "상태별 모임 요약")
    val summary: MeetingDashboardSummaryResponse,

    @Schema(description = "확정된 모임 목록(CONFIRMED)")
    val meetings: List<MeetingDashboardCardResponse>,
)

data class MeetingDashboardSummaryResponse(
    @Schema(description = "투표중 모임 개수", example = "3")
    val votingCount: Int,

    @Schema(description = "확정 모임 개수", example = "2")
    val confirmedCount: Int,
)

data class MeetingDashboardCardResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val meetingId: MeetingId,

    @Schema(description = "모임 제목", example = "팀 점심")
    val title: String,

    @Schema(description = "모임 상태", example = "VOTING")
    val status: MeetingStatus,

    @Schema(description = "유력 날짜(최다 득표 기준)", example = "2026-02-20")
    val leadingDate: LocalDate?,

    @Schema(description = "최종 확정 날짜", example = "2026-02-20")
    val finalizedDate: LocalDate?,

    @Schema(description = "투표 완료 인원 수", example = "4")
    val completedVoteCount: Int,

    @Schema(description = "전체 투표 대상 인원 수", example = "6")
    val totalVoteCount: Int,
)

fun MeetingDashboard.toInProgressResponse(): InProgressMeetingDashboardResponse = InProgressMeetingDashboardResponse(
    hostName = this.hostName,
    summary = this.summary.toResponse(),
    meetings = this.inProgressMeetings.map { it.toResponse() },
)

fun MeetingDashboard.toConfirmedResponse(): ConfirmedMeetingDashboardResponse = ConfirmedMeetingDashboardResponse(
    hostName = this.hostName,
    summary = this.summary.toResponse(),
    meetings = this.confirmedMeetings.map { it.toResponse() },
)

private fun MeetingDashboardSummary.toResponse(): MeetingDashboardSummaryResponse = MeetingDashboardSummaryResponse(
    votingCount = this.votingCount,
    confirmedCount = this.confirmedCount,
)

private fun MeetingDashboardCard.toResponse(): MeetingDashboardCardResponse = MeetingDashboardCardResponse(
    meetingId = this.meetingId,
    title = this.title,
    status = this.status,
    leadingDate = this.leadingDate,
    finalizedDate = this.finalizedDate,
    completedVoteCount = this.completedVoteCount,
    totalVoteCount = this.totalVoteCount,
)
