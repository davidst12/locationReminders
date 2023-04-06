package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.Is.`is`
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel(){
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_DataSourceWithTwoItems_CorrectReminderList() {
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        dataSource.addReminders(reminder1, reminder2)

        remindersListViewModel.loadReminders()

        assertEquals(remindersListViewModel.remindersList.value!![0].id, reminder1.id)
        assertEquals(remindersListViewModel.remindersList.value!![1].id, reminder2.id)
    }

    @Test
    fun loadReminders_DataSourceEmpty_EmptyListLiveDataIsTrue(){
        remindersListViewModel.loadReminders()

        val value = remindersListViewModel.showNoData.getOrAwaitValue()
        assertThat(value, `is`(true))
    }

    @Test
    fun loadReminders_DataSourceNoEmpty_EmptyListLiveDataIsFalse(){
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        dataSource.addReminders(reminder1, reminder2)

        remindersListViewModel.loadReminders()

        val value = remindersListViewModel.showNoData.getOrAwaitValue()
        assertThat(value, `is`(false))
    }

    @Test
    fun loadReminders_DataSourceReturnError_ShowSnackBarLiveDataIsFalse(){
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        dataSource.addReminders(reminder1, reminder2)
        dataSource.setReturnError(true)

        remindersListViewModel.loadReminders()

        val value = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assertThat(value, `is`("error message"))
    }

    @Test
    fun loadReminders_CheckShowLoadingValueChanges(){
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}