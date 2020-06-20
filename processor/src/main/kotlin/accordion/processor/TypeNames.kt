package accordion.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import reactor.core.publisher.Mono

object TypeNames {
    val discordClient = ClassName.bestGuess("discord4j.core.DiscordClient")
    val gateway = ClassName.bestGuess("discord4j.core.GatewayDiscordClient")
    val gatewayMono = ParameterizedTypeName.get(ClassName.get(Mono::class.java), gateway)
}