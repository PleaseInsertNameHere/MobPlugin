package nukkitcoders.mobplugin.utils;

import cn.nukkit.item.Item;
import cn.nukkit.item.MinecraftItemID;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.randomitem.ConstantItemSelector;
import cn.nukkit.item.randomitem.ConstantItemWithRandomEnchantmentSelector;
import cn.nukkit.item.randomitem.Selector;

import static nukkitcoders.mobplugin.utils.RandomItem.*;


public class PiglinDrops {
    public static final Selector ROOT_TRADING = putSelector(new Selector(ROOT));
    public static final Selector GRAVEL = putSelector(new ConstantItemSelector(MinecraftItemID.GRAVEL.get(1), 8, 16, ROOT_TRADING), 0.077f);
    public static final Selector BLACKSTONE = putSelector(new ConstantItemSelector(MinecraftItemID.BLACKSTONE.get(1), 8, 16, ROOT_TRADING), 0.077f);
    public static final Selector ARROW = putSelector(new ConstantItemSelector(MinecraftItemID.ARROW.get(1), 6, 12, ROOT_TRADING), 0.087f);
    public static final Selector SOULSAND = putSelector(new ConstantItemSelector(MinecraftItemID.SOUL_SAND.get(1), 2, 8, ROOT_TRADING), 0.087f);
    public static final Selector NETHER_BRICK = putSelector(new ConstantItemSelector(Item.get(Item.BRICK, 0, 1), 2, 8, ROOT_TRADING), 0.087f);
    public static final Selector LEATHER = putSelector(new ConstantItemSelector(MinecraftItemID.LEATHER.get(1), 2, 4, ROOT_TRADING), 0.087f);
    // public static final Selector CRYING_OBSIDIAN = putSelector(new ConstantItemSelector(MinecraftItemID.CRYING_OBSIDIAN.get(1), 1, 3, ROOT_TRADING), 0.087f);
    public static final Selector OBSIDIAN = putSelector(new ConstantItemSelector(MinecraftItemID.OBSIDIAN.get(1), ROOT_TRADING), 0.087f);
    public static final Selector FIRE_CHARGE = putSelector(new ConstantItemSelector(MinecraftItemID.FIRE_CHARGE.get(1), ROOT_TRADING), 0.087f);
    public static final Selector NETHER_QUARZ = putSelector(new ConstantItemSelector(MinecraftItemID.QUARTZ.get(1), 5, 12, ROOT_TRADING), 0.043f);
    public static final Selector STRING = putSelector(new ConstantItemSelector(MinecraftItemID.STRING.get(1), 3, 9, ROOT_TRADING), 0.043f);
    public static final Selector ENDER_PEARL = putSelector(new ConstantItemSelector(MinecraftItemID.ENDER_PEARL.get(1), 2, 4, ROOT_TRADING), 0.043f);
    public static final Selector IRON_NUGGET = putSelector(new ConstantItemSelector(MinecraftItemID.IRON_NUGGET.get(1), 10, 36, ROOT_TRADING), 0.021f);
    public static final Selector FIRE_RESITANCE_POTION_TROWABLE = putSelector(new ConstantItemSelector(Item.POTION, 13, 1, ROOT_TRADING), 0.021f);
    public static final Selector FIRE_RESITANCE_POTION = putSelector(new ConstantItemSelector(Item.get(Item.SPLASH_POTION, 13, 1), ROOT_TRADING), 0.021f);
    public static final Selector WATER_BOTTLE = putSelector(new ConstantItemSelector(MinecraftItemID.IRON_NUGGET.get(1), ROOT_TRADING), 0.021f);
    public static final Selector ENCHANTED_IRON_BOOTS = putSelector(new ConstantItemWithRandomEnchantmentSelector(MinecraftItemID.IRON_BOOTS.get(1), new Enchantment[]{Enchantment.getEnchantment(Enchantment.ID_SOUL_SPEED)}, 1, false, false, ROOT_TRADING), 0.017f);
    public static final Selector ENCHANTED_BOOK;

    static {
        Enchantment[] enchantments = new Enchantment[34];
        for (int i = 0; i < enchantments.length; i++) {
            if (i == 33) enchantments[i] = Enchantment.getEnchantment(36);
            enchantments[i] = Enchantment.getEnchantment(i);
        }
        ENCHANTED_BOOK = putSelector(new ConstantItemWithRandomEnchantmentSelector(Item.ENCHANTED_BOOK, enchantments, 1, false, ROOT_TRADING), 0.01f);
    }

    public static Item getResult() {
        Object result = selectFrom(ROOT_TRADING);
        if (result instanceof Item) {
            return (Item) result;
        }
        return null;
    }
}
