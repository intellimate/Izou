package org.intellimate.izou.output;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.util.IdentifiableSet;

import java.util.Optional;

/**
 * The OutputControlManager controls all {@link OutputControllerModel}s in izou. In other words it offers a way to
 * interact with the {@link OutputControllerModel}s.
 *
 * @author Julian Brendl
 * @version 1.0
 */
public class OutputControllerManager {
    /**
     * This HashMap stores all added outputControllers
     */
    private IdentifiableSet<OutputControllerModel> outputControllers;

    /**
     * Creates a new OutputControllerManager
     */
    public OutputControllerManager() {
        this.outputControllers = new IdentifiableSet<>();
    }

    /**
     * Adds a new {@link OutputControllerModel} to Izou.
     *
     * @param outputController The OutputController to add to Izou.
     */
    public void addOutputController(OutputControllerModel outputController) {
        outputControllers.add(outputController);

    }

    /**
     * Removes an existing {@link OutputControllerModel} from Izou.
     *
     * @param outputController The OutputController to remove from to Izou.
     */
    public void removeOutputController(OutputControllerModel outputController) {
        outputControllers.remove(outputController);
    }

    /**
     * Returns a {@link Optional} object that may or may not contain the desired {@link OutputControllerModel},
     * depending on whether it was registered with Izou or not.
     *
     * @param identifiable The ID of the OutputController that should be retrieved.
     * @return The {@link Optional} object that may or may not contain the desired {@link OutputControllerModel},
     * depending on whether it was registered with Izou or not.
     */
    public Optional<OutputControllerModel> getOutputController(Identifiable identifiable) {
        return outputControllers.stream()
                .filter(outputController ->  outputController.getID().equals(identifiable.getID()))
                .findFirst();
    }
}
