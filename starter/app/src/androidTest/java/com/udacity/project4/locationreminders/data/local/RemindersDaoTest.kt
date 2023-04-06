package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb(){
        database.close()
    }

    @Test
    fun saveReminder_GetRemindersCorrectly() = runBlockingTest{
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        val loaded = database.reminderDao().getReminders()

        assertThat(loaded, `is`(notNullValue()))
        assertThat(loaded[0].id, `is`(reminder1.id))
        assertThat(loaded[0].title, `is`(reminder1.title))
        assertThat(loaded[1].id, `is`(reminder2.id))
        assertThat(loaded[1].title, `is`(reminder2.title))
    }

    @Test
    fun saveReminder_GetByIdCorrectly() = runBlockingTest{
        val reminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat(loaded!!, Is.`is`(notNullValue()))
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminder_GetRemindersReturnNone() = runBlockingTest{
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().deleteAllReminders()

        val loaded = database.reminderDao().getReminders()

        assertThat(loaded.size, `is`(0))
    }
}