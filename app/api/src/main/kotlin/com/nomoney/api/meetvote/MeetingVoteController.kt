package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.CreateMeetingRequest
import com.nomoney.api.meetvote.model.CreateMeetingResponse
import com.nomoney.api.meetvote.model.MeetingInfoResponse
import com.nomoney.api.meetvote.model.toResponse
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.service.MeetingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "모임 투표", description = "모임 일정 투표 관련 API")
@RestController
class MeetingVoteController(
    private val meetingService: MeetingService,
) {

    @Operation(summary = "모임 정보 조회", description = "모임 ID로 모임 정보와 참여자들의 투표 현황을 조회합니다")
    @GetMapping("/api/v1/meeting")
    fun getMeetingInfo(
        @Parameter(description = "모임 고유 ID", required = true, example = "aBcDeFgHiJ")
        @RequestParam(value = "모임 고유 ID")
        meetId: String,
    ): MeetingInfoResponse {
        val meeting = meetingService.getMeetingInfo(MeetingId(meetId))
        return meeting.toResponse()
    }

    @Operation(summary = "모임 생성", description = "새로운 모임을 생성하고 고유 ID를 발급합니다")
    @PostMapping("/api/v1/meeting")
    fun createMeeting(
        @RequestBody request: CreateMeetingRequest,
    ): CreateMeetingResponse {
        return CreateMeetingResponse(
            id = meetingService.generateMeetId(),
        )
    }
}
