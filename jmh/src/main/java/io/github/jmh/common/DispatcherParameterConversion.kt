package io.github.jmh.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

interface DispatcherParameterConversion {

  fun convertDispatcherParam(dispatcher: String): CoroutineDispatcher {
    return when (dispatcher) {
      "Unconfined" -> Dispatchers.Unconfined
      "IO" -> Dispatchers.IO
      "Default" -> Dispatchers.Default
      "SingleThread" -> newSingleThreadContext("coroutine-single-thread")
      "TwoThread" -> newFixedThreadPoolContext(2, "coroutine-single-thread")
      else -> throw RuntimeException("no dispatcher arguments matched")
    }
  }
}
