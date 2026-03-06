package com.nomoney.meeting.port

import com.nomoney.auth.domain.UserId
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingSummary
import java.time.LocalDate

interface MeetingRepository {
    fun findByMeetingId(meetingId: MeetingId): Meeting?
    fun save(meeting: Meeting): Meeting
    fun findAll(): List<Meeting>
    fun findAllByHostUserId(hostUserId: UserId): List<Meeting>
    fun findAllMeetingSummaries(): List<MeetingSummary>
    fun existsConfirmedMeetingByHostUserIdAndFinalizedDate(
        hostUserId: UserId,
        meetingIdToExclude: MeetingId,
        finalizedDate: LocalDate,
    ): Boolean
    fun reassignHostUserId(fromUserId: UserId, toUserId: UserId)
}
