package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "participant_vote_dates")
class ParticipantVoteDateJpaEntity : BaseJpaEntity() {
    @EmbeddedId
    lateinit var id: ParticipantVoteDateId

    @MapsId("participantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    lateinit var participant: ParticipantJpaEntity

    @get:Transient
    val voteDate: LocalDate
        get() = id.voteDate

    companion object {
        fun of(participant: ParticipantJpaEntity, voteDate: LocalDate): ParticipantVoteDateJpaEntity {
            return ParticipantVoteDateJpaEntity().apply {
                this.id = ParticipantVoteDateId(
                    participantId = 0L,
                    voteDate = voteDate,
                )
                this.participant = participant
            }
        }
    }
}

@Embeddable
data class ParticipantVoteDateId(
    @Column(name = "participant_id")
    var participantId: Long = 0L,

    @Column(name = "vote_date")
    var voteDate: LocalDate = LocalDate.now(),
) : Serializable
