package com.nomoney.meeting.service

import com.nomoney.exception.NotFoundException
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.port.MeetingRepository
import java.security.SecureRandom
import java.time.LocalDate
import org.springframework.stereotype.Service

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
) {
    private val random = SecureRandom()

    fun createMeeting(
        title: String,
        dates: Set<LocalDate>,
        maxParticipantCount: Int? = null,
    ): Meeting {
        val meetingId = generateMeetId()
        val meeting = Meeting(
            id = meetingId,
            title = title,
            dates = dates,
            maxParticipantCount = maxParticipantCount,
            participants = emptyList(),
        )
        return meetingRepository.save(meeting)
    }

    fun getMeetingInfo(meetingId: MeetingId): Meeting? {
        return meetingRepository.findByMeetingId(meetingId)
    }

    fun addParticipant(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        val newParticipant = Participant(
            id = ParticipantId(0L),
            name = name,
            voteDates = voteDates,
        )

        val updatedMeeting = meeting.copy(
            participants = meeting.participants + newParticipant,
        )

        return meetingRepository.save(updatedMeeting)
    }

    fun updateParticipant(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        val existingParticipant = meeting.participants.find { it.name == name }
            ?: throw NotFoundException("참여자를 찾을 수 없습니다.", "name: $name")

        val updatedParticipants = meeting.participants.map { participant ->
            if (participant.name == name) {
                participant.copy(voteDates = voteDates)
            } else {
                participant
            }
        }

        val updatedMeeting = meeting.copy(participants = updatedParticipants)

        return meetingRepository.save(updatedMeeting)
    }

    fun isExistName(meetingId: MeetingId, name: String): Boolean {
        val meeting = getMeetingInfo(meetingId) ?: return false
        return meeting.participants.any { it.name == name }
    }

    fun generateMeetId(): MeetingId {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val meetId = (1..12)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")

        return MeetingId(meetId)
    }
}
