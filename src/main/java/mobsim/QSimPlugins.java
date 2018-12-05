package mobsim;

import com.google.inject.Inject;
import mobsim.qnetsimenginelong.QNetsimEnginePlugin;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.ActivityEnginePlugin;
import org.matsim.core.mobsim.qsim.PopulationPlugin;
import org.matsim.core.mobsim.qsim.TeleportationPlugin;
import org.matsim.core.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import org.matsim.core.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import org.matsim.core.mobsim.qsim.pt.TransitEnginePlugin;

import java.util.ArrayList;
import java.util.Collection;

public class QSimPlugins {
    private final Collection<AbstractQSimPlugin> plugins = new ArrayList<>();
    @Inject
    public QSimPlugins(Config config) {
        plugins.add(new MessageQueuePlugin(config));
        plugins.add(new ActivityEnginePlugin(config));
        plugins.add(new QNetsimEnginePlugin(config));
        if (config.network().isTimeVariantNetwork()) {
            plugins.add(new NetworkChangeEventsPlugin(config));
        }
        if (config.transit().isUseTransit()) {
            plugins.add(new TransitEnginePlugin(config));
        }
        plugins.add(new TeleportationPlugin(config));
        plugins.add(new PopulationPlugin(config));
    }

    public Collection<AbstractQSimPlugin> getPlugins() {
        return plugins;
    }
}
