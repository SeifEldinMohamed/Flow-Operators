package com.seif.flowoperators

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    private lateinit var testDispatchers: TestDispatchers

    @Before
    fun setUp() {
        testDispatchers = TestDispatchers()
        viewModel = MainViewModel(testDispatchers)
    }

    @Test
    fun `countDownFlow, properly counts down from 5 to 0`() = runBlocking {
        // Terminal flow operator that collects events from given flow and allows the validate lambda to consume and assert properties on them in order
        viewModel.countDownFlow.test {
//            val emission = awaitItem()//5 // this now will suspend this block of code untils it gets an item here
//            awaitItem() // 4
            for (i in 5 downTo 0){
                // to skip the delay
                testDispatchers.testDispatcher.scheduler.apply {
                    advanceTimeBy(1000L) // Moves the virtual clock of this dispatcher forward by the specified amount, running the scheduled tasks in the meantime.
                    runCurrent() // Runs the tasks that are scheduled to execute at this moment of virtual time.
                }
                val emission = awaitItem()
                assertThat(emission).isEqualTo(i)
            }
            cancelAndConsumeRemainingEvents() // To make sure that we finish this test scope
        } // if there are more events coming in after the last one here then it will throw an exception
    }

    @Test
    fun `squareNumber, number properly squared`() = runBlocking {
        val job = launch {
            viewModel.sharedFlow.test {
                val emission = awaitItem()
                assertThat(emission).isEqualTo(9)
                cancelAndConsumeRemainingEvents()
            }
        }
        viewModel.squareNumber(3)
        job.join() // Suspends the coroutine until this job is complete. This invocation resumes normally (without exception) when the job is complete for any reason and the Job of the invoking coroutine is still active. This function also starts the corresponding coroutine if the Job was still in new state.
        job.cancel()
    }
}