package com.kvlg.runningtracker.models

import java.math.BigDecimal

/**
 * Week goals for profile (domain layer)
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
data class WeekGoal(
    val time: BigDecimal,
    val speed: BigDecimal,
    val distance: BigDecimal,
    val calories: BigDecimal
)