package com.kvlg.runningtracker.db.goals

import java.math.BigDecimal
import com.kvlg.runningtracker.models.WeekGoal as DomainWeekGoal

/**
 * @author Konstantin Koval
 * @since 13.09.2020
 */
class GoalConverter {
    fun convertToDomainModel(dbGoal: WeekGoal?): DomainWeekGoal = DomainWeekGoal(
        time = BigDecimal.valueOf(dbGoal?.time ?: 0.0).stripTrailingZeros(),
        speed = BigDecimal.valueOf(dbGoal?.speed ?: 0.0).stripTrailingZeros(),
        distance = BigDecimal.valueOf(dbGoal?.distance ?: 0.0).stripTrailingZeros(),
        calories = BigDecimal.valueOf(dbGoal?.calories ?: 0.0).stripTrailingZeros(),
    )
}