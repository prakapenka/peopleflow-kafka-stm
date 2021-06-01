package localhost.config;

import localhost.data.Event;
import localhost.data.StateInfo;
import localhost.data.States;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Configuration
public class StateMachineConfig {

    /*
    Here we declare only one singleton state machine that will be reused for
    whole applications, using switching contexts. See code InMemoryStateMachinePersist below.
     */
    @Bean
    public StateMachine<States, Event> stateMachineTarget() throws Exception {
        StateMachineBuilder.Builder<States, Event> builder = StateMachineBuilder.builder();

        builder.configureConfiguration()
                .withConfiguration()
                .autoStartup(true);

        builder.configureStates()
                .withStates()
                .initial(States.NOT_EXISTED)
                .states(EnumSet.allOf(States.class));

        builder.configureTransitions()
                .withExternal()
                .source(States.NOT_EXISTED).target(States.ADDED).event(Event.CREATE)
                .and()
                .withExternal()
                .source(States.ADDED).target(States.IN_CHECK).event(Event.CHECK)
                .and()
                .withExternal()
                .source(States.IN_CHECK).target(States.APPROVED).event(Event.APPROVE)
                .and()
                .withExternal()
                .source(States.APPROVED).target(States.ACTIVE).event(Event.ACTIVATE);

        return builder.build();
    }

    /*
    Here we configure context persister to use in-memory simple Map implementation
     */
    @Bean
    public StateMachinePersister<States, Event, String> redisStateMachinePersister(
            StateMachinePersist<States, Event, String> stateMachinePersist) {
        return new DefaultStateMachinePersister<>(stateMachinePersist);
    }


    /*
    Note, here we declare this bean by InMemoryStateMachinePersist class, not by interface itself.
    This is done just for simplicity to read internal states in controller.
    For product-ready app state can be saved in DB or in-memory source.
     */
    @Bean
    public InMemoryStateMachinePersist stateMachinePersist() {
        return new InMemoryStateMachinePersist();
    }


    public static class InMemoryStateMachinePersist implements StateMachinePersist<States, Event, String> {

        private final Map<String, StateMachineContext<States, Event>> contexts = new ConcurrentHashMap<>();

        private final Predicate<StateInfo> notExisted =
                e -> e.getState().equals(States.NOT_EXISTED);

        @Override
        public void write(StateMachineContext<States, Event> context, String contextObj) {
            if (contextObj == null) {
                throw new RuntimeException("Attempt to same state machine with null context");
            }
            contexts.put(contextObj, context);
        }

        @Override
        public StateMachineContext<States, Event> read(String contextObj) {
            return contextObj == null ? null : contexts.get(contextObj);
        }

        public Stream<StateInfo> getInternalStates() {
            return contexts.entrySet().stream()
                    .map(entry -> new StateInfo()
                            .setEmail(entry.getKey())
                            .setState(entry.getValue().getState())
                    ).filter(notExisted.negate());
        }

        public Optional<StateInfo> getStateForEmail(final String email) {
            return Optional.ofNullable(contexts.get(email))
                    .map(c -> new StateInfo()
                            .setState(c.getState())
                            .setEmail(email)
                    );
        }
    }
}
