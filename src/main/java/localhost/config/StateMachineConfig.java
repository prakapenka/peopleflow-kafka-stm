package localhost.config;

import localhost.data.Event;
import localhost.data.StateInfo;
import localhost.data.States;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Configuration
public class StateMachineConfig {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ProxyFactoryBean stateMachine() {
        ProxyFactoryBean pfb = new ProxyFactoryBean();
        pfb.setTargetSource(poolTargetSource());
        return pfb;
    }

    @Bean
    public CommonsPool2TargetSource poolTargetSource() {
        CommonsPool2TargetSource pool = new CommonsPool2TargetSource();
        pool.setMaxSize(3);
        pool.setTargetBeanName("stateMachineTarget");
        return pool;
    }

    @Bean(name = "stateMachineTarget")
    @Scope(scopeName = "prototype")
    public StateMachine<States, Event> stateMachineTarget() throws Exception {
        StateMachineBuilder.Builder<States, Event> builder = StateMachineBuilder.builder();

        builder.configureConfiguration()
                .withConfiguration()
                .autoStartup(true);

        builder.configureStates()
                .withStates()
                .initial(States.ADDED)
                .states(EnumSet.allOf(States.class));

        builder.configureTransitions()
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

    @Bean
    public StateMachinePersister<States, Event, String> redisStateMachinePersister(
            StateMachinePersist<States, Event, String> stateMachinePersist) {
        return new DefaultStateMachinePersister<>(stateMachinePersist);
    }


    /*
    Note, here we declare this bean by InMemoryStateMachinePersist class, not by interface itself.
    This is done just for simplicity to read internal states in controller.
     */
    @Bean
    public InMemoryStateMachinePersist<States, Event, String> stateMachinePersist() {
        return new InMemoryStateMachinePersist<>();
    }

    public static class InMemoryStateMachinePersist<A, B, C> implements StateMachinePersist<States, Event, String> {

        private final Map<String, StateMachineContext<States, Event>> contexts = new ConcurrentHashMap<>();

        @Override
        public void write(StateMachineContext<States, Event> context, String contextObj) {
            contexts.put(contextObj, context);
        }

        @Override
        public StateMachineContext<States, Event> read(String contextObj) {
            return contexts.get(contextObj);
        }

        public Stream<StateInfo> getInternalStates() {
            return contexts.entrySet().stream()
                    .map(entry -> new StateInfo()
                            .setEmail(entry.getKey())
                            .setState(entry.getValue().getState())
                    );
        }
    }
}
