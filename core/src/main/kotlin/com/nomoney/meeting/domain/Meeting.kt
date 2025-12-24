package com.nomoney.meeting.domain

import java.time.LocalDate

@JvmInline
value class MeetingId(val value: String)

data class Meeting(
    val id: MeetingId,
    val title: String,
    val dates: Set<LocalDate>,
    val maxParticipantCount: Int?,
    val participants: List<Participant>,
)
