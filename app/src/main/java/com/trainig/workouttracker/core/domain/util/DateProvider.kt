package com.trainig.workouttracker.core.domain.util

import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface DateProvider {
    fun today(): LocalDate
    fun systemZoneId(): ZoneId
}

@Singleton
class SystemDateProvider @Inject constructor() : DateProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun systemZoneId(): ZoneId = ZoneId.systemDefault()
}

