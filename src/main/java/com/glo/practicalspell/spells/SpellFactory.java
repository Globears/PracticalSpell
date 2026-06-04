package com.glo.practicalspell.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Given a spell name, returns a Spell instance to invoke. */
public class SpellFactory {

    private static final Map<String, Supplier<Spell>> spells = new HashMap<>();

    static {
        // stateless spells: same instance every time
        register("circle", CircleSpell::new);
        register("triangle", TriangleSpell::new);
        register("star", StarSpell::new);
        register("rectangle", TntSpell::new);
        register("pigtail", JetSpell::new);
        // later: register("poison", () -> POISON_SINGLETON);
        // stateful spells: fresh instance per cast
        // later: register("shield", ShieldSpell::new);
    }

    public static void register(String gestureName, Supplier<Spell> supplier) {
        spells.put(gestureName, supplier);
    }

    public static Spell create(String name) {
        Supplier<Spell> supplier = spells.get(name);
        return supplier != null ? supplier.get() : null;
    }
}
