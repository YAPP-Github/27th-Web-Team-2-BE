package com.nomoney.meeting.port

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId

interface MeetingRepository {
    fun findByMeetingId(meetingId: MeetingId): Meeting?
    fun save(meeting: Meeting): Meeting
}
