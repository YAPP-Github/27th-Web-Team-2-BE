package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.MeetingInfoResponse
import com.nomoney.api.meetvote.model.toResponse
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.service.MeetingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MeetingVoteController(
    private val meetingService: MeetingService,
) {

    @GetMapping("/api/v1/meeting")
    fun getMeetingInfo(
        @RequestParam(value = "모임 고유 ID") meetId: String,
    ): MeetingInfoResponse {
        val meeting = meetingService.getMeetingInfo(MeetingId(meetId))
        return meeting.toResponse()
    }
}
