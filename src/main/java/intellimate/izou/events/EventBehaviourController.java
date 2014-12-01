package intellimate.izou.events;

import intellimate.izou.system.Identification;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * This class can control the Behaviour of the the Event, like which Output-Plugin should get the Event first.
 */
public class EventBehaviourController {
    public static final int MostPriority = 0;
    public static final int LeastPriority = 1;
    private final Event event;
    private Function<List<Identification>, HashMap<Integer, List<Identification>>> outputPluginBehaviour;
    protected EventBehaviourController(Event event) {
        this.event = event;
    }

    /**
     * this method sets the controls for the Output-Plugin Behaviour.
     * <p>
     * Supply a Function, which controls the OutputPlugin-Behaviour. You can set Priorities.
     * This function returns an HashMap, where the keys represent the associated Behaviour
     * (use the Constants) and the values the Identification;
     * </p>
     * @param outputPluginBehaviour all the registered outputPlugins
     */
    public void controlOutputPluginBehaviour(Function<List<Identification>,
            HashMap<Integer, List<Identification>>> outputPluginBehaviour) {
        this.outputPluginBehaviour = outputPluginBehaviour;
    }

    /**
     * generates the data to control the Event
     * @param identifications the Identifications of the OutputPlugins
     * @return a HashMap, where the keys represent the associated Behaviour and the values the Identification;
     */
    public HashMap<Integer, List<Identification>> getOutputPluginBehaviour(List<Identification> identifications) {
        return outputPluginBehaviour.apply(identifications);
    }

    public Event getEvent() {
        return event;
    }
}
