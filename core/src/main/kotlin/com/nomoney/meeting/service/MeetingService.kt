package com.nomoney.meeting.service

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import java.time.LocalDate
import org.springframework.stereotype.Service

@Service
class MeetingService {
    fun getMeetingInfo(meetingId: MeetingId): Meeting {
        return Meeting(
            meetingId,
            "Mock Meeting",
            HashSet(List(5) { index -> LocalDate.now().plusDays(index + 1L) }),
        )
    }
}
