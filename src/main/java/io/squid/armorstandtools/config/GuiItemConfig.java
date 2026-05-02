package io.squid.armorstandtools.config;

import java.util.List;

public class GuiItemConfig {

    public final List<Integer> slots;
    public final String        material;
    public final int           modelData;

    public GuiItemConfig(List<Integer> slots, String material, int modelData) {
        this.slots     = slots;
        this.material  = material;
        this.modelData = modelData;
    }
}
