package mobsim;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import org.apache.log4j.Logger;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;

import javax.inject.Inject;
import java.util.Collection;

public class QSimProvider implements Provider<QSim> {
    private static final Logger log = Logger.getLogger( org.matsim.core.mobsim.qsim.QSimProvider.class ) ;

    private Injector injector;
    private Collection<AbstractQSimPlugin> plugins;

    @Inject
    public QSimProvider(Injector injector, QSimPlugins plugins) {
        this.injector = injector;
        this.plugins = plugins.getPlugins();
    }

    @Override
    public QSim get() {
        com.google.inject.AbstractModule module = new com.google.inject.AbstractModule() {
            @Override
            protected void configure() {
                for (AbstractQSimPlugin plugin : plugins) {
                    // install each plugin's modules:
                    for (Module module1 : plugin.modules()) {
                        install(module1);
                    }
                }
                bind(QSim.class).asEagerSingleton();
                bind(Netsim.class).to(QSim.class);
            }
        };
        Injector qSimLocalInjector = injector.createChildInjector(module);
        org.matsim.core.controler.Injector.printInjector( qSimLocalInjector, log ) ;
        QSim qSim = qSimLocalInjector.getInstance(QSim.class);
//        qSim.setChildInjector( qSimLocalInjector ) ;
        for (AbstractQSimPlugin plugin : plugins) {
            // add each plugin's mobsim engines:
            for (Class<? extends MobsimEngine> mobsimEngine : plugin.engines()) {
                qSim.addMobsimEngine(qSimLocalInjector.getInstance(mobsimEngine));
            }
            // add each plugin's activity handlers:
            for (Class<? extends ActivityHandler> activityHandler : plugin.activityHandlers()) {
                qSim.addActivityHandler(qSimLocalInjector.getInstance(activityHandler));
            }
            // add each plugin's departure handlers:
            for (Class<? extends DepartureHandler> mobsimEngine : plugin.departureHandlers()) {
                qSim.addDepartureHandler(qSimLocalInjector.getInstance(mobsimEngine));
            }
            // add each plugin's mobsim listeners:
            for (Class<? extends MobsimListener> mobsimListener : plugin.listeners()) {
                qSim.addQueueSimulationListeners(qSimLocalInjector.getInstance(mobsimListener));
            }
            // add each plugin's agent sources:
            for (Class<? extends AgentSource> agentSource : plugin.agentSources()) {
                qSim.addAgentSource(qSimLocalInjector.getInstance(agentSource));
            }
        }
        return qSim;
    }

}