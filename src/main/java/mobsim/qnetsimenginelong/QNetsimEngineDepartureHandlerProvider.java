package mobsim.qnetsimenginelong;

import javax.inject.Inject;
import javax.inject.Provider;

class QNetsimEngineDepartureHandlerProvider implements Provider<VehicularDepartureHandler> {

    @Inject
    QNetsimEngine qNetsimEngine;

    @Override
    public VehicularDepartureHandler get() {
        return qNetsimEngine.getDepartureHandler();
    }
}
