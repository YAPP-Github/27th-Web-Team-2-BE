package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingDashboard
import com.nomoney.meeting.service.MeetingDashboardCard
import com.nomoney.meeting.service.MeetingDashboardSummary
import com.nomoney.meeting.service.MeetingDateVoteDetail
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

    @Schema(description = "유력 날짜(최다 득표, 동률 시 가장 빠른 날짜)", example = "2026-02-20")
    val leadingDate: LocalDate?,

    @Schema(description = "유력 날짜 공동 1위 여부", example = "false")
    val isLeadingDateTied: Boolean,

    @Schema(description = "최다 득표 날짜 상세 정보(동률 시 2개 이상)")
    val topDateVoteDetails: List<MeetingDateVoteDetailResponse>,

    @Schema(description = "최종 확정 날짜", example = "2026-02-20")
    val finalizedDate: LocalDate?,

    @Schema(description = "투표 완료 인원 수", example = "4")
    val completedVoteCount: Int,

    @Schema(description = "전체 투표 대상 인원 수", example = "6")
    val totalVoteCount: Int,
)

data class MeetingDateVoteDetailResponse(
    @Schema(description = "날짜", example = "2026-02-20")
    val date: LocalDate,

    @Schema(description = "해당 날짜 투표 인원 수", example = "3")
    val voteCount: Int,

    @Schema(description = "해당 날짜 투표자 이름 목록", example = "[\"홍길동\", \"김철수\"]")
    val voterNames: List<String>,
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
    isLeadingDateTied = this.isLeadingDateTied,
    topDateVoteDetails = this.topDateVoteDetails.map { it.toResponse() },
    finalizedDate = this.finalizedDate,
    completedVoteCount = this.completedVoteCount,
    totalVoteCount = this.totalVoteCount,
)

private fun MeetingDateVoteDetail.toResponse(): MeetingDateVoteDetailResponse = MeetingDateVoteDetailResponse(
    date = this.date,
    voteCount = this.voteCount,
    voterNames = this.voterNames,
)
