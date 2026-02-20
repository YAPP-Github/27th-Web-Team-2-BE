package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingDateVoteDetail
import com.nomoney.meeting.service.MeetingFinalizePreview
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "모임 확정 후보 조회 응답")
data class FinalizeMeetingPreviewResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val meetingId: MeetingId,

    @Schema(description = "모임 제목", example = "팀 점심")
    val meetingTitle: String,

    @Schema(description = "최다 득표 날짜 상세 정보")
    val topDateVoteDetails: List<FinalizeMeetingDateVoteDetailResponse>,

    @Schema(description = "확정일 선택 필요 여부(동률 1위가 2개 이상인 경우 true)", example = "false")
    val requiresDateSelection: Boolean,
)

data class FinalizeMeetingDateVoteDetailResponse(
    @Schema(description = "날짜", example = "2026-02-20")
    val date: LocalDate,

    @Schema(description = "해당 날짜 투표 인원 수", example = "3")
    val voteCount: Int,

    @Schema(description = "해당 날짜 투표자 이름 목록", example = "[\"홍길동\", \"김철수\"]")
    val voterNames: List<String>,
)

@Schema(description = "모임 확정 요청")
data class FinalizeMeetingRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(
        description = "최종 확정 날짜 (공동 1위인 경우 필수)",
        example = "2026-02-20",
        required = false,
    )
    val finalizedDate: LocalDate? = null,
)

@Schema(description = "모임 확정 응답")
data class FinalizeMeetingResponse(
    @Schema(description = "변경된 모임 상태", example = "CONFIRMED")
    val status: MeetingStatus,

    @Schema(description = "최종 확정 날짜", example = "2026-02-20")
    val finalizedDate: LocalDate,
)

fun MeetingFinalizePreview.toFinalizePreviewResponse(): FinalizeMeetingPreviewResponse = FinalizeMeetingPreviewResponse(
    meetingId = this.meetingId,
    meetingTitle = this.meetingTitle,
    topDateVoteDetails = this.topDateVoteDetails.map { it.toResponse() },
    requiresDateSelection = this.requiresDateSelection,
)

private fun MeetingDateVoteDetail.toResponse(): FinalizeMeetingDateVoteDetailResponse = FinalizeMeetingDateVoteDetailResponse(
    date = this.date,
    voteCount = this.voteCount,
    voterNames = this.voterNames,
)
