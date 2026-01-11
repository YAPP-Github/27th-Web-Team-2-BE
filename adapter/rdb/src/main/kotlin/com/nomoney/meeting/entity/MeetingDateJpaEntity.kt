package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "meeting_dates")
@IdClass(MeetingDateId::class)
class MeetingDateJpaEntity : BaseJpaEntity() {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id", nullable = false)
    lateinit var meeting: MeetingJpaEntity

    @Id
    @Column(name = "available_date", nullable = false)
    lateinit var availableDate: LocalDate
}

data class MeetingDateId(
    val meeting: String = "",
    val availableDate: LocalDate = LocalDate.now(),
) : Serializable
