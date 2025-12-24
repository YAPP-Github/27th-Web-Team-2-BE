package com.nomoney.meeting.domain

import java.time.LocalDate

@JvmInline
value class ParticipantId(val value: Long)

data class Participant(
    val id: ParticipantId,
    val name: String,
    val voteDates: Set<LocalDate>,
)
