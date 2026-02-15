package com.nomoney.meeting.service

import com.nomoney.exception.DuplicateContentException
import com.nomoney.exception.InvalidRequestException
import com.nomoney.exception.NotFoundException
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.port.MeetingRepository
import java.security.SecureRandom
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.springframework.stereotype.Service

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
) {
    private val random = SecureRandom()

    fun createMeeting(
        title: String,
        hostName: String?,
        dates: Set<LocalDate>,
        maxParticipantCount: Int? = null,
    ): Meeting {
        assertValidMaxParticipantCount(maxParticipantCount)

        val meetingId = generateMeetId()
        val meeting = Meeting(
            id = meetingId,
            title = title,
            hostName = hostName,
            dates = dates,
            maxParticipantCount = maxParticipantCount,
            participants = emptyList(),
            status = MeetingStatus.VOTING,
            finalizedDate = null,
        )
        return meetingRepository.save(meeting)
    }

    fun getMeetingInfo(meetingId: MeetingId): Meeting? {
        return meetingRepository.findByMeetingId(meetingId)
    }

    fun getMeetingInfoSortedByParticipantUpdatedAt(meetingId: MeetingId): Meeting? {
        val meeting = meetingRepository.findByMeetingId(meetingId) ?: return null
        if (meeting.participants.isEmpty()) {
            return meeting
        }

        val sortedParticipants = meeting.participants.sortedWith(
            compareByDescending<Participant> { it.updatedAt },
        )

        return meeting.copy(participants = sortedParticipants)
    }

    fun getAllMeetings(): List<Meeting> {
        return meetingRepository.findAll()
    }

    fun updateMeeting(
        meetingId: MeetingId,
        title: String,
        dates: Set<LocalDate>,
        maxParticipantCount: Int?,
        removedParticipantNames: Set<String>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        if (meeting.status != MeetingStatus.VOTING) {
            throw InvalidRequestException(
                "투표중인 모임만 수정할 수 있습니다.",
                "meetingId=${meetingId.value}, status=${meeting.status}",
            )
        }

        assertValidMaxParticipantCount(maxParticipantCount)
        assertNotEmptyDates(meetingId, dates)
        assertRemovableParticipants(meeting, removedParticipantNames)

        val remainingParticipants = meeting.participants
            .filterNot { it.name in removedParticipantNames }

        assertVoteDatesWithinCandidates(
            meetingId = meetingId,
            participants = remainingParticipants,
            dates = dates,
        )

        if (maxParticipantCount != null && maxParticipantCount < remainingParticipants.size) {
            throw InvalidRequestException(
                "최대 참여 인원은 현재 참여자 수보다 작을 수 없습니다.",
                "meetingId=${meetingId.value}, maxParticipantCount=$maxParticipantCount, participants=${remainingParticipants.size}",
            )
        }

        return meetingRepository.save(
            meeting.copy(
                title = title,
                dates = dates,
                maxParticipantCount = maxParticipantCount,
                participants = remainingParticipants,
            ),
        )
    }

    fun getHostMeetingDashboard(
        hostName: String,
        today: LocalDate = LocalDate.now(),
    ): MeetingDashboard {
        val meetings = meetingRepository.findAll()
            .filter { it.hostName == hostName }

        val dashboardCards = meetings.map { meeting ->
            val topDates = topVotedDates(meeting)
            val leadingDate = topDates.minOrNull()
            val referenceDate = if (meeting.status == MeetingStatus.CONFIRMED) {
                meeting.finalizedDate
            } else {
                leadingDate
            }

            val completedVoteCount = meeting.participants.count { it.hasVoted }
            val totalVoteCount = (meeting.maxParticipantCount ?: meeting.participants.size)
                .coerceAtLeast(completedVoteCount)
            val voteProgressPercent = if (totalVoteCount == 0) {
                0
            } else {
                (completedVoteCount * 100) / totalVoteCount
            }

            MeetingDashboardCard(
                meetingId = meeting.id,
                title = meeting.title,
                status = meeting.status,
                leadingDate = leadingDate,
                isLeadingDateTied = topDates.size > 1,
                finalizedDate = meeting.finalizedDate,
                dDay = referenceDate?.let { ChronoUnit.DAYS.between(today, it).toInt() },
                completedVoteCount = completedVoteCount,
                totalVoteCount = totalVoteCount,
                voteProgressPercent = voteProgressPercent,
            )
        }

        return MeetingDashboard(
            hostName = hostName,
            summary = MeetingDashboardSummary(
                votingCount = meetings.count { it.status == MeetingStatus.VOTING },
                closedCount = meetings.count { it.status == MeetingStatus.CLOSED },
                confirmedCount = meetings.count { it.status == MeetingStatus.CONFIRMED },
            ),
            inProgressMeetings = dashboardCards
                .filter { it.status != MeetingStatus.CONFIRMED }
                .sortedWith(compareBy<MeetingDashboardCard> { it.dDay == null }.thenBy { it.dDay }),
            confirmedMeetings = dashboardCards
                .filter { it.status == MeetingStatus.CONFIRMED }
                .sortedWith(compareBy<MeetingDashboardCard> { it.dDay == null }.thenBy { it.dDay }),
        )
    }

    fun addParticipant(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
        hasVoted: Boolean,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        assertAvailableParticipantCapacity(meeting)

        val newParticipant = Participant(
            id = ParticipantId(0L),
            name = name,
            voteDates = voteDates,
            hasVoted = hasVoted,
        )

        val updatedMeeting = meeting.copy(
            participants = meeting.participants + newParticipant,
        )

        return meetingRepository.save(updatedMeeting)
    }

    fun updateParticipant(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        assertAllowedVoteDates(meeting, voteDates)

        if (meeting.participants.none { it.name == name }) {
            throw NotFoundException("참여자를 찾을 수 없습니다.", "name: $name")
        }

        val updatedParticipants = meeting.participants.map { participant ->
            if (participant.name == name) {
                participant.copy(
                    voteDates = voteDates,
                    hasVoted = true,
                )
            } else {
                participant
            }
        }

        val updatedMeeting = meeting.copy(participants = updatedParticipants)

        return meetingRepository.save(updatedMeeting)
    }

    fun submitVote(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        assertAllowedVoteDates(meeting, voteDates)

        val participant = meeting.participants.firstOrNull { it.name == name }
        return when {
            participant == null -> {
                addParticipant(
                    meetingId = meetingId,
                    name = name,
                    voteDates = voteDates,
                    hasVoted = true,
                )
            }
            participant.hasVoted -> {
                throw DuplicateContentException("이미 투표를 완료한 참여자입니다.", "name: $name")
            }
            else -> {
                require(meeting.hostName == name) { "주최자 Participant는 반드시 meeting.hostName과 동일한 name을 가져야 한다.: $name" }
                updateParticipant(
                    meetingId = meetingId,
                    name = name,
                    voteDates = voteDates,
                )
            }
        }
    }

    fun existsVotedParticipantByName(meetingId: MeetingId, name: String): Boolean {
        val meeting = getMeetingInfo(meetingId) ?: return false
        return meeting.participants.any { participant ->
            participant.name == name && participant.hasVoted
        }
    }

    fun closeMeeting(meetingId: MeetingId): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        return when (meeting.status) {
            MeetingStatus.VOTING -> {
                meetingRepository.save(
                    meeting.copy(
                        status = MeetingStatus.CLOSED,
                        finalizedDate = null,
                    ),
                )
            }
            MeetingStatus.CLOSED -> meeting
            MeetingStatus.CONFIRMED -> throw InvalidRequestException(
                "이미 확정된 모임은 마감할 수 없습니다.",
                "meetingId=${meetingId.value}",
            )
        }
    }

    fun finalizeMeeting(
        meetingId: MeetingId,
        selectedDate: LocalDate?,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        if (meeting.status == MeetingStatus.CONFIRMED) {
            return if (selectedDate == null || selectedDate == meeting.finalizedDate) {
                meeting
            } else {
                throw InvalidRequestException(
                    "이미 다른 날짜로 확정된 모임입니다.",
                    "meetingId=${meetingId.value}, finalizedDate=${meeting.finalizedDate}",
                )
            }
        }

        if (meeting.dates.isEmpty()) {
            throw InvalidRequestException(
                "후보 날짜가 없는 모임은 확정할 수 없습니다.",
                "meetingId=${meetingId.value}",
            )
        }

        if (selectedDate != null && selectedDate !in meeting.dates) {
            throw InvalidRequestException(
                "모임 후보 날짜에 없는 값은 확정일로 선택할 수 없습니다.",
                "meetingId=${meetingId.value}, selectedDate=$selectedDate",
            )
        }

        val resolvedFinalizedDate = resolveFinalizedDate(meeting, selectedDate)

        return meetingRepository.save(
            meeting.copy(
                status = MeetingStatus.CONFIRMED,
                finalizedDate = resolvedFinalizedDate,
            ),
        )
    }

    fun generateMeetId(): MeetingId {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val meetId = (1..12)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")

        return MeetingId(meetId)
    }

    private fun assertAllowedVoteDates(meeting: Meeting, voteDates: Set<LocalDate>) {
        if (!meeting.isVoteDatesAllowed(voteDates)) {
            throw InvalidRequestException(
                "모임에서 선택 가능한 날짜가 아닙니다.",
                "meetingId=${meeting.id.value}}",
            )
        }
    }

    private fun assertValidMaxParticipantCount(maxParticipantCount: Int?) {
        if (maxParticipantCount != null && maxParticipantCount < 1) {
            throw InvalidRequestException(
                "최대 참여 인원은 1 이상이어야 합니다.",
                "maxParticipantCount=$maxParticipantCount",
            )
        }
    }

    private fun assertNotEmptyDates(
        meetingId: MeetingId,
        dates: Set<LocalDate>,
    ) {
        if (dates.isEmpty()) {
            throw InvalidRequestException(
                "후보 날짜는 1개 이상이어야 합니다.",
                "meetingId=${meetingId.value}",
            )
        }
    }

    private fun assertAvailableParticipantCapacity(meeting: Meeting) {
        val maxParticipantCount = meeting.maxParticipantCount ?: return
        if (meeting.participants.size >= maxParticipantCount) {
            throw InvalidRequestException(
                "최대 참여 인원을 초과할 수 없습니다.",
                "meetingId=${meeting.id.value}, maxParticipantCount=$maxParticipantCount, currentParticipants=${meeting.participants.size}",
            )
        }
    }

    private fun assertRemovableParticipants(
        meeting: Meeting,
        removedParticipantNames: Set<String>,
    ) {
        if (removedParticipantNames.isEmpty()) {
            return
        }

        val participantsByName = meeting.participants.associateBy { it.name }
        val unknownNames = removedParticipantNames.filterNot { it in participantsByName.keys }
        if (unknownNames.isNotEmpty()) {
            throw InvalidRequestException(
                "존재하지 않는 참여자는 삭제할 수 없습니다.",
                "meetingId=${meeting.id.value}, unknownNames=$unknownNames",
            )
        }

        if (meeting.hostName != null && meeting.hostName in removedParticipantNames) {
            throw InvalidRequestException(
                "주최자는 삭제할 수 없습니다.",
                "meetingId=${meeting.id.value}, hostName=${meeting.hostName}",
            )
        }

        val votedParticipantNames = removedParticipantNames.filter { name ->
            participantsByName.getValue(name).hasVoted
        }
        if (votedParticipantNames.isNotEmpty()) {
            throw InvalidRequestException(
                "이미 투표한 참여자는 삭제할 수 없습니다.",
                "meetingId=${meeting.id.value}, votedParticipantNames=$votedParticipantNames",
            )
        }
    }

    private fun assertVoteDatesWithinCandidates(
        meetingId: MeetingId,
        participants: List<Participant>,
        dates: Set<LocalDate>,
    ) {
        val invalidParticipants = participants
            .filter { participant -> (participant.voteDates - dates).isNotEmpty() }
            .map { it.name }

        if (invalidParticipants.isNotEmpty()) {
            throw InvalidRequestException(
                "기존 투표 데이터와 충돌하는 후보 날짜 변경입니다.",
                "meetingId=${meetingId.value}, invalidParticipants=$invalidParticipants",
            )
        }
    }

    private fun resolveFinalizedDate(
        meeting: Meeting,
        selectedDate: LocalDate?,
    ): LocalDate {
        val topDates = topVotedDates(meeting)
        return when {
            topDates.size == 1 -> {
                val topDate = topDates.first()
                if (selectedDate != null && selectedDate != topDate) {
                    throw InvalidRequestException(
                        "최다 득표 날짜와 다른 날짜를 확정일로 선택할 수 없습니다.",
                        "meetingId=${meeting.id.value}, selectedDate=$selectedDate, expected=$topDate",
                    )
                }
                selectedDate ?: topDate
            }
            selectedDate == null -> throw InvalidRequestException(
                "공동 1위 날짜가 있어 확정일 선택이 필요합니다.",
                "meetingId=${meeting.id.value}, candidateDates=${topDates.sorted()}",
            )
            selectedDate !in topDates -> throw InvalidRequestException(
                "공동 1위 날짜 중에서 확정일을 선택해야 합니다.",
                "meetingId=${meeting.id.value}, selectedDate=$selectedDate, candidateDates=${topDates.sorted()}",
            )
            else -> selectedDate
        }
    }

    private fun topVotedDates(meeting: Meeting): Set<LocalDate> {
        val voteCounts = meeting.dates.associateWith { 0 }.toMutableMap()

        meeting.participants
            .filter { it.hasVoted }
            .forEach { participant ->
                participant.voteDates
                    .filter { it in voteCounts }
                    .forEach { voteDate ->
                        voteCounts[voteDate] = voteCounts.getValue(voteDate) + 1
                    }
            }

        val maxCount = voteCounts.maxOfOrNull { it.value } ?: 0
        return voteCounts
            .filterValues { it == maxCount }
            .keys
    }
}
