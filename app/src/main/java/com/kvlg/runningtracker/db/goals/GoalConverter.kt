package com.kvlg.runningtracker.db.goals

import java.math.BigDecimal
import com.kvlg.runningtracker.models.WeekGoal as DomainWeekGoal

/**
 * @author Konstantin Koval
 * @since 13.09.2020
 */
class GoalConverter {
    fun convertToDomainModel(dbGoal: WeekGoal?): DomainWeekGoal = DomainWeekGoal(
        time = getPlainString(dbGoal?.time ?: 0.0),
        speed = getPlainString(dbGoal?.speed ?: 0.0),
        distance = getPlainString(dbGoal?.distance ?: 0.0),
        calories = getPlainString(dbGoal?.calories ?: 0.0),
    )

    private fun getPlainString(value: Double) = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
}