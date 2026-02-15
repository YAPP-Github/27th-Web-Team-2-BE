package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.CloseMeetingRequest
import com.nomoney.api.meetvote.model.CreateMeetingRequest
import com.nomoney.api.meetvote.model.FinalizeMeetingRequest
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingDashboard
import com.nomoney.meeting.service.MeetingDashboardCard
import com.nomoney.meeting.service.MeetingDashboardSummary
import com.nomoney.meeting.service.MeetingService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

        describe("POST /api/v1/meeting") {
            it("모임 생성 시 maxParticipantCount를 서비스로 전달한다") {
                val meetingId = MeetingId("created-meeting")
                val request = CreateMeetingRequest(
                    title = "신규 모임",
                    hostName = "주최자",
                    maxParticipantCount = 5,
                    dates = listOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                )
                every {
                    meetingService.createMeeting(
                        title = request.title,
                        hostName = request.hostName,
                        dates = request.dates.toSet(),
                        maxParticipantCount = request.maxParticipantCount,
                    )
                } returns fixtureMeeting(
                    id = meetingId,
                    status = MeetingStatus.VOTING,
                )
                every {
                    meetingService.addParticipant(
                        meetingId = meetingId,
                        name = request.hostName,
                        voteDates = emptySet(),
                        hasVoted = false,
                    )
                } returns fixtureMeeting(
                    id = meetingId,
                    status = MeetingStatus.VOTING,
                )

                val response = controller.createMeeting(request)

                response.id shouldBe meetingId
                verify(exactly = 1) {
                    meetingService.createMeeting(
                        title = request.title,
                        hostName = request.hostName,
                        dates = request.dates.toSet(),
                        maxParticipantCount = 5,
                    )
                }
            }
        }

        describe("GET /api/v1/meeting/dashboard") {
            it("주최자 이름으로 대시보드를 조회한다") {
                val hostName = "이파이"
                every { meetingService.getHostMeetingDashboard(hostName) } returns MeetingDashboard(
                    hostName = hostName,
                    summary = MeetingDashboardSummary(
                        votingCount = 1,
                        closedCount = 0,
                        confirmedCount = 1,
                    ),
                    inProgressMeetings = listOf(
                        MeetingDashboardCard(
                            meetingId = MeetingId("meeting-a"),
                            title = "진행중 모임",
                            status = MeetingStatus.VOTING,
                            leadingDate = LocalDate.of(2026, 2, 20),
                            isLeadingDateTied = false,
                            finalizedDate = null,
                            dDay = 5,
                            completedVoteCount = 2,
                            totalVoteCount = 4,
                            voteProgressPercent = 50,
                        ),
                    ),
                    confirmedMeetings = emptyList(),
                )

                val response = controller.getMeetingDashboard(hostName)

                response.hostName shouldBe hostName
                response.summary.votingCount shouldBe 1
                response.inProgressMeetings.size shouldBe 1
                response.inProgressMeetings.first().meetingId shouldBe MeetingId("meeting-a")
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
