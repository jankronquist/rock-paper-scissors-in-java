package com.jayway.rps.app;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.es.impl.ApplicationService;
import com.jayway.es.impl.ReflectionUtil;
import com.jayway.es.store.eventstore.EventStoreEventStore;
import com.jayway.rps.domain.game.Game;
import com.jayway.rps.domain.game.GamesProjection;
import com.jayway.rps.infra.rest.HandleAllExceptions;
import com.jayway.rps.infra.rest.RpsResource;

public class RpsConfig extends ResourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(RpsConfig.class);

    private ObjectMapper mapper;

    public RpsConfig() throws Exception {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        
		GamesProjection gameProjection = new GamesProjection();
        EventStoreEventStore eventStore = new EventStoreEventStore("game", mapper);
		ApplicationService applicationService = new ApplicationService(eventStore, Game.class);
		eventStore.all().collect(()-> gameProjection, ReflectionUtil::invokeHandleMethod);
        
        register(new RpsResource(applicationService, gameProjection));
        register(new HandleAllExceptions());
    }
}
