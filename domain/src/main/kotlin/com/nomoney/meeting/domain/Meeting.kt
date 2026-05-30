package com.nomoney.meeting.domain

import com.nomoney.auth.domain.UserId
import java.time.LocalDate
import java.time.LocalTime

@JvmInline
value class MeetingId(val value: String)

enum class MeetingStatus {
    VOTING,
    CLOSED,
    CONFIRMED,
}

data class Meeting(
    val id: MeetingId,
    val title: String,
    val hostName: String?,
    val hostUserId: UserId? = null,
    val dates: Set<LocalDate>,
    val maxParticipantCount: Int?,
    val participants: List<Participant>,
    val memo: String? = null,
    val status: MeetingStatus = MeetingStatus.VOTING,
    val finalizedDate: LocalDate? = null,
    val timeRange: MeetingTimeRange? = null,
    val finalizedStartTime: LocalTime? = null,
    val finalizedEndTime: LocalTime? = null,
) {
    fun isVoteDatesAllowed(voteDates: Set<LocalDate>): Boolean {
        return (voteDates - dates).isEmpty()
    }
}
