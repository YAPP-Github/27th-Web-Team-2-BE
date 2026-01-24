package com.nomoney.meeting.repository

import com.nomoney.meeting.entity.MeetingJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
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
}
