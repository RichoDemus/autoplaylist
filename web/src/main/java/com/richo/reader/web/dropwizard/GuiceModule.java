package com.richo.reader.web.dropwizard;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.richo.casinobots.api.Bot;
import com.richo.casinobots.bots.alwaysbetsmaxbot.AlwaysBetsMaxBot;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

public class GuiceModule extends AbstractModule {

    @Override
    public void configure() {
        //binds here
        bindBots();
    }

    private void bindBots() {
        //use cool reflection instead
        final Multibinder<Bot> filterBinder = Multibinder.newSetBinder(binder(), Bot.class);

        final List<Class<? extends Bot>> bots = Collections.singletonList(AlwaysBetsMaxBot.class);

        bots.stream()
                .filter(this::botImplementations)
                .forEach(filterClass -> filterBinder.addBinding().to(filterClass));
    }

    private boolean botImplementations(Class<? extends Bot> botClass) {
        return !Modifier.isAbstract(botClass.getModifiers()) && !Modifier.isInterface(botClass.getModifiers());
    }
}