package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "meetings")
class MeetingJpaEntity : BaseJpaEntity() {
    @Id
    @Column(name = "meet_id", length = 10, nullable = false)
    lateinit var meetId: String

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "host_name", nullable = true)
    var hostName: String? = null

    @OneToMany(mappedBy = "meeting", cascade = [CascadeType.ALL], orphanRemoval = true)
    var dates: MutableSet<MeetingDateJpaEntity> = mutableSetOf()

    @OneToMany(mappedBy = "meeting", cascade = [CascadeType.ALL], orphanRemoval = true)
    var participants: MutableSet<ParticipantJpaEntity> = mutableSetOf()

    companion object {
        fun of(
            meetId: String,
            title: String,
            hostName: String?,
            dates: MutableSet<MeetingDateJpaEntity> = mutableSetOf(),
            participants: MutableSet<ParticipantJpaEntity> = mutableSetOf(),
        ): MeetingJpaEntity {
            return MeetingJpaEntity().apply {
                this.meetId = meetId
                this.title = title
                this.hostName = hostName
                this.dates = dates
                this.participants = participants
            }
        }
    }
}
