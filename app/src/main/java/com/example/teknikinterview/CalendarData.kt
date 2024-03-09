package com.example.teknikinterview

import java.time.LocalDate
import java.util.Calendar

data class CalendarData(
    val calendarName: String,
    val calendarDesc1: String,
    val calendarText: String,
    val calendarEventDatesCalendarData: ArrayList<Calendar>,
    val calendarStDate: LocalDate,
    val calendarEndDate: LocalDate
)
