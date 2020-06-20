package accordion.test

import accordion.annotation.Command
import accordion.annotation.DiscordBot
import org.junit.jupiter.api.Test

class BasicGenerationTests {
    @DiscordBot
    class Example {
        @Command
        fun ping() = "pong"
    }

    @Test
    fun `Should generate basic test`() {
        ExampleDiscordBot.create("test")
    }
}