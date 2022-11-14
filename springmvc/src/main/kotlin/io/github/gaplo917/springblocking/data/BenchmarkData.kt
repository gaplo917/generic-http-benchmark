package io.github.gaplo917.springblocking.data

data class DummyResponse(
  val name: String,
  val isActive: Boolean,
  val isVirtual: Boolean,
  val threadGroupActiveCount: Int,
  val threadGroupCount: Int
) {
  companion object {
    fun dummy(thread: Thread = Thread.currentThread()): DummyResponse {
      return DummyResponse(
        name = thread.name,
        isActive = thread.isAlive,
        isVirtual = thread.isVirtual,
        threadGroupActiveCount = thread.threadGroup.activeCount(),
        threadGroupCount = thread.threadGroup.activeGroupCount()
      )
    }
  }
}
