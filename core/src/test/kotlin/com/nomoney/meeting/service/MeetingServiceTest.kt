package com.nomoney.meeting.service

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

        describe("closeMeeting") {
            it("VOTING 상태 모임은 CLOSED로 전환된다") {
                val meeting = fixtureMeeting(status = MeetingStatus.VOTING)
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting
                every { meetingRepository.save(any()) } answers { firstArg() }

                val result = meetingService.closeMeeting(meeting.id)

                result.status shouldBe MeetingStatus.CLOSED
                verify(exactly = 1) { meetingRepository.save(any()) }
            }

            it("이미 CLOSED인 모임을 다시 마감하면 변경 없이 반환된다") {
                val meeting = fixtureMeeting(status = MeetingStatus.CLOSED)
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                val result = meetingService.closeMeeting(meeting.id)

                result.status shouldBe MeetingStatus.CLOSED
                verify(exactly = 0) { meetingRepository.save(any()) }
            }

            it("이미 CONFIRMED인 모임은 마감할 수 없다") {
                val meeting = fixtureMeeting(
                    status = MeetingStatus.CONFIRMED,
                    finalizedDate = LocalDate.of(2026, 2, 20),
                )
                every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

                shouldThrow<InvalidRequestException> {
                    meetingService.closeMeeting(meeting.id)
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

                val result = meetingService.finalizeMeeting(meeting.id, null)

                result.status shouldBe MeetingStatus.CONFIRMED
                result.finalizedDate shouldBe LocalDate.of(2026, 2, 20)
                verify(exactly = 1) { meetingRepository.save(any()) }
            }

            it("공동 1위일 때 finalizedDate가 없으면 확정할 수 없다") {
                val meeting = fixtureMeeting(
                    status = MeetingStatus.CLOSED,
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
                    meetingService.finalizeMeeting(meeting.id, null)
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

                val result = meetingService.finalizeMeeting(meeting.id, selectedDate)

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
                    meetingService.finalizeMeeting(meeting.id, LocalDate.of(2026, 2, 25))
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

                val result = meetingService.finalizeMeeting(meeting.id, finalizedDate)

                result.status shouldBe MeetingStatus.CONFIRMED
                result.finalizedDate shouldBe finalizedDate
                verify(exactly = 0) { meetingRepository.save(any()) }
            }
        }
    }
},)

private fun fixtureMeeting(
    id: MeetingId = MeetingId("meeting-1"),
    status: MeetingStatus = MeetingStatus.VOTING,
    finalizedDate: LocalDate? = null,
    dates: Set<LocalDate> = setOf(LocalDate.of(2026, 2, 20)),
    participants: List<Participant> = emptyList(),
): Meeting {
    return Meeting(
        id = id,
        title = "테스트 모임",
        hostName = "주최자",
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
