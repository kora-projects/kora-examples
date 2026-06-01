package ru.tinkoff.kora.kotlin.example.bpmn.camunda7

import org.camunda.bpm.engine.ArtifactFactory
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.IdGenerator
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.el.JuelExpressionManager
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import ru.tinkoff.kora.camunda.engine.bpmn.KoraResolverFactory
import ru.tinkoff.kora.test.extension.junit5.KoraAppGraph

class KoraAppTestInMemoryProcessEngineConfiguration(g: KoraAppGraph) : StandaloneInMemProcessEngineConfiguration() {
    init {
        databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP
        jdbcUrl = "jdbc:h2:mem:camunda;DB_CLOSE_ON_EXIT=FALSE"
        authorizationEnabled = false
        isJobExecutorActivate = true
        isHistoryCleanupEnabled = true
        isEnforceHistoryTimeToLive = true
        historyTimeToLive = "P1D"
        expressionManager = g.getFirst(JuelExpressionManager::class.java)
        artifactFactory = g.getFirst(ArtifactFactory::class.java)
        idGenerator = g.getFirst(IdGenerator::class.java)
        jobExecutor = g.getFirst(JobExecutor::class.java)
        if (resolverFactories == null) {
            resolverFactories = ArrayList()
        }
        resolverFactories.add(g.getFirst(KoraResolverFactory::class.java))
    }
}
