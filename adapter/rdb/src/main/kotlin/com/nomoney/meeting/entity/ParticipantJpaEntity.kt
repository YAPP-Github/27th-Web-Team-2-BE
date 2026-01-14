package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "participants")
class ParticipantJpaEntity : BaseJpaEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id", nullable = false)
    var participantId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id", nullable = false)
    lateinit var meeting: MeetingJpaEntity

    @Column(name = "name", length = 100, nullable = false)
    lateinit var name: String

    @OneToMany(mappedBy = "participant", cascade = [CascadeType.ALL], orphanRemoval = true)
    var voteDates: MutableSet<ParticipantVoteDateJpaEntity> = mutableSetOf()

    companion object {
        fun of(
            participantId: Long?,
            meeting: MeetingJpaEntity,
            name: String,
            voteDates: MutableSet<ParticipantVoteDateJpaEntity> = mutableSetOf(),
        ): ParticipantJpaEntity {
            return ParticipantJpaEntity().apply {
                this.participantId = participantId
                this.meeting = meeting
                this.name = name
                this.voteDates = voteDates
            }
        }
    }
}
