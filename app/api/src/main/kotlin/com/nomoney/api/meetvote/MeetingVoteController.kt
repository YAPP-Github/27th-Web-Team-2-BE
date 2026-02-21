package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.ConfirmedMeetingDashboardResponse
import com.nomoney.api.meetvote.model.CreateMeetingRequest
import com.nomoney.api.meetvote.model.CreateMeetingResponse
import com.nomoney.api.meetvote.model.FinalizeMeetingConflictCheckRequest
import com.nomoney.api.meetvote.model.FinalizeMeetingConflictCheckResponse
import com.nomoney.api.meetvote.model.FinalizeMeetingPreviewResponse
import com.nomoney.api.meetvote.model.FinalizeMeetingRequest
import com.nomoney.api.meetvote.model.FinalizeMeetingResponse
import com.nomoney.api.meetvote.model.InProgressMeetingDashboardResponse
import com.nomoney.api.meetvote.model.IsExistNameResponse
import com.nomoney.api.meetvote.model.MeetingInfoResponse
import com.nomoney.api.meetvote.model.MeetingSummaryResponse
import com.nomoney.api.meetvote.model.UpdateMeetingRequest
import com.nomoney.api.meetvote.model.UpdateMeetingResponse
import com.nomoney.api.meetvote.model.VoteRequest
import com.nomoney.api.meetvote.model.VoteResponse
import com.nomoney.api.meetvote.model.toConfirmedResponse
import com.nomoney.api.meetvote.model.toFinalizePreviewResponse
import com.nomoney.api.meetvote.model.toInProgressResponse
import com.nomoney.api.meetvote.model.toResponse
import com.nomoney.api.meetvote.model.toSummaryResponse
import com.nomoney.api.meetvote.model.toUpdateResponse
import com.nomoney.api.swagger.SwaggerApiOperation
import com.nomoney.api.swagger.SwaggerApiTag
import com.nomoney.auth.domain.User
import com.nomoney.exception.NotFoundException
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.service.MeetingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MeetingVoteController(
    private val meetingService: MeetingService,
) {

    @Operation(
        tags = [SwaggerApiTag.MEETING_QUERY_CREATE],
        summary = SwaggerApiOperation.MeetingVote.GET_MEETING_INFO_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.GET_MEETING_INFO_DESCRIPTION,
    )
    @GetMapping("/api/v1/meeting")
    fun getMeetingInfo(
        @Parameter(description = "모임 고유 ID", required = true, example = "aBcDeFgHiJ")
        @RequestParam
        meetId: String,
    ): MeetingInfoResponse {
        val meeting = meetingService.getMeetingInfoSortedByParticipantUpdatedAt(MeetingId(meetId))
        return meeting?.toResponse() ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: $meetId")
    }

    @Operation(
        tags = [SwaggerApiTag.MEETING_QUERY_CREATE],
        summary = SwaggerApiOperation.MeetingVote.GET_MEETING_LIST_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.GET_MEETING_LIST_DESCRIPTION,
    )
    @GetMapping("/api/v1/meeting/list")
    fun getMeetingList(): List<MeetingSummaryResponse> {
        return meetingService.getAllMeetings()
            .map { it.toSummaryResponse() }
    }

    @Operation(
        tags = [SwaggerApiTag.HOST_MEETING_MANAGEMENT],
        summary = SwaggerApiOperation.MeetingVote.GET_IN_PROGRESS_DASHBOARD_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.GET_IN_PROGRESS_DASHBOARD_DESCRIPTION,
    )
    @GetMapping("/api/v1/host/meeting/dashboard/in-progress")
    fun getInProgressMeetingDashboard(
        user: User,
    ): InProgressMeetingDashboardResponse {
        return meetingService.getHostMeetingDashboard(user.id).toInProgressResponse()
    }

    @Operation(
        tags = [SwaggerApiTag.HOST_MEETING_MANAGEMENT],
        summary = SwaggerApiOperation.MeetingVote.GET_CONFIRMED_DASHBOARD_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.GET_CONFIRMED_DASHBOARD_DESCRIPTION,
    )
    @GetMapping("/api/v1/host/meeting/dashboard/confirmed")
    fun getConfirmedMeetingDashboard(
        user: User,
    ): ConfirmedMeetingDashboardResponse {
        return meetingService.getHostMeetingDashboard(user.id).toConfirmedResponse()
    }

    @Operation(
        tags = [SwaggerApiTag.HOST_MEETING_MANAGEMENT],
        summary = SwaggerApiOperation.MeetingVote.GET_FINALIZE_PREVIEW_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.GET_FINALIZE_PREVIEW_DESCRIPTION,
    )
    @GetMapping("/api/v1/host/meeting/finalize/preview")
    fun getFinalizeMeetingPreview(
        user: User,
        @Parameter(description = "모임 고유 ID", required = true, example = "aBcDeFgHiJ")
        @RequestParam
        meetId: String,
    ): FinalizeMeetingPreviewResponse {
        return meetingService.getFinalizePreview(MeetingId(meetId), user.id).toFinalizePreviewResponse()
    }

    @Operation(
        tags = [SwaggerApiTag.MEETING_QUERY_CREATE],
        summary = SwaggerApiOperation.MeetingVote.CREATE_MEETING_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.CREATE_MEETING_DESCRIPTION,
    )
    @PostMapping("/api/v1/meeting")
    fun createMeeting(
        user: User,
        @RequestBody request: CreateMeetingRequest,
    ): CreateMeetingResponse {
        val meeting = meetingService.createMeeting(
            title = request.title,
            hostName = request.hostName,
            hostUserId = user.id,
            dates = request.dates.toSet(),
            maxParticipantCount = request.maxParticipantCount,
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

    @Operation(
        tags = [SwaggerApiTag.HOST_MEETING_MANAGEMENT],
        summary = SwaggerApiOperation.MeetingVote.UPDATE_MEETING_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.UPDATE_MEETING_DESCRIPTION,
    )
    @PutMapping("/api/v1/host/meeting")
    fun updateMeeting(
        user: User,
        @RequestBody request: UpdateMeetingRequest,
    ): UpdateMeetingResponse {
        val meeting = meetingService.updateMeeting(
            meetingId = request.meetingId,
            requesterUserId = user.id,
            title = request.title,
            dates = request.dates.toSet(),
            maxParticipantCount = request.maxParticipantCount,
            removedParticipantNames = request.removedParticipantNames.toSet(),
        )
        return meeting.toUpdateResponse()
    }

    @Operation(
        tags = [SwaggerApiTag.MEETING_QUERY_CREATE],
        summary = SwaggerApiOperation.MeetingVote.CHECK_DUPLICATE_NAME_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.CHECK_DUPLICATE_NAME_DESCRIPTION,
    )
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

    @Operation(
        tags = [SwaggerApiTag.VOTE],
        summary = SwaggerApiOperation.MeetingVote.CREATE_VOTE_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.CREATE_VOTE_DESCRIPTION,
    )
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

    @Operation(
        tags = [SwaggerApiTag.VOTE],
        summary = SwaggerApiOperation.MeetingVote.UPDATE_VOTE_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.UPDATE_VOTE_DESCRIPTION,
    )
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

    @Operation(
        tags = [SwaggerApiTag.HOST_MEETING_MANAGEMENT],
        summary = SwaggerApiOperation.MeetingVote.FINALIZE_MEETING_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.FINALIZE_MEETING_DESCRIPTION,
    )
    @PostMapping("/api/v1/host/meeting/finalize")
    fun finalizeMeeting(
        user: User,
        @RequestBody request: FinalizeMeetingRequest,
    ): FinalizeMeetingResponse {
        val meeting = meetingService.finalizeMeeting(
            meetingId = request.meetingId,
            selectedDate = request.finalizedDate,
            requesterUserId = user.id,
        )
        return FinalizeMeetingResponse(
            status = meeting.status,
            finalizedDate = requireNotNull(meeting.finalizedDate) {
                "CONFIRMED 상태의 모임에는 finalizedDate가 반드시 존재해야 합니다. meetingId=${meeting.id.value}"
            },
        )
    }

    @Operation(
        tags = [SwaggerApiTag.HOST_MEETING_MANAGEMENT],
        summary = SwaggerApiOperation.MeetingVote.CHECK_FINALIZED_DATE_CONFLICT_AND_FINALIZE_SUMMARY,
        description = SwaggerApiOperation.MeetingVote.CHECK_FINALIZED_DATE_CONFLICT_AND_FINALIZE_DESCRIPTION,
    )
    @PostMapping("/api/v1/host/meeting/finalize/check")
    fun checkFinalizedDateConflictAndFinalize(
        user: User,
        @RequestBody request: FinalizeMeetingConflictCheckRequest,
    ): FinalizeMeetingConflictCheckResponse {
        val isConflict = meetingService.checkFinalizedDateConflictAndFinalizeMeeting(
            meetingId = request.meetingId,
            finalizedDate = request.finalizedDate,
            requesterUserId = user.id,
        )
        return FinalizeMeetingConflictCheckResponse(isConflict = isConflict)
    }
}
