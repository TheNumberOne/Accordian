package accordian.internal;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class StaticStringPrefixExtractor implements PrefixExtractor {
    private final String prefix;

    public StaticStringPrefixExtractor(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Mono<String> removePrefix(MessageCreateEvent messageCreateEvent) {
        String content = messageCreateEvent.getMessage().getContent();
        if (content.startsWith(prefix)) {
            return Mono.just(content.substring(prefix.length()));
        } else {
            return Mono.empty();
        }
    }
}
