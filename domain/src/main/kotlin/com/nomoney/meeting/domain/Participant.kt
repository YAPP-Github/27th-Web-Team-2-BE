package com.nomoney.meeting.domain

import java.time.LocalDate
import java.time.LocalDateTime

@JvmInline
value class ParticipantId(val value: Long)

data class Participant(
    val id: ParticipantId,
    val name: String,
    val voteDates: Set<LocalDate>,
    val hasVoted: Boolean,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
