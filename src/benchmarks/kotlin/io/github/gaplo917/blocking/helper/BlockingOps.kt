package io.github.gaplo917.blocking.helper

interface BlockingOps {
  val ioDelay: Long

  fun blockingIO() {
    Thread.sleep(ioDelay)
  }
}
