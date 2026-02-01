package com.nomoney.meeting.domain

import java.time.LocalDate

@JvmInline
value class MeetingId(val value: String)

data class Meeting(
    val id: MeetingId,
    val title: String,
    val hostName: String?, // 우선 nullable로 유지 -> 변경 예정
    val dates: Set<LocalDate>,
    val maxParticipantCount: Int?,
    val participants: List<Participant>,
) {
    fun isVoteDatesAllowed(voteDates: Set<LocalDate>): Boolean {
        return (voteDates - dates).isEmpty()
    }
}
