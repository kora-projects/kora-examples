package ru.tinkoff.kora.example.kafka.listener;

import ru.tinkoff.kora.common.Component;

@Component
public final class RootListener {

    private final AutoCommitRecordExceptionListener autoCommitRecordExceptionListener;
    private final AutoCommitRecordJsonListener autoCommitRecordJsonListener;
    private final AutoCommitRecordListener autoCommitRecordListener;
    private final AutoCommitRecordMapperListener autoCommitRecordMapperListener;
    private final AutoCommitRecordsListener autoCommitRecordsListener;
    private final AutoCommitTopicExceptionListener autoCommitTopicExceptionListener;
    private final AutoCommitTopicJsonListener autoCommitTopicJsonListener;
    private final AutoCommitTopicKeyHeadersListener autoCommitTopicKeyHeadersListener;
    private final AutoCommitTopicKeyListener autoCommitTopicKeyListener;
    private final AutoCommitTopicListener autoCommitTopicListener;
    private final AutoCommitTopicMapperListener autoCommitTopicMapperListener;
    private final ManualCommitRecordListener manualCommitRecordListener;

    public RootListener(AutoCommitRecordExceptionListener autoCommitRecordExceptionListener,
                        AutoCommitRecordJsonListener autoCommitRecordJsonListener,
                        AutoCommitRecordListener autoCommitRecordListener,
                        AutoCommitRecordMapperListener autoCommitRecordMapperListener,
                        AutoCommitRecordsListener autoCommitRecordsListener1,
                        AutoCommitTopicExceptionListener autoCommitTopicExceptionListener,
                        AutoCommitTopicJsonListener autoCommitTopicJsonListener,
                        AutoCommitTopicKeyHeadersListener autoCommitTopicKeyHeadersListener,
                        AutoCommitTopicKeyListener autoCommitTopicKeyListener,
                        AutoCommitTopicListener autoCommitTopicListener,
                        AutoCommitTopicMapperListener autoCommitTopicMapperListener,
                        ManualCommitRecordListener manualCommitRecordListener) {
        this.autoCommitRecordExceptionListener = autoCommitRecordExceptionListener;
        this.autoCommitRecordJsonListener = autoCommitRecordJsonListener;
        this.autoCommitRecordListener = autoCommitRecordListener;
        this.autoCommitRecordMapperListener = autoCommitRecordMapperListener;
        this.autoCommitRecordsListener = autoCommitRecordsListener1;
        this.autoCommitTopicExceptionListener = autoCommitTopicExceptionListener;
        this.autoCommitTopicJsonListener = autoCommitTopicJsonListener;
        this.autoCommitTopicKeyHeadersListener = autoCommitTopicKeyHeadersListener;
        this.autoCommitTopicKeyListener = autoCommitTopicKeyListener;
        this.autoCommitTopicListener = autoCommitTopicListener;
        this.autoCommitTopicMapperListener = autoCommitTopicMapperListener;
        this.manualCommitRecordListener = manualCommitRecordListener;
    }
}
