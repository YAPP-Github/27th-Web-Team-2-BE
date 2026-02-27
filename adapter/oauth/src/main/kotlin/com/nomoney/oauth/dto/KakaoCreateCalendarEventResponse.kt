package com.nomoney.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoCreateCalendarEventResponse(
    @JsonProperty("event_id")
    val eventId: String,
)
