package com.nomoney.oauth.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.nomoney.calendar.domain.KakaoCalendarEvent
import com.nomoney.calendar.domain.KakaoCalendarEventCreateCommand
import com.nomoney.calendar.port.KakaoCalendarRepository
import com.nomoney.oauth.dto.KakaoCreateCalendarEventResponse
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class KakaoCalendarApiClient(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) : KakaoCalendarRepository {

    override fun createEvent(
        accessToken: String,
        command: KakaoCalendarEventCreateCommand,
    ): KakaoCalendarEvent {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val request = LinkedMultiValueMap<String, String>().apply {
            add("event", objectMapper.writeValueAsString(buildEvent(command)))
        }

        val response = restTemplate.exchange(
            CREATE_EVENT_URL,
            HttpMethod.POST,
            HttpEntity(request, headers),
            KakaoCreateCalendarEventResponse::class.java,
        ).body ?: throw RuntimeException("카카오 캘린더 이벤트 생성 실패: 응답이 없습니다")

        return KakaoCalendarEvent(eventId = response.eventId)
    }

    private fun buildEvent(command: KakaoCalendarEventCreateCommand): Map<String, Any?> {
        val zoneId = ZoneId.of("Asia/Seoul")

        return mapOf(
            "title" to command.title,
            "description" to command.description,
            "location" to command.location,
            "time" to mapOf(
                "all_day" to true,
                "start_at" to command.startDate.atStartOfDay(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "end_at" to command.endDate.plusDays(1).atStartOfDay(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            ),
        )
    }

    companion object {
        private const val CREATE_EVENT_URL = "https://kapi.kakao.com/v2/api/calendar/create/event"
    }
}
