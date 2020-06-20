package accordian.internal;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CommandHelper {
    public static Mono<Void> respondToMessageEvent(MessageCreateEvent message, String response) {
        return message
                .getMessage()
                .getChannel()
                .flatMap(channel -> channel.createMessage(response))
                .then();
    }
}
