package com.nomoney.meeting.adapter

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.entity.MeetingDateJpaEntity
import com.nomoney.meeting.entity.MeetingJpaEntity
import com.nomoney.meeting.entity.ParticipantJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteDateJpaEntity
import com.nomoney.meeting.port.MeetingRepository
import com.nomoney.meeting.repository.MeetingJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MeetingAdapter(
    private val meetingJpaRepository: MeetingJpaRepository,
) : MeetingRepository {

    @Transactional(readOnly = true)
    override fun findByMeetingId(meetingId: MeetingId): Meeting? {
        val entity = meetingJpaRepository.findByMeetIdWithParticipants(meetingId.value)
            ?: return null

        return entity.toDomain()
    }

    @Transactional
    override fun save(meeting: Meeting): Meeting {
        val entity = meeting.toEntity()
        val savedEntity = meetingJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    private fun MeetingJpaEntity.toDomain(): Meeting {
        return Meeting(
            id = MeetingId(this.meetId),
            title = this.title,
            dates = this.dates.map { it.availableDate }.toSet(),
            maxParticipantCount = null,
            participants = this.participants.map { it.toDomain() },
        )
    }

    private fun ParticipantJpaEntity.toDomain(): Participant {
        return Participant(
            id = ParticipantId(this.participantId ?: 0L),
            name = this.name,
            voteDates = this.voteDates.map { it.voteDate }.toSet(),
        )
    }

    private fun Meeting.toEntity(): MeetingJpaEntity {
        val meetingEntity = MeetingJpaEntity().apply {
            meetId = this@toEntity.id.value
            title = this@toEntity.title
        }

        val dateEntities = this.dates.map { date ->
            MeetingDateJpaEntity().apply {
                meeting = meetingEntity
                availableDate = date
            }
        }.toMutableSet()

        val participantEntities = this.participants.map { participant ->
            val participantEntity = ParticipantJpaEntity().apply {
                participantId = participant.id.value.takeIf { it != 0L }
                meeting = meetingEntity
                name = participant.name
            }

            val voteDateEntities = participant.voteDates.map { voteDate ->
                ParticipantVoteDateJpaEntity().apply {
                    this.participant = participantEntity
                    this.voteDate = voteDate
                }
            }.toMutableSet()

            participantEntity.apply {
                voteDates = voteDateEntities
            }
        }.toMutableSet()

        meetingEntity.apply {
            dates = dateEntities
            participants = participantEntities
        }

        return meetingEntity
    }
}
