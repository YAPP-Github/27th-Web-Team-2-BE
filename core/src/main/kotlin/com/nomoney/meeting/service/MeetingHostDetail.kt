package com.nomoney.meeting.service

import com.nomoney.meeting.domain.Meeting

data class MeetingHostDetail(
    val meeting: Meeting,
    val notVotedParticipantCount: Int,
)
