package com.nomoney.meeting.service

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import java.time.LocalDate

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
    val finalizedDate: LocalDate?,
    val dDay: Int?,
    val completedVoteCount: Int,
    val totalVoteCount: Int,
    val voteProgressPercent: Int,
)
