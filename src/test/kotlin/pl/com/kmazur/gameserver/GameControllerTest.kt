package pl.com.kmazur.gameserver

import assertk.all
import assertk.assertThat
import assertk.assertions.containsAll
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.reflect.Type
import java.util.*


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [GameServerApplication::class])
class GameControllerTest(@Autowired private val testWebSocketStompClient: TestWebSocketStompClient, @Value("\${local.server.port}") private val port: Int) {

  private val serverUrl: String = "ws://localhost:$port/game"

  private val packageHandler = TestPackageHandler()

  @AfterEach
  internal fun tearDown() {
    packageHandler.clear()
  }

  @Test
  fun testCreateGameEndpoint() {
    val uuid = UUID.randomUUID().toString()

    val stompSession = testWebSocketStompClient.connect(serverUrl)

    stompSession.subscribe(SUBSCRIBE_MAP_ENDPOINT + uuid, packageHandler)
    stompSession.send(JOIN_GAME_ENDPOINT + uuid, "user-1")
    stompSession.send(JOIN_GAME_ENDPOINT + uuid, "user-2")

    await until { packageHandler.packages.size >= 2 }

    assertThat(packageHandler.packages)
      .containsAll(
        Package(uuid, "user-1"),
        Package(uuid, "user-2")
      )
  }

  companion object {
    private const val JOIN_GAME_ENDPOINT = "/app/join/"
    private const val SUBSCRIBE_MAP_ENDPOINT = "/topic/map/"
  }
}

class TestPackageHandler : StompFrameHandler {

  val packages: MutableList<Package> = mutableListOf()

  override fun getPayloadType(stompHeaders: StompHeaders): Type {
    return Package::class.java
  }

  override fun handleFrame(stompHeaders: StompHeaders, o: Any?) {
    packages.add(o as Package);
  }

  fun clear() = packages.clear()
}




