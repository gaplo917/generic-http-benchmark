package io.github.gaplo917.springmvc

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SpringMVCApplicationTests {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test
  @Throws(Exception::class)
  fun endpointShouldReady() {
    listOf(
      "/mvc-bio/0",
      "/mvc-bio-future-in-vt/0",
      "/mvc-bio-structured-concurrency-parallel/0",
      "/mvc-nio-future/0",
      "/mvc-nio-future-structured-concurrency-parallel/0",
      "/mvc-bio-coroutine-in-vt/0",
      "/mvc-bio-coroutine-parallel/0",
      "/mvc-nio-coroutine/0",
      "/mvc-nio-coroutine-parallel/0",
      "/actuator"
    ).forEach {
      mockMvc.perform(
        get(it)
      ).andExpect(status().isOk)
    }
  }
}
