package DynAgent;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;

import java.util.Collection;
import java.util.Collections;

public class VrpAgentSourcePlugin extends AbstractQSimPlugin {
	public VrpAgentSourcePlugin(Config config) {
		super(config);
	}

	@Override
	public Collection<? extends Module> modules() {
		return Collections.singletonList(new AbstractModule() {
			@Override
			public void configure() {
				bind(VrpAgentSource.class).asEagerSingleton();
			}
		});
	}

	@Override
	public Collection<Class<? extends AgentSource>> agentSources() {
		return Collections.singletonList(VrpAgentSource.class);
	}
}
