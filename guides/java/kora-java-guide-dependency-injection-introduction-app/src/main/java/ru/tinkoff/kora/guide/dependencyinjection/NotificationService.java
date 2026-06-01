package ru.tinkoff.kora.guide.dependencyinjection;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import ru.tinkoff.kora.application.graph.All;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class NotificationService {

    private final All<Notifier> notifiers;
    private final Notifier emailNotifier;
    @Nullable
    private final AuditSink auditSink;

    public NotificationService(
            @Tag(Tag.Any.class) All<Notifier> notifiers,
            @Tag(EmailTag.class) Notifier emailNotifier,
            @Nullable AuditSink auditSink) {
        this.notifiers = notifiers;
        this.emailNotifier = emailNotifier;
        this.auditSink = auditSink;
    }

    public List<String> broadcast(String message) {
        var result = new ArrayList<String>();
        for (var notifier : this.notifiers) {
            var output = notifier.notifyUser(message);
            result.add(output);
            if (this.auditSink != null) {
                this.auditSink.record(notifier.channel(), output);
            }
        }
        return result;
    }

    public String notifyEmailOnly(String message) {
        var output = this.emailNotifier.notifyUser(message);
        if (this.auditSink != null) {
            this.auditSink.record(this.emailNotifier.channel(), output);
        }
        return output;
    }

    public boolean isAuditEnabled() {
        return this.auditSink != null;
    }
}