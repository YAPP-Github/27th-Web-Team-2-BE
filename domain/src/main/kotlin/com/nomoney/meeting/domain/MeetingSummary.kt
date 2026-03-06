package com.nomoney.meeting.domain

import java.time.LocalDate

data class MeetingSummary(
    val id: MeetingId,
    val title: String,
    val hostName: String?,
    val status: MeetingStatus,
    val finalizedDate: LocalDate?,
)
