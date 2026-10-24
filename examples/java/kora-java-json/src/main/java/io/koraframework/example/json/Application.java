package io.koraframework.example.json;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.json.common.JsonModule;

@KoraApp
public interface Application extends JsonModule {
    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
