package com.nomoney.meeting.service

import com.nomoney.exception.DuplicateContentException
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
        hostName: String?,
        dates: Set<LocalDate>,
        maxParticipantCount: Int? = null,
    ): Meeting {
        val meetingId = generateMeetId()
        val meeting = Meeting(
            id = meetingId,
            title = title,
            hostName = hostName,
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
        hasVoted: Boolean,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        val newParticipant = Participant(
            id = ParticipantId(0L),
            name = name,
            voteDates = voteDates,
            hasVoted = hasVoted,
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
                participant.copy(
                    voteDates = voteDates,
                    hasVoted = true,
                )
            } else {
                participant
            }
        }

        val updatedMeeting = meeting.copy(participants = updatedParticipants)

        return meetingRepository.save(updatedMeeting)
    }

    fun submitVote(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        val participant = meeting.participants.firstOrNull { it.name == name }
        return when {
            participant == null -> {
                addParticipant(
                    meetingId = meetingId,
                    name = name,
                    voteDates = voteDates,
                    hasVoted = true,
                )
            }
            participant.hasVoted -> {
                throw DuplicateContentException("이미 투표를 완료한 참여자입니다.", "name: $name")
            }
            else -> {
                require(meeting.hostName == name) { "주최자 Participant는 반드시 meeting.hostName과 동일한 name을 가져야 한다.: $name" }
                updateParticipant(
                    meetingId = meetingId,
                    name = name,
                    voteDates = voteDates,
                )
            }
        }
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
