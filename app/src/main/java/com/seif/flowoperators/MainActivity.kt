package com.seif.flowoperators

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seif.flowoperators.ui.theme.FlowOperatorsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // usually we use collectLatest for uiState as we always want to show the latest state on ui to the user
        collectLatestLifecycleFlow(viewModel.stateFlow) { number ->
           // binding.tvNumber.text = number.toString()
        }
        collectLifecycleFlow(viewModel.stateFlow) { number ->
            // binding.tvNumber.text = number.toString()
        }

        setContent {
            FlowOperatorsTheme {
                val viewModel = viewModel<MainViewModel>()
                // val count = viewModel.countDownFlow.collectAsState(initial = 0)
                val count: State<Int> = viewModel.stateFlow.collectAsState(initial = 0)

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(onClick = {
                        viewModel.incrementCounter()
                    }) {
                        Text(text = "Counter: ${count.value}")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlowOperatorsTheme {
        Greeting("Android")
    }
}

// in xml will be AppCompatActivity
fun <T> ComponentActivity.collectLatestLifecycleFlow (flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED){
            flow.collectLatest(collect)
        }
    }
}

fun <T> ComponentActivity.collectLifecycleFlow (flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED){
            flow.collect {
                collect(it)
            }
        }
    }
}


// - repeatOnLifecycle: restarts its coroutine from scratch on each repeat,
// and cancels it each time lifecycle falls below the specified state.
// It’s a natural fit for collecting most flows, because it fully cancels
// the flow when it’s not needed, which saves resources related to the flow
// continuing to emit values.
//
// - launchWhenX: doesn’t cancel the coroutine and restart it. It just postpones when
// it starts, and pauses execution while below the specified state. They plan to
// deprecate these functions but I suspect there will need to be some replacement
// if they do, for the case where you are calling some time consuming suspend
// function and then want to do something when it’s done, like starting a
// fragment transaction. Using repeatOnLifecycle for this would result in redoing
// the time consuming action.


/**
 * launchWhenStarted is just a one-time delay.

 * repeatOnLifecycle creates a suspending point that acts as a handler that runs
 * provided block every time the lifecycle enters provided state and cancels
 * it whenever it falls below it (so for STARTED it happens when it gets stopped).
 **/