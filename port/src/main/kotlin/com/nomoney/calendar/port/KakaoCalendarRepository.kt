package com.nomoney.calendar.port

import com.nomoney.calendar.domain.KakaoCalendarEvent
import com.nomoney.calendar.domain.KakaoCalendarEventCreateCommand

interface KakaoCalendarRepository {
    fun createEvent(accessToken: String, command: KakaoCalendarEventCreateCommand): KakaoCalendarEvent
}
