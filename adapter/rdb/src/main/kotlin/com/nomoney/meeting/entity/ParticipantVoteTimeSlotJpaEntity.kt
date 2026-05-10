package com.nomoney.meeting.entity

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
@Table(name = "participant_vote_time_slots")
class ParticipantVoteTimeSlotJpaEntity {
    @EmbeddedId
    lateinit var id: ParticipantVoteTimeSlotId

    @MapsId("participantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    lateinit var participant: ParticipantJpaEntity

    @Column(name = "time_slots", length = 48, nullable = false)
    lateinit var timeSlots: String

    @get:Transient
    val voteDate: LocalDate
        get() = id.voteDate

    companion object {
        fun of(
            participant: ParticipantJpaEntity,
            voteDate: LocalDate,
            timeSlots: String,
        ): ParticipantVoteTimeSlotJpaEntity {
            return ParticipantVoteTimeSlotJpaEntity().apply {
                this.id = ParticipantVoteTimeSlotId(participantId = 0L, voteDate = voteDate)
                this.participant = participant
                this.timeSlots = timeSlots
            }
        }
    }
}

@Embeddable
data class ParticipantVoteTimeSlotId(
    @Column(name = "participant_id")
    var participantId: Long = 0L,

    @Column(name = "vote_date")
    var voteDate: LocalDate = LocalDate.now(),
) : Serializable
