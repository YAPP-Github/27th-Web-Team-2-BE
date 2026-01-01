package com.nomoney.meeting.service

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import org.springframework.stereotype.Service

@Service
class MeetingService {
    fun getMeetingInfo(meetingId: MeetingId): Meeting {
        return Meeting(
            meetingId,
            "Mock Meeting",
            HashSet(List(5) { index -> LocalDate.now().plusDays(index + 1L) }),
            null,
            listOf(
                Participant(
                    ParticipantId(1),
                    "이호연",
                    HashSet(List(2) { index -> LocalDate.now().plusDays(index + 1L) }),
                ),
                Participant(
                    ParticipantId(2),
                    "박상민",
                    HashSet(List(3) { index -> LocalDate.now().plusDays(index * 2 + 1L) }),
                ),
            ),
        )
    }

    fun generateMeetId(): MeetingId {
        val random = SecureRandom()
        val bytes = ByteArray(8)
        random.nextBytes(bytes)

        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val meetId = encoded.take(10)

        return MeetingId(meetId)
    }
}
