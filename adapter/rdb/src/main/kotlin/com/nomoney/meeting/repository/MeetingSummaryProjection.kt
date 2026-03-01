package com.nomoney.meeting.repository

import com.nomoney.meeting.domain.MeetingStatus
import java.time.LocalDate

interface MeetingSummaryProjection {
    val meetId: String
    val title: String
    val hostName: String?
    val status: MeetingStatus
    val finalizedDate: LocalDate?
}
