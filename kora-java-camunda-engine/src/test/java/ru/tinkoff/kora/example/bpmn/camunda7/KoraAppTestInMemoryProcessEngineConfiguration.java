package ru.tinkoff.kora.example.bpmn.camunda7;

import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import ru.tinkoff.kora.camunda.engine.KoraResolverFactory;
import ru.tinkoff.kora.test.extension.junit5.KoraAppGraph;

import java.util.ArrayList;

public class KoraAppTestInMemoryProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {

    public KoraAppTestInMemoryProcessEngineConfiguration(KoraAppGraph g) {
        setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        setJdbcUrl("jdbc:h2:mem:camunda;DB_CLOSE_ON_EXIT=FALSE");
        setAuthorizationEnabled(false);
        setJobExecutorActivate(true);
        setHistoryCleanupEnabled(true);
        setEnforceHistoryTimeToLive(true);
        setHistoryTimeToLive("P1D");
        setExpressionManager(g.getFirst(JuelExpressionManager.class));
        setArtifactFactory(g.getFirst(ArtifactFactory.class));
        setIdGenerator(g.getFirst(IdGenerator.class));
        setJobExecutor(g.getFirst(JobExecutor.class));
        if (getResolverFactories() == null) {
            setResolverFactories(new ArrayList<>());
        }
        getResolverFactories().add(g.getFirst(KoraResolverFactory.class));
    }
}
