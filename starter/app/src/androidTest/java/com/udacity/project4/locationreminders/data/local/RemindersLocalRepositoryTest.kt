package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_RetrievesReminderByIdCorrectly() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        localDataSource.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same reminder is returned.
        assertThat(result.succeeded, Is.`is`(true))
        result as Result.Success<ReminderDTO>
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminders_retrievesRemindersCorrectly() = runBlocking {
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)

        val result = localDataSource.getReminders()

        assertThat(result.succeeded, Is.`is`(true))
        result as Result.Success<List<ReminderDTO>>
        assertThat(result.data[0].title, `is`(reminder1.title))
        assertThat(result.data[1].title, `is`(reminder2.title))
    }

    @Test
    fun deleteReminders_retrievesRemindersReturnNone() = runBlocking {
        localDataSource.deleteAllReminders()

        val result = localDataSource.getReminders()

        assertThat(result.succeeded, Is.`is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun saveReminder_retrievesWithWrongIdReminder() = runBlocking {
        val reminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        localDataSource.saveReminder(reminder)

        val result = localDataSource.getReminder(UUID.randomUUID().toString())

        assertThat(result.succeeded, Is.`is`(false))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }
}