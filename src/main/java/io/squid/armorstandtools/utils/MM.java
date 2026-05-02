package io.squid.armorstandtools.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MM {

    private static MiniMessage MINI = MiniMessage.miniMessage();

    public static void init(TagResolver extraTags) {
        MINI = MiniMessage.builder()
                .tags(TagResolver.resolver(TagResolver.standard(), extraTags))
                .build();
    }

    public static Component parse(String s) {
        if (s == null || s.isEmpty()) return Component.empty();
        return MINI.deserialize(s);
    }

    public static String serialize(Component c) {
        if (c == null) return "";
        return MINI.serialize(c);
    }
}