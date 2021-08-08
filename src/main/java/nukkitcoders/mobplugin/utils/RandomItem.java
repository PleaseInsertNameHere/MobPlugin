package nukkitcoders.mobplugin.utils;

import cn.nukkit.item.randomitem.Selector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RandomItem {
    public static final Selector ROOT = new Selector(null);
    private static final Map<Selector, Float> selectors = new HashMap<>();

    public static Selector putSelector(Selector selector) {
        return putSelector(selector, 1);
    }

    public static Selector putSelector(Selector selector, float chance) {
        if (selector.getParent() == null) selector.setParent(ROOT);
        selectors.put(selector, chance);
        return selector;
    }

    static Object selectFrom(Selector selector) {
        Objects.requireNonNull(selector);
        Map<Selector, Float> child = new HashMap<>();
        selectors.forEach((s, f) -> {
            if (s.getParent() == selector) child.put(s, f);
        });
        if (child.size() == 0) return selector.select();
        return selectFrom(Selector.selectRandom(child));
    }
}
