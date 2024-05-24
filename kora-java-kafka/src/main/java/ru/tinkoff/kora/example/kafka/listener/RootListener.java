package ru.tinkoff.kora.example.kafka.listener;

import ru.tinkoff.kora.common.Component;

@Component
public final class RootListener {

    private final AutoCommitRecordExceptionListener autoCommitRecordExceptionListener;
    private final AutoCommitRecordJsonListener autoCommitRecordJsonListener;
    private final AutoCommitRecordListener autoCommitRecordListener;
    private final AutoCommitRecordMapperListener autoCommitRecordMapperListener;
    private final AutoCommitRecordsListener autoCommitRecordsListener;
    private final AutoCommitRecordsTelemetryListener autoCommitRecordsTelemetryListener;
    private final AutoCommitValueExceptionListener autoCommitValueExceptionListener;
    private final AutoCommitValueJsonListener autoCommitValueJsonListener;
    private final AutoCommitValueKeyHeadersListener autoCommitValueKeyHeadersListener;
    private final AutoCommitValueKeyListener autoCommitValueKeyListener;
    private final AutoCommitValueListener autoCommitValueListener;
    private final AutoCommitValueMapperListener autoCommitValueMapperListener;
    private final ManualCommitRecordListener manualCommitRecordListener;

    public RootListener(AutoCommitRecordExceptionListener autoCommitRecordExceptionListener,
                        AutoCommitRecordJsonListener autoCommitRecordJsonListener,
                        AutoCommitRecordListener autoCommitRecordListener,
                        AutoCommitRecordMapperListener autoCommitRecordMapperListener,
                        AutoCommitRecordsListener autoCommitRecordsListener,
                        AutoCommitRecordsTelemetryListener autoCommitRecordsTelemetryListener,
                        AutoCommitValueExceptionListener autoCommitValueExceptionListener,
                        AutoCommitValueJsonListener autoCommitValueJsonListener,
                        AutoCommitValueKeyHeadersListener autoCommitValueKeyHeadersListener,
                        AutoCommitValueKeyListener autoCommitValueKeyListener,
                        AutoCommitValueListener autoCommitValueListener,
                        AutoCommitValueMapperListener autoCommitValueMapperListener,
                        ManualCommitRecordListener manualCommitRecordListener) {
        this.autoCommitRecordExceptionListener = autoCommitRecordExceptionListener;
        this.autoCommitRecordJsonListener = autoCommitRecordJsonListener;
        this.autoCommitRecordListener = autoCommitRecordListener;
        this.autoCommitRecordMapperListener = autoCommitRecordMapperListener;
        this.autoCommitRecordsListener = autoCommitRecordsListener;
        this.autoCommitRecordsTelemetryListener = autoCommitRecordsTelemetryListener;
        this.autoCommitValueExceptionListener = autoCommitValueExceptionListener;
        this.autoCommitValueJsonListener = autoCommitValueJsonListener;
        this.autoCommitValueKeyHeadersListener = autoCommitValueKeyHeadersListener;
        this.autoCommitValueKeyListener = autoCommitValueKeyListener;
        this.autoCommitValueListener = autoCommitValueListener;
        this.autoCommitValueMapperListener = autoCommitValueMapperListener;
        this.manualCommitRecordListener = manualCommitRecordListener;
    }
}
