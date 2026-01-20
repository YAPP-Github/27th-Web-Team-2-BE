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
import java.time.LocalDate
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
        val existing = meetingJpaRepository.findByMeetIdWithParticipants(meeting.id.value)

        val entity = if (existing != null) {
            existing.apply { this.updateFrom(meeting) }
        } else {
            meeting.toEntity()
        }

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
        val meetingEntity = MeetingJpaEntity.of(
            meetId = this.id.value,
            title = this.title,
        )

        meetingEntity.updateDates(this.dates)
        meetingEntity.updateParticipants(this.participants)

        return meetingEntity
    }

    private fun MeetingJpaEntity.updateFrom(meeting: Meeting) {
        this.title = meeting.title
        this.updateParticipants(meeting.participants)
    }

    private fun MeetingJpaEntity.updateDates(dates: Set<LocalDate>) {
        this.dates.removeIf { it.availableDate !in dates }
        val existingDates = this.dates.map { it.availableDate }.toSet()
        val datesToAdd = dates - existingDates
        datesToAdd.forEach { date ->
            this.dates.add(
                MeetingDateJpaEntity.of(
                    meeting = this,
                    availableDate = date,
                ),
            )
        }
    }

    private fun MeetingJpaEntity.updateParticipants(participants: List<Participant>) {
        val existingById = this.participants
            .mapNotNull { entity -> entity.participantId?.let { id -> id to entity } }
            .toMap()

        val incomingIds = participants.mapNotNull { participant ->
            participant.id.value.takeIf { it != 0L }
        }.toSet()

        this.participants.removeIf { entity ->
            entity.participantId != null && entity.participantId !in incomingIds
        }

        val currentIds = this.participants.mapNotNull { it.participantId }.toSet()

        participants.forEach { participant ->
            if (participant.id.value != 0L) {
                val participantEntity = existingById[participant.id.value]
                    ?: ParticipantJpaEntity.of(
                        participantId = participant.id.value,
                        meeting = this,
                        name = participant.name,
                    )

                participantEntity.name = participant.name
                updateVoteDates(participantEntity, participant.voteDates)

                if (participant.id.value !in currentIds) {
                    this.participants.add(participantEntity)
                }
            } else {
                val participantEntity = ParticipantJpaEntity.of(
                    participantId = null,
                    meeting = this,
                    name = participant.name,
                )
                updateVoteDates(participantEntity, participant.voteDates)
                this.participants.add(participantEntity)
            }
        }
    }

    private fun updateVoteDates(
        participantEntity: ParticipantJpaEntity,
        voteDates: Set<LocalDate>,
    ) {
        participantEntity.voteDates.removeIf { it.voteDate !in voteDates }
        val existingDates = participantEntity.voteDates.map { it.voteDate }.toSet()
        val datesToAdd = voteDates - existingDates
        datesToAdd.forEach { voteDate ->
            participantEntity.voteDates.add(
                ParticipantVoteDateJpaEntity.of(
                    participant = participantEntity,
                    voteDate = voteDate,
                ),
            )
        }
    }
}
