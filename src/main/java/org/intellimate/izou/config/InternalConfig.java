package org.intellimate.izou.config;

import org.intellimate.server.proto.IzouInstanceStatus;

import java.util.ArrayList;

/**
 * @author LeanderK
 * @version 1.0
 */
public class InternalConfig {
    public ArrayList<AddOn> addOns;
    public String state = IzouInstanceStatus.Status.RUNNING.name();

    public InternalConfig() {
    }

    public InternalConfig(ArrayList<AddOn> addOns, IzouInstanceStatus.Status state) {
        this.addOns = addOns;
        this.state = state.name();
    }

    private InternalConfig(ArrayList<AddOn> addOns, String state) {
        this.addOns = addOns;
        this.state = state;
    }

    public InternalConfig createNew(ArrayList<AddOn> selected) {
        return new InternalConfig(selected, state);
    }

    public InternalConfig createNew(IzouInstanceStatus.Status status) {
        return new InternalConfig(addOns, status);
    }
}
