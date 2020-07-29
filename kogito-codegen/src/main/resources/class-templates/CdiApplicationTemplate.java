package $Package$;

import org.kie.kogito.Config;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.process.Processes;
import org.kie.kogito.rules.RuleUnits;

@javax.inject.Singleton
public class Application extends org.kie.kogito.AbstractApplication {

    @javax.inject.Inject
    public Application(
            Config config,
            javax.enterprise.inject.Instance<Processes> processes/*,
            javax.enterprise.inject.Instance<RuleUnits> ruleUnits,
            javax.enterprise.inject.Instance<DecisionModels> decisionModels*/) {
        this.config = config;
        this.processes = orNull(processes);
        this.ruleUnits = null /* $RuleUnits$ */;
        this.decisionModels = null /* $DecisionModels$ */;

        if (config().process() != null) {
            unitOfWorkManager().eventManager().setAddons(config().addons());
        }
    }

    private static <T> T orNull(javax.enterprise.inject.Instance<T> instance) {
        if (instance.isUnsatisfied()) {
            return null;
        } else {
            return instance.get();
        }
    }

}
