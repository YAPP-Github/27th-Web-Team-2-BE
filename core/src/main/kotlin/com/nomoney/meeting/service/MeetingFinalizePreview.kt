package com.nomoney.meeting.service

import com.nomoney.meeting.domain.MeetingId

data class MeetingFinalizePreview(
    val meetingId: MeetingId,
    val meetingTitle: String,
    val topDateVoteDetails: List<MeetingDateVoteDetail>,
    val requiresDateSelection: Boolean,
)
