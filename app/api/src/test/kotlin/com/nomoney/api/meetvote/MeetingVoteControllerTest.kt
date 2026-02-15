package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.CloseMeetingRequest
import com.nomoney.api.meetvote.model.FinalizeMeetingRequest
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class MeetingVoteControllerTest : DescribeSpec({

    val meetingService = mockk<MeetingService>()
    val controller = MeetingVoteController(meetingService)

    beforeTest {
        clearMocks(meetingService)
    }

    describe("MeetingVoteController") {

        describe("POST /api/v1/meeting/close") {
            it("모임 마감 요청 시 CLOSED 상태를 반환한다") {
                val meetingId = MeetingId("test-meeting")
                every { meetingService.closeMeeting(meetingId) } returns fixtureMeeting(
                    id = meetingId,
                    status = MeetingStatus.CLOSED,
                )

                val response = controller.closeMeeting(CloseMeetingRequest(meetingId = meetingId))

                response.status shouldBe MeetingStatus.CLOSED
            }
        }

        describe("POST /api/v1/meeting/finalize") {
            it("모임 확정 요청 시 CONFIRMED 상태와 확정 날짜를 반환한다") {
                val meetingId = MeetingId("test-meeting")
                val finalizedDate = LocalDate.of(2026, 2, 20)
                every { meetingService.finalizeMeeting(meetingId, finalizedDate) } returns fixtureMeeting(
                    id = meetingId,
                    status = MeetingStatus.CONFIRMED,
                    finalizedDate = finalizedDate,
                )

                val response = controller.finalizeMeeting(
                    FinalizeMeetingRequest(
                        meetingId = meetingId,
                        finalizedDate = finalizedDate,
                    ),
                )

                response.status shouldBe MeetingStatus.CONFIRMED
                response.finalizedDate shouldBe finalizedDate
            }
        }
    }
},)

private fun fixtureMeeting(
    id: MeetingId,
    status: MeetingStatus,
    finalizedDate: LocalDate? = null,
): Meeting {
    return Meeting(
        id = id,
        title = "테스트 모임",
        hostName = "주최자",
        dates = setOf(LocalDate.of(2026, 2, 20)),
        maxParticipantCount = null,
        participants = emptyList(),
        status = status,
        finalizedDate = finalizedDate,
    )
}
