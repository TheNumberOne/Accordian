package accordion.test

import accordion.annotation.DiscordBot
import org.junit.jupiter.api.Test

class BasicGenerationTests {
    @DiscordBot
    class Example

    @Test
    fun `Should generate basic test`() {
        ExampleDiscordBot.start(System.getenv("token"))
    }
}