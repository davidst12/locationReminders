package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: KoinTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupFragmentDependencies() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { FakeAndroidDataSource() as ReminderDataSource }
        }
        // Declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        // Get fake repository
        repository = get()

        // Clear the data
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        //Let our test to know about data binding resources
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun clickAddReminderButton_navigateToAddReminderFragment() {
        // GIVEN
        val navController = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the addReminder
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        // THEN
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun noData_checkIsShownCorrectly() = runBlockingTest{
        repository.deleteAllReminders()

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withText("No Data")).check(
            ViewAssertions.matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun addReminderItems_checkAreShownCorrectly() = runBlockingTest{
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withText("title1")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("title2")).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun errorWhileGettingReminders_checkSnackBarIsShown() = runBlockingTest{
        (repository as FakeAndroidDataSource).setReturnError(true)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // It is needed to remove animations in phone setting
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("error message")).check(ViewAssertions.matches(isDisplayed()))
    }

}