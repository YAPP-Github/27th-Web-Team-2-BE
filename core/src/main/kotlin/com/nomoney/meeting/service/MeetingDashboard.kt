package com.nomoney.meeting.service

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import java.time.LocalDate

data class MeetingDateVoteDetail(
    val date: LocalDate,
    val voteCount: Int,
    val voterNames: List<String>,
)

data class MeetingDashboard(
    val hostName: String,
    val summary: MeetingDashboardSummary,
    val inProgressMeetings: List<MeetingDashboardCard>,
    val confirmedMeetings: List<MeetingDashboardCard>,
)

data class MeetingDashboardSummary(
    val votingCount: Int,
    val closedCount: Int,
    val confirmedCount: Int,
)

data class MeetingDashboardCard(
    val meetingId: MeetingId,
    val title: String,
    val status: MeetingStatus,
    val leadingDate: LocalDate?,
    val isLeadingDateTied: Boolean,
    val topDateVoteDetails: List<MeetingDateVoteDetail>,
    val finalizedDate: LocalDate?,
    val completedVoteCount: Int,
    val totalVoteCount: Int,
)
