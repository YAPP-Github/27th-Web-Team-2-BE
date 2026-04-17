package com.nomoney.meeting.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime

class MeetingTimeRangeTest : DescribeSpec({
    describe("MeetingTimeRange") {
        describe("slotCount") {
            it("09:00~18:00은 18개 슬롯이다") {
                val range = MeetingTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0))
                range.slotCount shouldBe 18
            }
            it("00:00~00:30은 1개 슬롯이다") {
                val range = MeetingTimeRange(LocalTime.of(0, 0), LocalTime.of(0, 30))
                range.slotCount shouldBe 1
            }
        }
        describe("startIndex") {
            it("09:00의 startIndex는 18이다") {
                val range = MeetingTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0))
                range.startIndex shouldBe 18
            }
            it("00:00의 startIndex는 0이다") {
                val range = MeetingTimeRange(LocalTime.of(0, 0), LocalTime.of(1, 0))
                range.startIndex shouldBe 0
            }
            it("09:30의 startIndex는 19이다") {
                val range = MeetingTimeRange(LocalTime.of(9, 30), LocalTime.of(10, 0))
                range.startIndex shouldBe 19
            }
        }
    }
})
