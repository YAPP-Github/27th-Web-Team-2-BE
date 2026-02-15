package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.CloseMeetingRequest
import com.nomoney.api.meetvote.model.CloseMeetingResponse
import com.nomoney.api.meetvote.model.CreateMeetingRequest
import com.nomoney.api.meetvote.model.CreateMeetingResponse
import com.nomoney.api.meetvote.model.FinalizeMeetingRequest
import com.nomoney.api.meetvote.model.FinalizeMeetingResponse
import com.nomoney.api.meetvote.model.IsExistNameResponse
import com.nomoney.api.meetvote.model.MeetingDashboardResponse
import com.nomoney.api.meetvote.model.MeetingInfoResponse
import com.nomoney.api.meetvote.model.MeetingSummaryResponse
import com.nomoney.api.meetvote.model.VoteRequest
import com.nomoney.api.meetvote.model.VoteResponse
import com.nomoney.api.meetvote.model.toResponse
import com.nomoney.api.meetvote.model.toSummaryResponse
import com.nomoney.exception.NotFoundException
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.service.MeetingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
        @RequestParam
        meetId: String,
    ): MeetingInfoResponse {
        val meeting = meetingService.getMeetingInfoSortedByParticipantUpdatedAt(MeetingId(meetId))
        return meeting?.toResponse() ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: $meetId")
    }

    @Operation(summary = "모임 목록 조회", description = "모든 모임의 ID, 제목, 주최자를 조회합니다")
    @GetMapping("/api/v1/meeting/list")
    fun getMeetingList(): List<MeetingSummaryResponse> {
        return meetingService.getAllMeetings()
            .map { it.toSummaryResponse() }
    }

    @Operation(summary = "주최자 대시보드 조회", description = "주최자 기준 진행중/확정 모임 목록과 요약 정보를 조회합니다.")
    @GetMapping("/api/v1/meeting/dashboard")
    fun getMeetingDashboard(
        @Parameter(description = "주최자 이름", required = true, example = "이파이")
        @RequestParam
        hostName: String,
    ): MeetingDashboardResponse {
        return meetingService.getHostMeetingDashboard(hostName).toResponse()
    }

    @Operation(summary = "모임 생성", description = "새로운 모임을 생성하고 고유 ID를 발급합니다")
    @PostMapping("/api/v1/meeting")
    fun createMeeting(
        @RequestBody request: CreateMeetingRequest,
    ): CreateMeetingResponse {
        val meeting = meetingService.createMeeting(
            title = request.title,
            hostName = request.hostName,
            dates = request.dates.toSet(),
            maxParticipantCount = null,
        )

        // 주최자를 빈 투표 날짜로 participant에 추가
        meetingService.addParticipant(
            meetingId = meeting.id,
            name = request.hostName,
            voteDates = emptySet(),
            hasVoted = false,
        )

        return CreateMeetingResponse(id = meeting.id)
    }

    @Operation(summary = "참여자 이름 중복 확인", description = "모임에 동일한 이름의 참여자가 있는지 확인합니다")
    @GetMapping("/api/v1/meeting/participant/exist")
    fun checkDuplicateName(
        @Parameter(description = "모임 고유 ID", required = true, example = "aBcDeFgHiJ")
        @RequestParam
        meetId: String,
        @Parameter(description = "확인할 참여자 이름", required = true, example = "이호연")
        @RequestParam
        name: String,
    ): IsExistNameResponse {
        val isExist = meetingService.existsVotedParticipantByName(MeetingId(meetId), name)
        return IsExistNameResponse(isExist = isExist)
    }

    @Operation(summary = "투표 생성", description = "모임에 대한 투표를 생성합니다. 중복된 이름으로 투표할 경우 에러가 발생합니다.")
    @PostMapping("/api/v1/meeting/vote")
    fun createVote(
        @RequestBody request: VoteRequest,
    ): VoteResponse {
        meetingService.submitVote(
            meetingId = request.meetingId,
            name = request.name,
            voteDates = request.voteDates.toSet(),
        )

        return VoteResponse(success = true)
    }

    @Operation(summary = "투표 수정", description = "기존 투표 내용을 수정합니다.")
    @PutMapping("/api/v1/meeting/vote")
    fun updateVote(
        @RequestBody request: VoteRequest,
    ): VoteResponse {
        meetingService.updateParticipant(
            meetingId = request.meetingId,
            name = request.name,
            voteDates = request.voteDates.toSet(),
        )

        return VoteResponse(success = true)
    }

    @Operation(summary = "모임 마감", description = "모임 상태를 투표중에서 마감 상태로 전환합니다.")
    @PostMapping("/api/v1/meeting/close")
    fun closeMeeting(
        @RequestBody request: CloseMeetingRequest,
    ): CloseMeetingResponse {
        val meeting = meetingService.closeMeeting(request.meetingId)
        return CloseMeetingResponse(status = meeting.status)
    }

    @Operation(
        summary = "모임 확정",
        description = "투표 결과를 바탕으로 최종 날짜를 확정하고 모임 상태를 확정으로 전환합니다. 공동 1위인 경우 finalizedDate를 함께 요청해야 합니다.",
    )
    @PostMapping("/api/v1/meeting/finalize")
    fun finalizeMeeting(
        @RequestBody request: FinalizeMeetingRequest,
    ): FinalizeMeetingResponse {
        val meeting = meetingService.finalizeMeeting(request.meetingId, request.finalizedDate)
        return FinalizeMeetingResponse(
            status = meeting.status,
            finalizedDate = requireNotNull(meeting.finalizedDate) {
                "CONFIRMED 상태의 모임에는 finalizedDate가 반드시 존재해야 합니다. meetingId=${meeting.id.value}"
            },
        )
    }
}
