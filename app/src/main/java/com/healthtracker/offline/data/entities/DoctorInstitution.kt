package com.healthtracker.offline.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.healthtracker.offline.data.converters.StringListConverter
import com.healthtracker.offline.data.converters.DutyShiftConverter

/**
 * Room entity representing the assignment relationship between Doctor and Institution.
 * This is a junction table that handles many-to-many relationships.
 * 
 * @param assignmentId Primary key, auto-generated
 * @param doctorId Foreign key reference to Doctor
 * @param institutionId Foreign key reference to Institution
 * @param wardId Foreign key reference to Ward (optional)
 * @param designation Doctor's designation at this institution
 * @param dutyShift Duty shift (Morning, Evening, FullDay)
 * @param dutyDays List of days when doctor is on duty
 */
@Entity(
    tableName = "doctor_institutions",
    foreignKeys = [
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["doctorId"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Institution::class,
            parentColumns = ["institutionId"],
            childColumns = ["institutionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ward::class,
            parentColumns = ["wardId"],
            childColumns = ["wardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["doctorId"]),
        Index(value = ["institutionId"]),
        Index(value = ["wardId"]),
        Index(value = ["doctorId", "institutionId", "wardId"], unique = true)
    ]
)
@TypeConverters(StringListConverter::class, DutyShiftConverter::class)
data class DoctorInstitution(
    @PrimaryKey(autoGenerate = true)
    val assignmentId: Int = 0,
    
    val doctorId: Int,
    
    val institutionId: Int,
    
    val wardId: Int? = null,
    
    val designation: String = "",
    
    val dutyShift: DutyShift = DutyShift.FULL_DAY,
    
    val dutyDays: List<String> = emptyList()
) {
    /**
     * Validates the assignment data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return doctorId > 0 && 
               institutionId > 0 &&
               areValidDays(dutyDays)
    }
    
    /**
     * Validates that all days in the list are valid day names
     */
    private fun areValidDays(days: List<String>): Boolean {
        val validDays = setOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", 
            "Friday", "Saturday", "Sunday"
        )
        return days.all { it in validDays }
    }
    
    /**
     * Returns formatted duty days as a comma-separated string
     */
    fun getFormattedDutyDays(): String {
        return dutyDays.joinToString(", ")
    }
    
    /**
     * Checks if the doctor is on duty on a specific day
     */
    fun isOnDutyOnDay(day: String): Boolean {
        return dutyDays.contains(day)
    }
    
    /**
     * Returns a summary of the assignment
     */
    fun getAssignmentSummary(): String {
        val parts = mutableListOf<String>()
        if (designation.isNotBlank()) parts.add(designation)
        parts.add(dutyShift.displayName)
        if (dutyDays.isNotEmpty()) parts.add(getFormattedDutyDays())
        return parts.joinToString(" - ")
    }
}

/**
 * Enum representing different duty shifts
 */
enum class DutyShift(val displayName: String) {
    MORNING("Morning"),
    EVENING("Evening"),
    FULL_DAY("Full Day");
    
    companion object {
        /**
         * Gets DutyShift from display name
         */
        fun fromDisplayName(displayName: String): DutyShift {
            return values().find { it.displayName == displayName } ?: FULL_DAY
        }
    }
}