package pl.com.kmazur.gameserver

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.stereotype.Controller
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import kotlin.reflect.full.cast


@SpringBootApplication
class GameServerApplication

fun main(args: Array<String>) {
  runApplication<GameServerApplication>(*args)
}

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

  override fun configureMessageBroker(config: MessageBrokerRegistry) {
    config.enableSimpleBroker("/topic")
    config.setApplicationDestinationPrefixes("/app")
  }

  override fun registerStompEndpoints(stompEndpointRegistry: StompEndpointRegistry) {
    stompEndpointRegistry.addEndpoint("/game").withSockJS()
  }

  @Bean
  @Primary
  fun objectMapper(@Autowired builder: Jackson2ObjectMapperBuilder): ObjectMapper {
    val objectMapper = builder.createXmlMapper(false).build<ObjectMapper>()

    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY)
    objectMapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
    objectMapper.registerModule(KotlinModule())

    return objectMapper
  }

}

@Controller
class GameController {

  @MessageMapping("/join/{uuid}")
  @SendTo("/topic/map/{uuid}")
  fun joinGame(@DestinationVariable("uuid") uuid: String, @Payload payload: String): Package {
    return Package(uuid, payload)
  }
}

data class Package(val mapUuid: String, val content: String)
