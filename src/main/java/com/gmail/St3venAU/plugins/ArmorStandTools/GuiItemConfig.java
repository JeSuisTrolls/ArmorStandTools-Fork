package com.gmail.St3venAU.plugins.ArmorStandTools;

import java.util.List;

class GuiItemConfig {

    final List<Integer> slots;
    final String        material;
    final int           modelData;

    GuiItemConfig(List<Integer> slots, String material, int modelData) {
        this.slots     = slots;
        this.material  = material;
        this.modelData = modelData;
    }
}