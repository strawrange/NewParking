package Run.PrePostProcessing.Indicators;

import org.matsim.core.events.handler.EventHandler;

import java.io.IOException;

public interface IndicatorModule extends EventHandler {
    void output(String outputPath) throws IOException;
}
