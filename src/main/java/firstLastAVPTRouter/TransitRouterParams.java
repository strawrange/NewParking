package firstLastAVPTRouter;

import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

public class TransitRouterParams {
    final double marginalUtilityAV;
    final double marginalUtilityAVTaxi;
    final double avWaiting;
    final double initialCostAVTaxi;
    public TransitRouterParams(PlanCalcScoreConfigGroup planCalcScoreConfigGroup){
        this.marginalUtilityAV = planCalcScoreConfigGroup.getOrCreateModeParams("drt").getMarginalUtilityOfTraveling() /3600;
        this.marginalUtilityAVTaxi = planCalcScoreConfigGroup.getOrCreateModeParams("drtaxi").getMarginalUtilityOfTraveling()/3600;
        this.initialCostAVTaxi = -planCalcScoreConfigGroup.getOrCreateModeParams("drtaxi").getConstant();
        this.avWaiting = 5.0*60;
    }
}

