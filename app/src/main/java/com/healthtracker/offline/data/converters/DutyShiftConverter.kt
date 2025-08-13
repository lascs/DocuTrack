package com.healthtracker.offline.data.converters

import androidx.room.TypeConverter
import com.healthtracker.offline.data.entities.DutyShift

/**
 * Room TypeConverter for converting DutyShift enum to/from string for database storage.
 */
class DutyShiftConverter {
    
    /**
     * Converts DutyShift enum to string for database storage
     * @param dutyShift DutyShift enum value
     * @return String representation of the enum, or "FULL_DAY" if null
     */
    @TypeConverter
    fun fromDutyShift(dutyShift: DutyShift?): String {
        return dutyShift?.name ?: DutyShift.FULL_DAY.name
    }
    
    /**
     * Converts string back to DutyShift enum from database
     * @param value String value from database
     * @return DutyShift enum, or FULL_DAY if null/invalid
     */
    @TypeConverter
    fun toDutyShift(value: String?): DutyShift {
        return if (value.isNullOrBlank()) {
            DutyShift.FULL_DAY
        } else {
            try {
                DutyShift.valueOf(value)
            } catch (e: IllegalArgumentException) {
                // If enum parsing fails, return default
                DutyShift.FULL_DAY
            }
        }
    }
}