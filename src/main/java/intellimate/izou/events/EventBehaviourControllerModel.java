package intellimate.izou.events;

import intellimate.izou.identification.Identification;

import java.util.HashMap;
import java.util.List;

/**
 * The Interface used to control the EventBehaviour
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface EventBehaviourControllerModel {
    /**
     * generates the data to control the Event
     * <p>
     * The Identifications with the highest Integer get the priority.
     * </p> 
     * @param identifications the Identifications of the OutputPlugins
     * @return a HashMap, where the keys represent the associated Behaviour and the values the Identification;
     */
    HashMap<Integer, List<Identification>> getOutputPluginBehaviour(List<Identification> identifications);
}
