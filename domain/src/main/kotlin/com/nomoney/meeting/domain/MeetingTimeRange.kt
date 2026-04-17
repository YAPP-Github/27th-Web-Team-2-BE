package com.nomoney.meeting.domain

import java.time.LocalTime

data class MeetingTimeRange(
    val startTime: LocalTime,
    val endTime: LocalTime,
) {
    val slotCount: Int
        get() = ((endTime.hour * 60 + endTime.minute) - (startTime.hour * 60 + startTime.minute)) / 30

    val startIndex: Int
        get() = startTime.hour * 2 + startTime.minute / 30
}
