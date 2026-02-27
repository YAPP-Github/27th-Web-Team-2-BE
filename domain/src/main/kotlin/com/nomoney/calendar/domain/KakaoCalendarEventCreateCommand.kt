package com.nomoney.calendar.domain

import java.time.LocalDate

data class KakaoCalendarEventCreateCommand(
    val title: String,
    val description: String?,
    val location: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
