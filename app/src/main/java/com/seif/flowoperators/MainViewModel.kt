package com.seif.flowoperators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {

    val countDownFlow = flow<Int> {
        val startingValue = 10
        var currentValue = startingValue
        emit(startingValue)
        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    init {
        collectFlow()
    }

    private fun collectFlow() {
//        countDownFlow.onEach {
//            println(it)
//        }.launchIn(viewModelScope)

        viewModelScope.launch {
            countDownFlow
                .filter { time ->
                    time % 2 == 0 // receive even values
                }
                .map { time ->
                    time * time
                }
                .onEach { time -> // it doesn't transform our values instead it does something with these
                    println(time)
                } // returns flow
                .collect() { time ->
                println("the current time is $time")
            }
        }
    }

}
// collect: emit every change happened even it takes longer time than emition
// collectLatest: emit every change put if the work done in collector takes more time than emition then it will cancel this coroutine and collect the latest one
// this can be used in case of we update the ui then we need the latest update no matter if there is an older change not shown