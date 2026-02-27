package com.nomoney.oauth.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.nomoney.calendar.domain.KakaoCalendarEvent
import com.nomoney.calendar.domain.KakaoCalendarEventCreateCommand
import com.nomoney.calendar.port.KakaoCalendarRepository
import com.nomoney.oauth.dto.KakaoCreateCalendarEventResponse
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

    private fun buildEvent(command: KakaoCalendarEventCreateCommand): Map<String, Any> {
        val event = linkedMapOf<String, Any>(
            "title" to command.title,
            "time" to mapOf(
                "all_day" to true,
                "start_at" to "${command.startDate}T00:00:00Z",
                "end_at" to "${command.endDate.plusDays(1)}T00:00:00Z",
            ),
        )

        val description = command.description
        if (!description.isNullOrBlank()) {
            event["description"] = description
        }
        val location = command.location
        if (!location.isNullOrBlank()) {
            event["location"] = location
        }

        return event
    }

    companion object {
        private const val CREATE_EVENT_URL = "https://kapi.kakao.com/v2/api/calendar/create/event"
    }
}
