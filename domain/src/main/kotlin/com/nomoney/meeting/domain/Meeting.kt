package com.nomoney.meeting.domain

import com.nomoney.auth.domain.UserId
import java.time.LocalDate

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
    val hostName: String?, // 우선 nullable로 유지 -> 변경 예정
    val hostUserId: UserId? = null,
    val dates: Set<LocalDate>,
    val maxParticipantCount: Int?,
    val participants: List<Participant>,
    val memo: String? = null,
    val status: MeetingStatus = MeetingStatus.VOTING,
    val finalizedDate: LocalDate? = null,
) {
    fun isVoteDatesAllowed(voteDates: Set<LocalDate>): Boolean {
        return (voteDates - dates).isEmpty()
    }
}
