package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeAndroidDataSource that acts as a test double to the LocalDataSource
class FakeAndroidDataSource : ReminderDataSource {

    private var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    fun setReturnError(flag: Boolean){
        shouldReturnError = flag
    }

    fun addReminders(vararg reminders: ReminderDTO){
        for(reminder in reminders){
            remindersServiceData[reminder.id] = reminder
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("error message")
        }
        return try {
            Result.Success(remindersServiceData.values.toList())
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("error message")
        }
        return try {
            Result.Success(remindersServiceData[id]!!)
        }catch (e: KotlinNullPointerException){
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }

}