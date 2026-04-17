package com.gmail.St3venAU.plugins.ArmorStandTools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

class MM {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    static Component parse(String s) {
        if (s == null || s.isEmpty()) return Component.empty();
        return MINI.deserialize(s);
    }

    static String serialize(Component c) {
        if (c == null) return "";
        return MINI.serialize(c);
    }

}