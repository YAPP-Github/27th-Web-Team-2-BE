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
            hostName = this.hostName,
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
            hasVoted = this.hasVoted,
        )
    }

    private fun Meeting.toEntity(): MeetingJpaEntity {
        val meetingEntity = MeetingJpaEntity.of(
            meetId = this.id.value,
            title = this.title,
            hostName = this.hostName,
        )

        meetingEntity.addMeetingDates(this.dates)
        meetingEntity.addParticipants(this.participants)

        return meetingEntity
    }

    private fun MeetingJpaEntity.addMeetingDates(
        incomingDates: Set<LocalDate>,
    ) {
        incomingDates.forEach { date ->
            this.dates.add(
                MeetingDateJpaEntity.of(
                    meeting = this,
                    availableDate = date,
                ),
            )
        }
    }

    private fun MeetingJpaEntity.addParticipants(
        incomingParticipants: List<Participant>,
    ) {
        incomingParticipants.forEach { participant ->
            this.participants.add(participant.toEntity(this))
        }
    }

    private fun Participant.toEntity(meeting: MeetingJpaEntity): ParticipantJpaEntity {
        val participantEntity = ParticipantJpaEntity.of(
            participantId = this.id.value.takeIf { it != 0L },
            meeting = meeting,
            name = this.name,
            hasVoted = this.hasVoted,
        )
        this.voteDates.forEach { voteDate ->
            participantEntity.voteDates.add(
                ParticipantVoteDateJpaEntity.of(
                    participant = participantEntity,
                    voteDate = voteDate,
                ),
            )
        }
        return participantEntity
    }

    private fun MeetingJpaEntity.updateFrom(meeting: Meeting) {
        this.title = meeting.title
        this.updateParticipants(meeting.participants)
    }

    private fun MeetingJpaEntity.updateParticipants(participants: List<Participant>) {
        val existingById = indexExistingParticipants()
        val incomingIds = participants
            .filterNot { it.isNew() }
            .map { it.id.value }
            .toSet()

        removeParticipantsNotIn(incomingIds)

        val remainingIds = this.participants.mapNotNull { it.participantId }.toSet()

        participants.forEach { participant ->
            val participantEntity = resolveParticipantEntity(participant, existingById)

            participantEntity.name = participant.name
            participantEntity.hasVoted = participant.hasVoted
            updateVoteDates(participantEntity, participant.voteDates)

            if (participant.isNew() || participant.id.value !in remainingIds) {
                this.participants.add(participantEntity)
            }
        }
    }

    private fun MeetingJpaEntity.indexExistingParticipants(): Map<Long, ParticipantJpaEntity> {
        return this.participants
            .mapNotNull { entity ->
                entity.participantId?.let { id -> id to entity }
            }
            .toMap()
    }

    private fun MeetingJpaEntity.removeParticipantsNotIn(incomingIds: Set<Long>) {
        this.participants.removeIf { entity ->
            entity.participantId != null && entity.participantId !in incomingIds
        }
    }

    private fun MeetingJpaEntity.resolveParticipantEntity(
        participant: Participant,
        existingById: Map<Long, ParticipantJpaEntity>,
    ): ParticipantJpaEntity {
        return if (participant.isNew()) {
            ParticipantJpaEntity.of(
                participantId = null,
                meeting = this,
                name = participant.name,
                hasVoted = participant.hasVoted,
            )
        } else {
            existingById[participant.id.value]
                ?: ParticipantJpaEntity.of(
                    participantId = participant.id.value,
                    meeting = this,
                    name = participant.name,
                    hasVoted = participant.hasVoted,
                )
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

    private fun Participant.isNew(): Boolean = this.id.value == 0L
}
