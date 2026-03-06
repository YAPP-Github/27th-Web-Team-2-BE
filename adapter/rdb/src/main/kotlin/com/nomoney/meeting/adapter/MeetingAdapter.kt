package com.nomoney.meeting.adapter

import com.nomoney.auth.domain.UserId
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.domain.MeetingSummary
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.entity.MeetingDateJpaEntity
import com.nomoney.meeting.entity.MeetingJpaEntity
import com.nomoney.meeting.entity.ParticipantJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteDateJpaEntity
import com.nomoney.meeting.port.MeetingRepository
import com.nomoney.meeting.repository.MeetingJpaRepository
import com.nomoney.meeting.repository.MeetingSummaryProjection
import java.time.LocalDate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MeetingAdapter(
    private val meetingJpaRepository: MeetingJpaRepository,
) : MeetingRepository {

    @Transactional(readOnly = true)
    override fun findByMeetingId(meetingId: MeetingId): Meeting? {
        val meeting = meetingJpaRepository.findById(meetingId.value).orElse(null)
            ?: return null
        return mapMeetingsToDomain(listOf(meeting)).firstOrNull()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Meeting> {
        val meetings = meetingJpaRepository.findAll()
        return mapMeetingsToDomain(meetings)
    }

    @Transactional(readOnly = true)
    override fun findAllByHostUserId(hostUserId: UserId): List<Meeting> {
        val meetings = meetingJpaRepository.findAllByHostUserId(hostUserId.value)
        return mapMeetingsToDomain(meetings)
    }

    @Transactional(readOnly = true)
    override fun findAllMeetingSummaries(): List<MeetingSummary> {
        return meetingJpaRepository.findAllMeetingSummaries()
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun existsConfirmedMeetingByHostUserIdAndFinalizedDate(
        hostUserId: UserId,
        meetingIdToExclude: MeetingId,
        finalizedDate: LocalDate,
    ): Boolean {
        return meetingJpaRepository.existsByHostUserIdAndStatusAndMeetIdNotAndFinalizedDate(
            hostUserId = hostUserId.value,
            status = MeetingStatus.CONFIRMED,
            meetId = meetingIdToExclude.value,
            finalizedDate = finalizedDate,
        )
    }

    @Transactional
    override fun reassignHostUserId(fromUserId: UserId, toUserId: UserId) {
        meetingJpaRepository.updateHostUserId(fromUserId.value, toUserId.value)
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
        return toDomain(
            dates = this.dates.map { it.availableDate }.toSet(),
            participants = this.participants.map { it.toDomain() },
        )
    }

    private fun MeetingJpaEntity.toDomain(
        dates: Set<LocalDate>,
        participants: List<Participant>,
    ): Meeting {
        return Meeting(
            id = MeetingId(this.meetId),
            title = this.title,
            hostName = this.hostName,
            hostUserId = this.hostUserId?.let(::UserId),
            dates = dates,
            maxParticipantCount = this.maxParticipantCount,
            participants = participants,
            memo = this.memo,
            status = this.status,
            finalizedDate = this.finalizedDate,
        )
    }

    private fun ParticipantJpaEntity.toDomain(): Participant {
        return toDomain(
            voteDates = this.voteDates.map { it.voteDate }.toSet(),
        )
    }

    private fun ParticipantJpaEntity.toDomain(voteDates: Set<LocalDate>): Participant {
        return Participant(
            id = ParticipantId(this.participantId),
            name = this.name,
            voteDates = voteDates,
            hasVoted = this.hasVoted,
            updatedAt = this.updatedAt,
        )
    }

    private fun findVoteDatesByParticipantIds(
        participants: List<ParticipantJpaEntity>,
    ): Map<Long, Set<LocalDate>> {
        val participantIds = participants.map { it.participantId }
        if (participantIds.isEmpty()) {
            return emptyMap()
        }

        return meetingJpaRepository.findAllVoteDatesByParticipantIds(participantIds)
            .groupBy { it.participant.participantId }
            .mapValues { (_, entities) -> entities.map { it.voteDate }.toSet() }
    }

    private fun mapMeetingsToDomain(meetings: List<MeetingJpaEntity>): List<Meeting> {
        if (meetings.isEmpty()) {
            return emptyList()
        }

        val meetIds = meetings.map { it.meetId }
        val datesByMeetId = meetingJpaRepository.findAllMeetingDatesByMeetIds(meetIds)
            .groupBy { it.meeting.meetId }
            .mapValues { (_, entities) -> entities.map { it.availableDate }.toSet() }

        val participants = meetingJpaRepository.findAllParticipantsByMeetIds(meetIds)
        val voteDatesByParticipantId = findVoteDatesByParticipantIds(participants)

        val participantsByMeetId = participants
            .groupBy { it.meeting.meetId }
            .mapValues { (_, entities) ->
                entities.map { participant ->
                    participant.toDomain(
                        voteDates = voteDatesByParticipantId[participant.participantId].orEmpty(),
                    )
                }
            }

        return meetings.map { meeting ->
            meeting.toDomain(
                dates = datesByMeetId[meeting.meetId].orEmpty(),
                participants = participantsByMeetId[meeting.meetId].orEmpty(),
            )
        }
    }

    private fun MeetingSummaryProjection.toDomain(): MeetingSummary {
        return MeetingSummary(
            id = MeetingId(this.meetId),
            title = this.title,
            hostName = this.hostName,
            status = this.status,
            finalizedDate = this.finalizedDate,
        )
    }

    private fun Meeting.toEntity(): MeetingJpaEntity {
        val meetingEntity = MeetingJpaEntity.of(
            meetId = this.id.value,
            title = this.title,
            hostName = this.hostName,
            hostUserId = this.hostUserId?.value,
            maxParticipantCount = this.maxParticipantCount,
            memo = this.memo,
            status = this.status,
            finalizedDate = this.finalizedDate,
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
            participantId = this.id.value,
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
        this.hostUserId = meeting.hostUserId?.value
        this.maxParticipantCount = meeting.maxParticipantCount
        this.memo = meeting.memo
        this.status = meeting.status
        this.finalizedDate = meeting.finalizedDate
        this.updateMeetingDates(meeting.dates)
        this.updateParticipants(meeting.participants)
    }

    private fun MeetingJpaEntity.updateMeetingDates(dates: Set<LocalDate>) {
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
        val existingById = indexExistingParticipants()
        val incomingIds = participants
            .filterNot { it.isNew() }
            .map { it.id.value }
            .toSet()

        removeParticipantsNotIn(incomingIds)

        val remainingIds = this.participants.map { it.participantId }.toSet()

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
            .filter { it.participantId != 0L }
            .associateBy { it.participantId }
    }

    private fun MeetingJpaEntity.removeParticipantsNotIn(incomingIds: Set<Long>) {
        this.participants.removeIf { entity ->
            entity.participantId != 0L && entity.participantId !in incomingIds
        }
    }

    private fun MeetingJpaEntity.resolveParticipantEntity(
        participant: Participant,
        existingById: Map<Long, ParticipantJpaEntity>,
    ): ParticipantJpaEntity {
        return if (participant.isNew()) {
            ParticipantJpaEntity.of(
                participantId = 0L,
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
