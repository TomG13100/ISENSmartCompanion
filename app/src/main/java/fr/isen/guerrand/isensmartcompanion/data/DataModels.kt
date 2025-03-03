package fr.isen.guerrand.isensmartcompanion.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface ScheduleItem {
    val date: String
}

@Parcelize
data class Event(
    val id: String,
    val title: String,
    override val date: String,
    val description: String,
    val location: String,
    val category: String,
    var isSubscribed: Boolean = false
) : Parcelable, ScheduleItem

@Parcelize
data class StudentCourse(
    val id: String,
    val title: String,
    override val date: String,
    val time: String,
    val description: String = "",
    val location: String
) : Parcelable, ScheduleItem


fun loadStudentCourses(): List<StudentCourse> {
    return listOf(
        StudentCourse("1", "Math - Algebra", "2025-03-01", "10:00 - 11:30", "","Room 101"),
        StudentCourse("2", "Physics - Mechanics", "2025-03-01", "13:00 - 14:30", "","Lab 202"),
        StudentCourse("3", "Computer Science - AI", "2025-03-02", "09:00 - 10:30", "","Room 303"),
        StudentCourse("4", "Business Strategy", "2025-03-02", "11:00 - 12:30", "","Room 105")
    )
}