package pl.com.kmazur.gameserver

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.util.concurrent.TimeUnit

@Configuration
class TestConfig {
  @Bean
  fun testWebSocketStompClient(@Autowired objectMapper: ObjectMapper) = TestWebSocketStompClient(objectMapper)
}

class TestWebSocketStompClient(private val objectMapper: ObjectMapper) {

  private val webSocketStompClient = WebSocketStompClient(SockJsClient(createTransportClient())).also {
    it.messageConverter = MappingJackson2MessageConverter()
      .also { mappingJackson2MessageConverter -> mappingJackson2MessageConverter.objectMapper = objectMapper }
  }

  fun connect(url: String): StompSession = webSocketStompClient.connect(url, object : StompSessionHandlerAdapter() {
    override fun handleException(session: StompSession, command: StompCommand?, headers: StompHeaders, payload: ByteArray, exception: Throwable) {
      AssertionError(exception.message, exception)
    }
  }).get(1, TimeUnit.SECONDS)

  private fun createTransportClient(): List<Transport> {
    return listOf(WebSocketTransport(StandardWebSocketClient()))
  }
}
