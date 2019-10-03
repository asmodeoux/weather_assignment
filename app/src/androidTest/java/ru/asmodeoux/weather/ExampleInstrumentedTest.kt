package ru.asmodeoux.weather

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule

import org.junit.Test

import org.junit.Rule
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.uiautomator.UiDevice
import android.support.test.espresso.UiController
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.ViewAction
import android.view.View
import org.hamcrest.Matcher


class ExampleInstrumentedTest {

    @Rule @JvmField
    val mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun checkViewMore() {
        onView(withId(R.id.view_more)).perform(click())
        onView(withId(R.id.view_more_parent)).check(matches(isDisplayed()))
    }

    @Test
    fun checkRotation() {
        val device = UiDevice.getInstance(getInstrumentation()) // library to easily handle screen rotation during test

        device.setOrientationLeft()
        onView(isRoot()).perform(waitFor(200)) // a delay for rotation to finish
        onView(withId(R.id.citiesRV)).check(matches(isDisplayed()))

        device.setOrientationRight()
        onView(isRoot()).perform(waitFor(200)) // a delay for rotation to finish
        onView(withId(R.id.citiesRV)).check(matches(isDisplayed()))
    }


    fun waitFor(delay: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isRoot()
            }

            override fun getDescription(): String {
                return "wait for " + delay + "milliseconds"
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(delay)
            }
        }
    }
}
