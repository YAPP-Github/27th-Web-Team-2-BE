package com.nomoney.meeting.service

import com.nomoney.auth.domain.UserId
import com.nomoney.exception.InvalidRequestException
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.port.MeetingRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class MeetingServiceTest : DescribeSpec({

    val meetingRepository = mockk<MeetingRepository>()
    val meetingService = MeetingService(meetingRepository)

    beforeTest {
        clearMocks(meetingRepository)
    }

    describe("MeetingService") {
        describe("createMeeting") {
            it("maxParticipantCount가 1 미만이면 예외가 발생한다") {
                shouldThrow<InvalidRequestException> {
                    meetingService.createMeeting(
                        title = "테스트 모임",
                        hostName = "주최자",
                        hostUserId = UserId(1L),
                        dates = setOf(LocalDate.of(2026, 2, 20)),
                        maxParticipantCount = 0,
                    )
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }
        }
        describe("finalizeMeeting") {
            it("단일 최다 득표 날짜가 있으면 finalizedDate 없이 확정된다") {
                val meeting = fixtureMeeting(
                    status = MeetingStatus.VOTING,
                    dates = setOf(
                        LocalDate.of(2026, 2, 20),
                        LocalDate.of(2026, 2, 21),
                    ),
                    participants = listOf(
                        fixtureParticipant(
                            id = 1L,
                            name = "A",
                            voteDates = setOf(LocalDate.of(2026, 2, 20)),
                        ),
                        fixtureParticipant(
                            id = 2L,
                            name = "B",
                            voteDates = setOf(LocalDate.of(2026, 2, 20)),
                        ),
                        fixtureParticipant(
                            id = 3L,
                            name = "C",
                            voteDates = setOf(LocalDate.of(2026, 2, 21)),
                        ),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting
                every { meetingRepository.save(any()) } answers { firstArg() }

                val result = meetingService.finalizeMeeting(meeting.id, null, UserId(1L))

                result.status shouldBe MeetingStatus.CONFIRMED
                result.finalizedDate shouldBe LocalDate.of(2026, 2, 20)
                verify(exactly = 1) { meetingRepository.save(any()) }
            }

            it("공동 1위일 때 finalizedDate가 없으면 확정할 수 없다") {
                val meeting = fixtureMeeting(
                    status = MeetingStatus.VOTING,
                    dates = setOf(
                        LocalDate.of(2026, 2, 20),
                        LocalDate.of(2026, 2, 21),
                    ),
                    participants = listOf(
                        fixtureParticipant(
                            id = 1L,
                            name = "A",
                            voteDates = setOf(LocalDate.of(2026, 2, 20)),
                        ),
                        fixtureParticipant(
                            id = 2L,
                            name = "B",
                            voteDates = setOf(LocalDate.of(2026, 2, 21)),
                        ),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.finalizeMeeting(meeting.id, null, UserId(1L))
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("공동 1위일 때 후보 중 날짜를 선택하면 확정된다") {
                val selectedDate = LocalDate.of(2026, 2, 21)
                val meeting = fixtureMeeting(
                    status = MeetingStatus.VOTING,
                    dates = setOf(
                        LocalDate.of(2026, 2, 20),
                        LocalDate.of(2026, 2, 21),
                    ),
                    participants = listOf(
                        fixtureParticipant(
                            id = 1L,
                            name = "A",
                            voteDates = setOf(LocalDate.of(2026, 2, 20)),
                        ),
                        fixtureParticipant(
                            id = 2L,
                            name = "B",
                            voteDates = setOf(LocalDate.of(2026, 2, 21)),
                        ),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting
                every { meetingRepository.save(any()) } answers { firstArg() }

                val result = meetingService.finalizeMeeting(meeting.id, selectedDate, UserId(1L))

                result.status shouldBe MeetingStatus.CONFIRMED
                result.finalizedDate shouldBe selectedDate
                verify(exactly = 1) { meetingRepository.save(any()) }
            }

            it("후보 날짜가 아닌 값을 확정일로 요청하면 에러가 발생한다") {
                val meeting = fixtureMeeting(
                    status = MeetingStatus.VOTING,
                    dates = setOf(LocalDate.of(2026, 2, 20)),
                    participants = listOf(
                        fixtureParticipant(
                            id = 1L,
                            name = "A",
                            voteDates = setOf(LocalDate.of(2026, 2, 20)),
                        ),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.finalizeMeeting(meeting.id, LocalDate.of(2026, 2, 25), UserId(1L))
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("이미 CONFIRMED인 모임에 같은 날짜로 재요청하면 멱등하게 반환한다") {
                val finalizedDate = LocalDate.of(2026, 2, 20)
                val meeting = fixtureMeeting(
                    status = MeetingStatus.CONFIRMED,
                    finalizedDate = finalizedDate,
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                val result = meetingService.finalizeMeeting(meeting.id, finalizedDate, UserId(1L))

                result.status shouldBe MeetingStatus.CONFIRMED
                result.finalizedDate shouldBe finalizedDate
                verify(exactly = 0) { meetingRepository.save(any()) }
            }
        }
        describe("getFinalizePreview") {
            it("최다 득표 날짜 후보와 투표자 정보를 반환한다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("preview-meeting"),
                    status = MeetingStatus.VOTING,
                    title = "프리뷰 모임",
                    dates = setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                    participants = listOf(
                        fixtureParticipant(id = 1L, name = "A", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                        fixtureParticipant(id = 2L, name = "B", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                        fixtureParticipant(id = 3L, name = "C", voteDates = setOf(LocalDate.of(2026, 2, 21))),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                val result = meetingService.getFinalizePreview(meeting.id, UserId(1L))

                result.meetingId shouldBe meeting.id
                result.meetingTitle shouldBe "프리뷰 모임"
                result.requiresDateSelection shouldBe false
                result.topDateVoteDetails.size shouldBe 1
                result.topDateVoteDetails.single().date shouldBe LocalDate.of(2026, 2, 20)
                result.topDateVoteDetails.single().voterNames shouldBe listOf("A", "B")
            }

            it("공동 1위면 확정일 선택 필요값이 true다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("preview-tie"),
                    status = MeetingStatus.VOTING,
                    dates = setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                    participants = listOf(
                        fixtureParticipant(id = 1L, name = "A", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                        fixtureParticipant(id = 2L, name = "B", voteDates = setOf(LocalDate.of(2026, 2, 21))),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                val result = meetingService.getFinalizePreview(meeting.id, UserId(1L))

                result.requiresDateSelection shouldBe true
                result.topDateVoteDetails.map { it.date } shouldBe listOf(
                    LocalDate.of(2026, 2, 20),
                    LocalDate.of(2026, 2, 21),
                )
            }

            it("이미 CONFIRMED인 모임은 프리뷰를 조회할 수 없다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("confirmed-meeting"),
                    status = MeetingStatus.CONFIRMED,
                    finalizedDate = LocalDate.of(2026, 2, 20),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.getFinalizePreview(meeting.id, UserId(1L))
                }
            }
        }

        describe("getHostMeetingDashboard") {
            it("주최자 모임을 상태별로 분리하고 요약/카드 정보를 계산한다") {
                val hostName = "주최자A"

                val votingMeeting = fixtureMeeting(
                    id = MeetingId("voting-meeting"),
                    status = MeetingStatus.VOTING,
                    dates = setOf(
                        LocalDate.of(2026, 2, 20),
                        LocalDate.of(2026, 2, 21),
                    ),
                    participants = listOf(
                        fixtureParticipant(id = 1L, name = "A", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                        fixtureParticipant(id = 2L, name = "B", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                    ),
                ).copy(hostName = hostName)

                val closedMeeting = fixtureMeeting(
                    id = MeetingId("closed-meeting"),
                    status = MeetingStatus.CLOSED,
                    dates = setOf(
                        LocalDate.of(2026, 2, 16),
                        LocalDate.of(2026, 2, 17),
                    ),
                    participants = listOf(
                        fixtureParticipant(id = 3L, name = "C", voteDates = setOf(LocalDate.of(2026, 2, 16))),
                        fixtureParticipant(id = 4L, name = "D", voteDates = setOf(LocalDate.of(2026, 2, 17))),
                    ),
                ).copy(hostName = hostName)

                val confirmedMeeting = fixtureMeeting(
                    id = MeetingId("confirmed-meeting"),
                    status = MeetingStatus.CONFIRMED,
                    finalizedDate = LocalDate.of(2026, 2, 18),
                    dates = setOf(LocalDate.of(2026, 2, 18)),
                    participants = listOf(
                        fixtureParticipant(id = 5L, name = "E", voteDates = setOf(LocalDate.of(2026, 2, 18))),
                    ),
                ).copy(hostName = hostName)

                val othersMeeting = fixtureMeeting(id = MeetingId("other-host-meeting")).copy(
                    hostName = "다른주최자",
                    hostUserId = UserId(2L),
                )

                every { meetingRepository.findAll() } returns listOf(
                    votingMeeting,
                    closedMeeting,
                    confirmedMeeting,
                    othersMeeting,
                )

                val dashboard = meetingService.getHostMeetingDashboard(hostUserId = UserId(1L))

                dashboard.hostName shouldBe hostName
                dashboard.summary.votingCount shouldBe 1
                dashboard.summary.confirmedCount shouldBe 1

                dashboard.inProgressMeetings.size shouldBe 1
                dashboard.confirmedMeetings.size shouldBe 1

                val votingCard = dashboard.inProgressMeetings.first { it.meetingId == MeetingId("voting-meeting") }
                votingCard.leadingDate shouldBe LocalDate.of(2026, 2, 20)
                votingCard.completedVoteCount shouldBe 2
                votingCard.totalVoteCount shouldBe 2

                val confirmedCard = dashboard.confirmedMeetings.single()
                confirmedCard.meetingId shouldBe MeetingId("confirmed-meeting")
                confirmedCard.finalizedDate shouldBe LocalDate.of(2026, 2, 18)
            }
        }

        describe("submitVote") {
            it("최대 참여 인원에 도달한 상태에서 신규 참여자가 투표하면 예외가 발생한다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("capacity-meeting"),
                    dates = setOf(LocalDate.of(2026, 2, 20)),
                    participants = listOf(
                        fixtureParticipant(id = 1L, name = "기존1", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                        fixtureParticipant(id = 2L, name = "기존2", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                    ),
                ).copy(maxParticipantCount = 2)
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.submitVote(
                        meetingId = meeting.id,
                        name = "신규참여자",
                        voteDates = setOf(LocalDate.of(2026, 2, 20)),
                    )
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("최대 참여 인원에 도달해도 기존 참여자의 투표 수정은 가능하다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("capacity-meeting"),
                    hostName = "주최자",
                    dates = setOf(LocalDate.of(2026, 2, 20)),
                    participants = listOf(
                        Participant(
                            id = ParticipantId(1L),
                            name = "주최자",
                            voteDates = emptySet(),
                            hasVoted = false,
                        ),
                    ),
                ).copy(maxParticipantCount = 1)
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting
                every { meetingRepository.save(any()) } answers { firstArg() }

                val result = meetingService.submitVote(
                    meetingId = meeting.id,
                    name = "주최자",
                    voteDates = setOf(LocalDate.of(2026, 2, 20)),
                )

                result.participants.single().hasVoted shouldBe true
                verify(exactly = 1) { meetingRepository.save(any()) }
            }
        }

        describe("updateMeeting") {
            it("투표중 모임의 제목/인원/후보날짜를 수정하고 참여자를 삭제할 수 있다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("update-meeting"),
                    status = MeetingStatus.VOTING,
                    dates = setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                    participants = listOf(
                        Participant(
                            id = ParticipantId(1L),
                            name = "주최자",
                            voteDates = setOf(LocalDate.of(2026, 2, 20)),
                            hasVoted = true,
                        ),
                        Participant(
                            id = ParticipantId(2L),
                            name = "삭제대상",
                            voteDates = emptySet(),
                            hasVoted = false,
                        ),
                    ),
                ).copy(hostName = "주최자", maxParticipantCount = 5)
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting
                every { meetingRepository.save(any()) } answers { firstArg() }

                val result = meetingService.updateMeeting(
                    meetingId = meeting.id,
                    requesterUserId = UserId(1L),
                    title = "수정된 모임명",
                    dates = setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 22)),
                    maxParticipantCount = 3,
                    removedParticipantNames = setOf("삭제대상"),
                )

                result.title shouldBe "수정된 모임명"
                result.maxParticipantCount shouldBe 3
                result.dates shouldBe setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 22))
                result.participants.map { it.name } shouldBe listOf("주최자")
                verify(exactly = 1) { meetingRepository.save(any()) }
            }

            it("투표중이 아닌 모임은 수정할 수 없다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("closed-meeting"),
                    status = MeetingStatus.CLOSED,
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.updateMeeting(
                        meetingId = meeting.id,
                        requesterUserId = UserId(1L),
                        title = "수정시도",
                        dates = setOf(LocalDate.of(2026, 2, 20)),
                        maxParticipantCount = 3,
                        removedParticipantNames = emptySet(),
                    )
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("이미 투표한 참여자는 삭제할 수 없다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("meeting-1"),
                    status = MeetingStatus.VOTING,
                    participants = listOf(
                        fixtureParticipant(id = 1L, name = "투표완료", voteDates = setOf(LocalDate.of(2026, 2, 20))),
                    ),
                ).copy(hostName = "다른주최자")
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.updateMeeting(
                        meetingId = meeting.id,
                        requesterUserId = UserId(1L),
                        title = "수정시도",
                        dates = setOf(LocalDate.of(2026, 2, 20)),
                        maxParticipantCount = 2,
                        removedParticipantNames = setOf("투표완료"),
                    )
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("기존 투표가 있는 날짜는 후보 날짜에서 제거할 수 없다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("meeting-2"),
                    status = MeetingStatus.VOTING,
                    dates = setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                    participants = listOf(
                        fixtureParticipant(id = 1L, name = "참여자", voteDates = setOf(LocalDate.of(2026, 2, 21))),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.updateMeeting(
                        meetingId = meeting.id,
                        requesterUserId = UserId(1L),
                        title = "수정시도",
                        dates = setOf(LocalDate.of(2026, 2, 20)),
                        maxParticipantCount = 2,
                        removedParticipantNames = emptySet(),
                    )
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("최대 인원을 현재 참여자 수보다 작게 줄일 수 없다") {
                val meeting = fixtureMeeting(
                    id = MeetingId("meeting-3"),
                    status = MeetingStatus.VOTING,
                    participants = listOf(
                        Participant(id = ParticipantId(1L), name = "A", voteDates = emptySet(), hasVoted = false),
                        Participant(id = ParticipantId(2L), name = "B", voteDates = emptySet(), hasVoted = false),
                    ),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.updateMeeting(
                        meetingId = meeting.id,
                        requesterUserId = UserId(1L),
                        title = "수정시도",
                        dates = setOf(LocalDate.of(2026, 2, 20)),
                        maxParticipantCount = 1,
                        removedParticipantNames = emptySet(),
                    )
                }

                verify(exactly = 0) { meetingRepository.save(any()) }
            }
        }
    }
},)

private fun fixtureMeeting(
    id: MeetingId = MeetingId("meeting-1"),
    title: String = "테스트 모임",
    hostName: String = "주최자",
    hostUserId: UserId = UserId(1L),
    status: MeetingStatus = MeetingStatus.VOTING,
    finalizedDate: LocalDate? = null,
    dates: Set<LocalDate> = setOf(LocalDate.of(2026, 2, 20)),
    participants: List<Participant> = emptyList(),
): Meeting {
    return Meeting(
        id = id,
        title = title,
        hostName = hostName,
        hostUserId = hostUserId,
        dates = dates,
        maxParticipantCount = null,
        participants = participants,
        status = status,
        finalizedDate = finalizedDate,
    )
}

private fun fixtureParticipant(
    id: Long,
    name: String,
    voteDates: Set<LocalDate>,
): Participant {
    return Participant(
        id = ParticipantId(id),
        name = name,
        voteDates = voteDates,
        hasVoted = true,
    )
}
