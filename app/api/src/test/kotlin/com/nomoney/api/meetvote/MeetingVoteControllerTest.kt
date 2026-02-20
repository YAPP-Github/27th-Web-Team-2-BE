package com.nomoney.api.meetvote

import com.nomoney.api.meetvote.model.CreateMeetingRequest
import com.nomoney.api.meetvote.model.FinalizeMeetingRequest
import com.nomoney.api.meetvote.model.UpdateMeetingRequest
import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingDashboard
import com.nomoney.meeting.service.MeetingDashboardCard
import com.nomoney.meeting.service.MeetingDashboardSummary
import com.nomoney.meeting.service.MeetingDateVoteDetail
import com.nomoney.meeting.service.MeetingFinalizePreview
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
    val authenticatedUser = User(UserId(1L))

    beforeTest {
        clearMocks(meetingService)
    }

    describe("MeetingVoteController") {
        describe("POST /api/v1/host/meeting/finalize") {
            it("모임 확정 요청 시 CONFIRMED 상태와 확정 날짜를 반환한다") {
                val meetingId = MeetingId("test-meeting")
                val finalizedDate = LocalDate.of(2026, 2, 20)
                every {
                    meetingService.finalizeMeeting(meetingId, finalizedDate, authenticatedUser.id)
                } returns fixtureMeeting(
                    id = meetingId,
                    status = MeetingStatus.CONFIRMED,
                    finalizedDate = finalizedDate,
                )

                val response = controller.finalizeMeeting(
                    authenticatedUser,
                    FinalizeMeetingRequest(
                        meetingId = meetingId,
                        finalizedDate = finalizedDate,
                    ),
                )

                response.status shouldBe MeetingStatus.CONFIRMED
                response.finalizedDate shouldBe finalizedDate
            }
        }

        describe("GET /api/v1/host/meeting/finalize/preview") {
            it("모임 확정 후보 정보를 반환한다") {
                val meetingId = MeetingId("meeting-a")
                every {
                    meetingService.getFinalizePreview(meetingId, authenticatedUser.id)
                } returns MeetingFinalizePreview(
                    meetingId = meetingId,
                    meetingTitle = "모임A",
                    topDateVoteDetails = listOf(
                        MeetingDateVoteDetail(
                            date = LocalDate.of(2026, 2, 20),
                            voteCount = 2,
                            voterNames = listOf("A", "B"),
                        ),
                        MeetingDateVoteDetail(
                            date = LocalDate.of(2026, 2, 21),
                            voteCount = 2,
                            voterNames = listOf("A", "C"),
                        ),
                    ),
                    requiresDateSelection = true,
                )

                val response = controller.getFinalizeMeetingPreview(
                    user = authenticatedUser,
                    meetId = meetingId.value,
                )

                response.meetingId shouldBe meetingId
                response.meetingTitle shouldBe "모임A"
                response.requiresDateSelection shouldBe true
                response.topDateVoteDetails.size shouldBe 2
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
                        hostUserId = authenticatedUser.id,
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

                val response = controller.createMeeting(authenticatedUser, request)

                response.id shouldBe meetingId
                verify(exactly = 1) {
                    meetingService.createMeeting(
                        title = request.title,
                        hostName = request.hostName,
                        hostUserId = authenticatedUser.id,
                        dates = request.dates.toSet(),
                        maxParticipantCount = 5,
                    )
                }
            }
        }

        describe("GET /api/v1/host/meeting/dashboard/in-progress") {
            it("주최자 진행중 모임 대시보드를 조회한다") {
                val hostName = "이파이"
                every { meetingService.getHostMeetingDashboard(authenticatedUser.id) } returns MeetingDashboard(
                    hostName = hostName,
                    summary = MeetingDashboardSummary(
                        votingCount = 1,
                        confirmedCount = 1,
                    ),
                    inProgressMeetings = listOf(
                        MeetingDashboardCard(
                            meetingId = MeetingId("meeting-a"),
                            title = "진행중 모임",
                            status = MeetingStatus.VOTING,
                            leadingDate = LocalDate.of(2026, 2, 20),
                            isLeadingDateTied = false,
                            topDateVoteDetails = listOf(
                                MeetingDateVoteDetail(
                                    date = LocalDate.of(2026, 2, 20),
                                    voteCount = 2,
                                    voterNames = listOf("A", "B"),
                                ),
                            ),
                            finalizedDate = null,
                            completedVoteCount = 2,
                            totalVoteCount = 4,
                        ),
                    ),
                    confirmedMeetings = emptyList(),
                )

                val response = controller.getInProgressMeetingDashboard(authenticatedUser)

                response.hostName shouldBe hostName
                response.summary.votingCount shouldBe 1
                response.meetings.size shouldBe 1
                response.meetings.first().meetingId shouldBe MeetingId("meeting-a")
                response.meetings.first().topDateVoteDetails.single().voteCount shouldBe 2
            }
        }

        describe("GET /api/v1/host/meeting/dashboard/confirmed") {
            it("주최자 확정 모임 대시보드를 조회한다") {
                val hostName = "이파이"
                val finalizedDate = LocalDate.of(2026, 2, 25)
                every { meetingService.getHostMeetingDashboard(authenticatedUser.id) } returns MeetingDashboard(
                    hostName = hostName,
                    summary = MeetingDashboardSummary(
                        votingCount = 1,
                        confirmedCount = 1,
                    ),
                    inProgressMeetings = emptyList(),
                    confirmedMeetings = listOf(
                        MeetingDashboardCard(
                            meetingId = MeetingId("meeting-confirmed"),
                            title = "확정 모임",
                            status = MeetingStatus.CONFIRMED,
                            leadingDate = finalizedDate,
                            isLeadingDateTied = false,
                            topDateVoteDetails = listOf(
                                MeetingDateVoteDetail(
                                    date = finalizedDate,
                                    voteCount = 3,
                                    voterNames = listOf("A", "B", "C"),
                                ),
                            ),
                            finalizedDate = finalizedDate,
                            completedVoteCount = 3,
                            totalVoteCount = 3,
                        ),
                    ),
                )

                val response = controller.getConfirmedMeetingDashboard(authenticatedUser)

                response.hostName shouldBe hostName
                response.summary.confirmedCount shouldBe 1
                response.meetings.size shouldBe 1
                response.meetings.first().meetingId shouldBe MeetingId("meeting-confirmed")
                response.meetings.first().finalizedDate shouldBe finalizedDate
            }
        }

        describe("PUT /api/v1/host/meeting") {
            it("모임 수정 요청을 서비스로 위임하고 수정 결과를 반환한다") {
                val meetingId = MeetingId("meeting-to-update")
                val request = UpdateMeetingRequest(
                    meetingId = meetingId,
                    title = "수정된 모임",
                    maxParticipantCount = 4,
                    dates = listOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                    removedParticipantNames = listOf("삭제대상"),
                )
                every {
                    meetingService.updateMeeting(
                        meetingId = request.meetingId,
                        requesterUserId = authenticatedUser.id,
                        title = request.title,
                        dates = request.dates.toSet(),
                        maxParticipantCount = request.maxParticipantCount,
                        removedParticipantNames = request.removedParticipantNames.toSet(),
                    )
                } returns fixtureMeeting(
                    id = meetingId,
                    status = MeetingStatus.VOTING,
                ).copy(
                    title = "수정된 모임",
                    maxParticipantCount = 4,
                    dates = request.dates.toSet(),
                )

                val response = controller.updateMeeting(authenticatedUser, request)

                response.meetingId shouldBe meetingId
                response.title shouldBe "수정된 모임"
                response.maxParticipantCount shouldBe 4
                response.dates shouldBe request.dates
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
        hostUserId = UserId(1L),
        dates = setOf(LocalDate.of(2026, 2, 20)),
        maxParticipantCount = null,
        participants = emptyList(),
        status = status,
        finalizedDate = finalizedDate,
    )
}
