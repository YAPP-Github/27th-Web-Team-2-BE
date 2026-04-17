package com.nomoney.meeting.repository

import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.entity.MeetingDateJpaEntity
import com.nomoney.meeting.entity.MeetingJpaEntity
import com.nomoney.meeting.entity.ParticipantJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteDateJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteTimeSlotJpaEntity
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MeetingJpaRepository : JpaRepository<MeetingJpaEntity, String> {
    @Query(
        """
        SELECT DISTINCT m FROM MeetingJpaEntity m
        LEFT JOIN FETCH m.dates
        LEFT JOIN FETCH m.participants p
        LEFT JOIN FETCH p.voteDates
        LEFT JOIN FETCH p.voteTimeSlots
        WHERE m.meetId = :meetId
        """,
    )
    fun findByMeetIdWithParticipants(@Param("meetId") meetId: String): MeetingJpaEntity?

    fun findAllByHostUserId(hostUserId: Long): List<MeetingJpaEntity>

    @Query("SELECT d FROM MeetingDateJpaEntity d WHERE d.meeting.meetId IN :meetIds")
    fun findAllMeetingDatesByMeetIds(@Param("meetIds") meetIds: Collection<String>): List<MeetingDateJpaEntity>

    @Query("SELECT p FROM ParticipantJpaEntity p WHERE p.meeting.meetId IN :meetIds")
    fun findAllParticipantsByMeetIds(@Param("meetIds") meetIds: Collection<String>): List<ParticipantJpaEntity>

    @Query("SELECT v FROM ParticipantVoteDateJpaEntity v WHERE v.participant.participantId IN :participantIds")
    fun findAllVoteDatesByParticipantIds(@Param("participantIds") participantIds: Collection<Long>): List<ParticipantVoteDateJpaEntity>

    @Query("SELECT t FROM ParticipantVoteTimeSlotJpaEntity t WHERE t.participant.participantId IN :participantIds")
    fun findAllVoteTimeSlotsByParticipantIds(@Param("participantIds") participantIds: Collection<Long>): List<ParticipantVoteTimeSlotJpaEntity>

    @Query(
        """
        SELECT
            m.meetId as meetId,
            m.title as title,
            m.hostName as hostName,
            m.status as status,
            m.finalizedDate as finalizedDate
        FROM MeetingJpaEntity m
        """,
    )
    fun findAllMeetingSummaries(): List<MeetingSummaryProjection>

    fun existsByHostUserIdAndStatusAndMeetIdNotAndFinalizedDate(
        hostUserId: Long,
        status: MeetingStatus,
        meetId: String,
        finalizedDate: LocalDate,
    ): Boolean

    @Modifying
    @Query("UPDATE MeetingJpaEntity m SET m.hostUserId = :toUserId WHERE m.hostUserId = :fromUserId")
    fun updateHostUserId(
        @Param("fromUserId") fromUserId: Long,
        @Param("toUserId") toUserId: Long,
    )
}
