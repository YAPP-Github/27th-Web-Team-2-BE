package com.nomoney.meeting.repository

import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.entity.MeetingJpaEntity
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
        WHERE m.meetId = :meetId
        """,
    )
    fun findByMeetIdWithParticipants(@Param("meetId") meetId: String): MeetingJpaEntity?

    @Query(
        """
        SELECT DISTINCT m FROM MeetingJpaEntity m
        LEFT JOIN FETCH m.dates
        LEFT JOIN FETCH m.participants p
        LEFT JOIN FETCH p.voteDates
        WHERE m.hostUserId = :hostUserId
        """,
    )
    fun findAllByHostUserIdWithParticipants(@Param("hostUserId") hostUserId: Long): List<MeetingJpaEntity>

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
