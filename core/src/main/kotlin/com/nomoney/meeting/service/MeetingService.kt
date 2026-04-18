package com.nomoney.meeting.service

import com.nomoney.auth.domain.UserId
import com.nomoney.exception.DuplicateContentException
import com.nomoney.exception.InvalidRequestException
import com.nomoney.exception.NotFoundException
import com.nomoney.exception.UnauthorizedException
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.domain.MeetingSummary
import com.nomoney.meeting.domain.MeetingTimeRange
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.port.MeetingRepository
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalTime
import org.springframework.stereotype.Service

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
) {
    companion object {
        private const val MAX_MEMO_LENGTH = 200
    }

    private val random = SecureRandom()

    fun createMeeting(
        title: String,
        hostName: String?,
        hostUserId: UserId?,
        dates: Set<LocalDate>,
        maxParticipantCount: Int? = null,
        timeRange: MeetingTimeRange? = null,
    ): Meeting {
        assertValidMaxParticipantCount(maxParticipantCount)

        val meetingId = generateMeetId()
        val meeting = Meeting(
            id = meetingId,
            title = title,
            hostName = hostName,
            hostUserId = hostUserId,
            dates = dates,
            maxParticipantCount = maxParticipantCount,
            participants = emptyList(),
            status = MeetingStatus.VOTING,
            finalizedDate = null,
            timeRange = timeRange,
        )
        return meetingRepository.save(meeting)
    }

    fun getMeetingInfo(meetingId: MeetingId): Meeting? {
        return meetingRepository.findByMeetingId(meetingId)
    }

    fun saveMeetingMemo(
        meetingId: MeetingId,
        requesterUserId: UserId,
        memo: String,
    ): Boolean {
        if (memo.length > MAX_MEMO_LENGTH) {
            throw InvalidRequestException(
                "메모는 200자까지 입력 가능합니다.",
                "meetingId=${meetingId.value}, memoLength=${memo.length}",
            )
        }

        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
        assertMeetingHostOwnership(meeting, requesterUserId)

        meetingRepository.save(meeting.copy(memo = memo))
        return true
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

    fun getAllMeetingSummaries(): List<MeetingSummary> {
        return meetingRepository.findAllMeetingSummaries()
    }

    fun getHostMeetingDetail(
        meetingId: MeetingId,
        requesterUserId: UserId,
    ): MeetingHostDetail {
        val meeting = getMeetingInfoSortedByParticipantUpdatedAt(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
        assertMeetingHostOwnership(meeting, requesterUserId)

        val totalParticipantCount = meeting.maxParticipantCount ?: meeting.participants.size
        val votedParticipantCount = meeting.participants.count { it.hasVoted }

        return MeetingHostDetail(
            meeting = meeting,
            notVotedParticipantCount = (totalParticipantCount - votedParticipantCount).coerceAtLeast(0),
        )
    }

    fun updateMeeting(
        meetingId: MeetingId,
        requesterUserId: UserId,
        title: String,
        dates: Set<LocalDate>,
        maxParticipantCount: Int?,
        removedParticipantNames: Set<String>,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
        assertMeetingHostOwnership(meeting, requesterUserId)

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
        hostUserId: UserId,
    ): MeetingDashboard {
        val meetings = meetingRepository.findAllByHostUserId(hostUserId)

        val dashboardCards = meetings.map { meeting ->
            val topDateVoteDetails = topDateVoteDetails(meeting)
            val leadingDate = topDateVoteDetails.minOfOrNull { it.date }
            val referenceDate = if (meeting.status == MeetingStatus.CONFIRMED) {
                meeting.finalizedDate
            } else {
                leadingDate
            }

            val completedVoteCount = meeting.participants.count { it.hasVoted }
            val totalVoteCount = (meeting.maxParticipantCount ?: meeting.participants.size)
                .coerceAtLeast(completedVoteCount)
            MeetingDashboardCard(
                meetingId = meeting.id,
                title = meeting.title,
                status = meeting.status,
                leadingDate = leadingDate,
                finalizedDate = meeting.finalizedDate,
                completedVoteCount = completedVoteCount,
                totalVoteCount = totalVoteCount,
                memo = meeting.memo,
            ) to referenceDate
        }

        return MeetingDashboard(
            hostName = meetings.firstNotNullOfOrNull { it.hostName } ?: "",
            summary = MeetingDashboardSummary(
                votingCount = meetings.count { it.status == MeetingStatus.VOTING },
                confirmedCount = meetings.count { it.status == MeetingStatus.CONFIRMED },
            ),
            inProgressMeetings = dashboardCards
                .filter { it.first.status == MeetingStatus.VOTING }
                .sortedWith(compareBy<Pair<MeetingDashboardCard, LocalDate?>> { it.second == null }.thenBy { it.second })
                .map { it.first },
            confirmedMeetings = dashboardCards
                .filter { it.first.status == MeetingStatus.CONFIRMED }
                .sortedWith(compareBy<Pair<MeetingDashboardCard, LocalDate?>> { it.second == null }.thenBy { it.second })
                .map { it.first },
        )
    }

    fun addParticipant(
        meetingId: MeetingId,
        name: String,
        voteDates: Set<LocalDate>,
        hasVoted: Boolean,
        voteTimeSlots: Map<LocalDate, String> = emptyMap(),
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        assertAvailableParticipantCapacity(meeting)

        val newParticipant = Participant(
            id = ParticipantId(0L),
            name = name,
            voteDates = voteDates,
            hasVoted = hasVoted,
            voteTimeSlots = voteTimeSlots,
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
        voteTimeSlots: Map<LocalDate, String> = emptyMap(),
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
                    voteTimeSlots = voteTimeSlots,
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
        voteDates: List<LocalDate>,
        voteTimeSlots: List<List<Boolean>>? = null,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        val (resolvedVoteDates, resolvedVoteTimeSlots) = resolveVoteData(meeting, voteDates, voteTimeSlots)
        assertAllowedVoteDates(meeting, resolvedVoteDates)

        val participant = meeting.participants.firstOrNull { it.name == name }
        return when {
            participant == null -> addParticipant(
                meetingId = meetingId,
                name = name,
                voteDates = resolvedVoteDates,
                hasVoted = true,
                voteTimeSlots = resolvedVoteTimeSlots,
            )
            participant.hasVoted -> throw DuplicateContentException("이미 투표를 완료한 참여자입니다.", "name: $name")
            else -> {
                require(meeting.hostName == name) { "주최자 Participant는 반드시 meeting.hostName과 동일한 name을 가져야 한다.: $name" }
                updateParticipant(
                    meetingId = meetingId,
                    name = name,
                    voteDates = resolvedVoteDates,
                    voteTimeSlots = resolvedVoteTimeSlots,
                )
            }
        }
    }

    fun updateParticipantWithTimeSlots(
        meetingId: MeetingId,
        name: String,
        voteDates: List<LocalDate>,
        voteTimeSlots: List<List<Boolean>>? = null,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

        val (resolvedVoteDates, resolvedVoteTimeSlots) = resolveVoteData(meeting, voteDates, voteTimeSlots)
        return updateParticipant(
            meetingId = meetingId,
            name = name,
            voteDates = resolvedVoteDates,
            voteTimeSlots = resolvedVoteTimeSlots,
        )
    }

    fun existsVotedParticipantByName(meetingId: MeetingId, name: String): Boolean {
        val meeting = getMeetingInfo(meetingId) ?: return false
        return meeting.participants.any { participant ->
            participant.name == name && participant.hasVoted
        }
    }

    fun finalizeMeeting(
        meetingId: MeetingId,
        selectedDate: LocalDate?,
        requesterUserId: UserId,
        finalizedStartTime: LocalTime? = null,
        finalizedEndTime: LocalTime? = null,
    ): Meeting {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
        assertMeetingHostOwnership(meeting, requesterUserId)

        if (meeting.timeRange != null && (finalizedStartTime == null || finalizedEndTime == null)) {
            throw InvalidRequestException(
                "시간 투표 모임은 확정 시 시작/종료 시간이 필요합니다.",
                "meetingId=${meetingId.value}",
            )
        }

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
                finalizedStartTime = finalizedStartTime,
                finalizedEndTime = finalizedEndTime,
            ),
        )
    }

    fun checkFinalizedDateConflictAndFinalizeMeeting(
        meetingId: MeetingId,
        finalizedDate: LocalDate,
        requesterUserId: UserId,
        finalizedStartTime: LocalTime? = null,
        finalizedEndTime: LocalTime? = null,
    ): Boolean {
        if (
            hasDateConflictWithConfirmedMeetings(
                requesterUserId = requesterUserId,
                meetingId = meetingId,
                finalizedDate = finalizedDate,
            )
        ) {
            return true
        }

        finalizeMeeting(
            meetingId = meetingId,
            selectedDate = finalizedDate,
            requesterUserId = requesterUserId,
            finalizedStartTime = finalizedStartTime,
            finalizedEndTime = finalizedEndTime,
        )
        return false
    }

    fun getFinalizePreview(
        meetingId: MeetingId,
        requesterUserId: UserId,
    ): MeetingFinalizePreview {
        val meeting = getMeetingInfo(meetingId)
            ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
        assertMeetingHostOwnership(meeting, requesterUserId)

        if (meeting.status == MeetingStatus.CONFIRMED) {
            throw InvalidRequestException(
                "이미 확정된 모임입니다.",
                "meetingId=${meetingId.value}",
            )
        }

        val topDateVoteDetails = topDateVoteDetails(meeting)
        return MeetingFinalizePreview(
            meetingId = meeting.id,
            meetingTitle = meeting.title,
            topDateVoteDetails = topDateVoteDetails,
            requiresDateSelection = topDateVoteDetails.size > 1,
        )
    }

    private fun resolveVoteData(
        meeting: Meeting,
        voteDates: List<LocalDate>,
        voteTimeSlots: List<List<Boolean>>?,
    ): Pair<Set<LocalDate>, Map<LocalDate, String>> {
        if (voteTimeSlots == null) {
            return voteDates.toSet() to emptyMap()
        }

        val timeRange = meeting.timeRange
            ?: throw InvalidRequestException(
                "날짜 전용 모임에는 시간 슬롯 투표를 할 수 없습니다.",
                "meetingId=${meeting.id.value}",
            )

        val sortedDates = meeting.dates.sorted()
        if (voteTimeSlots.size != sortedDates.size) {
            throw InvalidRequestException(
                "날짜 수와 시간 슬롯 배열 수가 일치하지 않습니다.",
                "expected=${sortedDates.size}, actual=${voteTimeSlots.size}",
            )
        }

        val offset = timeRange.startIndex
        val slotCount = timeRange.slotCount

        val resultMap = sortedDates.mapIndexed { i, date ->
            val bools = voteTimeSlots[i]
            if (bools.size != slotCount) {
                throw InvalidRequestException(
                    "슬롯 수가 일치하지 않습니다.",
                    "expected=$slotCount, actual=${bools.size}",
                )
            }
            val mask = buildString(48) {
                repeat(offset) { append('0') }
                bools.forEach { append(if (it) '1' else '0') }
                repeat(48 - offset - slotCount) { append('0') }
            }
            date to mask
        }.toMap()

        val derivedVoteDates = resultMap
            .filterValues { mask -> mask.contains('1') }
            .keys

        return derivedVoteDates to resultMap
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

    private fun assertMeetingHostOwnership(
        meeting: Meeting,
        requesterUserId: UserId,
    ) {
        val hostUserId = meeting.hostUserId
            ?: throw UnauthorizedException(
                "모임 주최자 정보가 없습니다. meetingId=${meeting.id.value}",
            )

        if (hostUserId != requesterUserId) {
            throw UnauthorizedException(
                "모임 주최자만 요청할 수 있습니다. meetingId=${meeting.id.value}, requesterUserId=${requesterUserId.value}, hostUserId=${hostUserId.value}",
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
        return topDateVoteDetails(meeting)
            .map { it.date }
            .toSet()
    }

    private fun hasDateConflictWithConfirmedMeetings(
        requesterUserId: UserId,
        meetingId: MeetingId,
        finalizedDate: LocalDate,
    ): Boolean {
        return meetingRepository.existsConfirmedMeetingByHostUserIdAndFinalizedDate(
            hostUserId = requesterUserId,
            meetingIdToExclude = meetingId,
            finalizedDate = finalizedDate,
        )
    }

    private fun topDateVoteDetails(meeting: Meeting): List<MeetingDateVoteDetail> {
        val voteNamesByDate = meeting.dates
            .associateWith { mutableListOf<String>() }
            .toMutableMap()

        meeting.participants
            .filter { it.hasVoted }
            .forEach { participant ->
                participant.voteDates
                    .filter { it in voteNamesByDate }
                    .forEach { voteDate ->
                        voteNamesByDate.getValue(voteDate).add(participant.name)
                    }
            }

        val maxCount = voteNamesByDate.maxOfOrNull { it.value.size } ?: 0
        return voteNamesByDate
            .filterValues { it.size == maxCount }
            .toSortedMap()
            .map { (date, voters) ->
                val voterNames = voters.sorted()
                MeetingDateVoteDetail(
                    date = date,
                    voteCount = voterNames.size,
                    voterNames = voterNames,
                )
            }
    }
}
