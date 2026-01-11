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

    companion object {
        fun of(participant: ParticipantJpaEntity, voteDate: LocalDate): ParticipantVoteDateJpaEntity {
            return ParticipantVoteDateJpaEntity().apply {
                this.participant = participant
                this.voteDate = voteDate
            }
        }
    }
}

data class ParticipantVoteDateId(
    val participant: Long = 0L,
    val voteDate: LocalDate = LocalDate.now(),
) : Serializable
