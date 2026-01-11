package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "participant_vote_dates")
@IdClass(ParticipantVoteDateId::class)
class ParticipantVoteDateJpaEntity : BaseJpaEntity() {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    lateinit var participant: ParticipantJpaEntity

    @Id
    @Column(name = "vote_date", nullable = false)
    lateinit var voteDate: LocalDate
}

data class ParticipantVoteDateId(
    val participant: Long = 0L,
    val voteDate: LocalDate = LocalDate.now(),
) : Serializable
