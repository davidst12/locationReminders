package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var dataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel(){
        dataSource = FakeDataSource()
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        dataSource.addReminders(reminder1, reminder2)

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_NullReminderTitle_ReturnedFalseAndCheckShowSnackBarLiveData(){
        val reminder = ReminderDataItem(null, "description", "location", 1.0, 1.0)

        val result = saveReminderViewModel.validateEnteredData(reminder)

        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(result, `is`(false))
        assertThat(value, `is`(R.string.err_enter_title))

    }

    @Test
    fun validateEnteredData_EmptyReminderLocation_ReturnedFalseAndCheckShowSnackBarLiveData(){
        val reminder = ReminderDataItem("title", "description", "", 1.0, 1.0)

        val result = saveReminderViewModel.validateEnteredData(reminder)

        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(result, `is`(false))
        assertThat(value, `is`(R.string.err_select_location))

    }

    @Test
    fun validateEnteredData_CorrectReminder_True(){
        val reminder = ReminderDataItem("title", "description", "location", 1.0, 1.0)

        val result = saveReminderViewModel.validateEnteredData(reminder)

        assertEquals(true, result)

    }

    @Test
    fun saveReminder_ReminderItem_IsWellSaved() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem("title", "description", "location", 1.0, 1.0)

        saveReminderViewModel.saveReminder(reminder)

        val result = dataSource.getReminder(reminder.id) as com.udacity.project4.locationreminders.data.dto.Result.Success
        assertEquals(result.data.id, reminder.id)
    }

    @Test
    fun saveReminder_ReminderItem_CheckLiveDataValues() {
        val context = ApplicationProvider.getApplicationContext<Context?>()!!.applicationContext
        val reminder = ReminderDataItem("title", "description", "location", 1.0, 1.0)

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        val showLoadingValue = saveReminderViewModel.showLoading.getOrAwaitValue()
        val showToastValue = saveReminderViewModel.showToast.getOrAwaitValue()
        val navigationCommandValue = saveReminderViewModel.navigationCommand.getOrAwaitValue()

        assertThat(showLoadingValue, `is`(false))
        assertEquals(showToastValue, context.getString(R.string.reminder_saved))
        assertEquals(navigationCommandValue, NavigationCommand.Back)
    }

}