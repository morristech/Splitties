/*
 * Copyright (c) 2018. Louis Cognault Ayeva Derman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package splitties.lifecycle.coroutines

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/**
 * This function returns/resumes as soon as the state of this [Lifecycle] is at least the
 * passed [state].
 *
 * [Lifecycle.State.DESTROYED] is forbidden, to avoid leaks.
 */
@PotentialFutureAndroidXLifecycleKtxApi
@UseExperimental(ExperimentalCoroutinesApi::class)
suspend fun Lifecycle.awaitState(state: Lifecycle.State) {
    require(state != Lifecycle.State.DESTROYED) {
        "DESTROYED is a terminal state that is forbidden for awaitState(…), to avoid leaks."
    }
    if (currentState >= state) return // Fast path
    @UseExperimental(MainDispatcherPerformanceIssueWorkaround::class)
    withContext(Dispatchers.MainAndroid.immediate) {
        if (currentState == Lifecycle.State.DESTROYED) { // Fast path to cancellation
            cancel()
        } else suspendCancellableCoroutine<Unit> { c ->
            val observer = object : GenericLifecycleObserver {
                override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event) {
                    if (currentState >= state) {
                        removeObserver(this)
                        c.resume(Unit)
                    } else if (currentState == Lifecycle.State.DESTROYED) {
                        c.cancel()
                    }
                }
            }
            addObserver(observer)
            c.invokeOnCancellation { removeObserver(observer) }
        }
    }
}
