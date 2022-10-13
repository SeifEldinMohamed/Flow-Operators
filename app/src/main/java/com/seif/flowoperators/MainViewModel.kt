package com.seif.flowoperators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
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
        collectFlow4()
    }

    private fun collectFlow() {
//        countDownFlow.onEach {
//            println(it)
//        }.launchIn(viewModelScope)

//        viewModelScope.launch {
//           val count: Int = countDownFlow
//                .filter { time ->
//                    time % 2 == 0 // receive even values
//                }
//                .map { time ->
//                    time * time
//                }
//                .onEach { time -> // it doesn't transform our values instead it does something with these
//                    println(time)
//                }.count{
//
//                    it % 2 ==0
//                }
//            println("The count is $count")
//        }
        viewModelScope.launch {
            val reduceResult = countDownFlow
                .reduce { accumulator, value ->
                    accumulator + value
                } // output = 15

            val foldResult = countDownFlow
                .fold(100) { accumulator, value ->
                    accumulator + value
                } // output = 115
        }
    }

    @FlowPreview
    private fun collectFlow2(){
        val flow1 = flow {
            emit(1)
            delay(500L)
            emit(2)
        }
        viewModelScope.launch {
            flow1.flatMapConcat { value ->
                flow {
                    emit(value + 1)
                    delay(500L)
                    emit(value + 2)
                }
            }.collect{ value ->
                println("the value is : $value")

            }
        }
    }

    @FlowPreview
    private fun collectFlow3(){
        val flow1 = (1..5).asFlow()
        viewModelScope.launch {
            flow1.flatMapConcat { id ->
           //  getRecipesId(id) // this function will accessed the cache and return a flow
                flow {
                    emit(id)
                }
            }.collect{ value ->
                println("the value is : $value")

            }
        }
    }

    private fun collectFlow4(){
        val flow = flow {
            delay(250L)
            emit("appetizer")
            delay(1000L)
            emit("main dish")
            delay(100L)
            emit("dessert")
        }
        viewModelScope.launch {
            flow.onEach {
                println("Flow: $it is delivered")
            }.buffer()
                .collect {
                println("Flow: now eating $it")
                delay(1500L)
                println("Flow: finish eating $it")
            }
        }
        // in this scenario the flow emits a value when finish eating is happened (when collect is finished)
        // we have 3 strategies in this situation:
        // 1) buffer(): will make sure that the collect is runs in different coroutine that flow does
        // 2) conflate(): Also run collect in a difference coroutine than flow, if there are 2 emissions from the flow that we can't collect yet when finishes we will skip to the latest one (skip main dish)
        // 3) collectLatest: only consider latest emission
    }

}
// collect: emit every change happened even it takes longer time than emition
// collectLatest: emit every change put if the work done in collector takes more time than emition then it will cancel this coroutine and collect the latest one
// this can be used in case of we update the ui then we need the latest update no matter if there is an older change not shown

// Terminal Flow Operators: it called terminal bec it terminates the flow
// reduce:  as we can accumulate all values in list (accumulator: at first will be the first value then it will be the addition of it and value, value: next emission)
// fold : same as reduce but it has initial value
// count:  it will count the values match a specific condition

// flattening: if we have list of lists then we want to have a single list of all entries
// [[1,2], [1,2,3]] -> [1,2,1,2,3]

// flatMapConcat: will finish this flow's emission and then actually go with id 2 finish id 2 emission and so on
// flatMapMerge: will do all that at the same time it won't wait the first emission to finish it will directly launch the flow for id 1,2,3 and so on
// flatMapLatest: it will drop the flow that working if another flow emits