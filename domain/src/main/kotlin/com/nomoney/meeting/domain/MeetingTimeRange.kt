package com.nomoney.meeting.domain

import java.time.Duration
import java.time.LocalTime

data class MeetingTimeRange(
    val startTime: LocalTime,
    val endTime: LocalTime,
) {
    val slotCount: Int
        get() = Duration.between(startTime, endTime).toMinutes().toInt() / 30

    val startIndex: Int
        get() = startTime.hour * 2 + startTime.minute / 30

    init {
        require(endTime > startTime) { "endTime must be after startTime" }
        require(Duration.between(startTime, endTime).toMinutes() % 30 == 0L) {
            "Time range must be a multiple of 30 minutes"
        }
        require(startIndex + slotCount <= 48) {
            "Time range exceeds 48-slot bitmask capacity"
        }
    }
}
