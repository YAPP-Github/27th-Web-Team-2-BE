package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주최자 메모 저장 요청")
data class SaveMeetingMemoRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(description = "메모 내용", example = "회의실 예약 완료", required = true, maxLength = 200)
    val memo: String,
)

@Schema(description = "주최자 메모 저장 응답")
data class SaveMeetingMemoResponse(
    @Schema(description = "저장 성공 여부", example = "true")
    val success: Boolean,
)
