package li.cil.scannable.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import li.cil.scannable.util.config.*;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.MapColor;

@Type(ConfigType.CLIENT)
public final class ClientConfig {
    @WorldRestart
    @Comment("""
        The colors for blocks used when rendering their result bounding box
        by block name. Each entry must be a key-value pair separated by a `=`,
        with the key being the tag name and the value being the hexadecimal
        RGB value of the color.""")
    @KeyValueTypes(keyType = ResourceLocation.class, valueType = int.class,
        valueSerializer = @CustomSerializer(serializer = "toHexString", deserializer = "fromHexString"))
    public static Object2IntMap<ResourceLocation> blockColors = new Object2IntOpenHashMap<>();

    @WorldRestart
    @Comment("The colors for blocks used when rendering their result bounding box\n" +
        "by block tag. See `blockColors` for format entries have to be in.")
    @KeyValueTypes(keyType = ResourceLocation.class, valueType = int.class,
        valueSerializer = @CustomSerializer(serializer = "toHexString", deserializer = "fromHexString"))
    public static Object2IntMap<ResourceLocation> blockTagColors = Util.make(new Object2IntOpenHashMap<>(), c -> {
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/coal"), MapColor.COLOR_GRAY.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/iron"), MapColor.COLOR_BROWN.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/gold"), MapColor.GOLD.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/lapis"), MapColor.LAPIS.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/diamond"), MapColor.DIAMOND.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/redstone"), MapColor.COLOR_RED.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/emerald"), MapColor.EMERALD.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/quartz"), MapColor.QUARTZ.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/tin"), MapColor.COLOR_CYAN.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/copper"), MapColor.TERRACOTTA_ORANGE.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/lead"), MapColor.TERRACOTTA_BLUE.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/silver"), MapColor.COLOR_LIGHT_GRAY.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/nickel"), MapColor.COLOR_LIGHT_BLUE.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/platinum"), MapColor.TERRACOTTA_WHITE.col);
        c.put(ResourceLocation.fromNamespaceAndPath("c", "ores/mithril"), MapColor.COLOR_PURPLE.col);
    });

    @WorldRestart
    @Comment("The colors for fluids used when rendering their result bounding box\n" +
        "by fluid name. See `blockColors` for format entries have to be in.")
    @KeyValueTypes(keyType = ResourceLocation.class, valueType = int.class,
        valueSerializer = @CustomSerializer(serializer = "toHexString", deserializer = "fromHexString"))
    public static Object2IntMap<ResourceLocation> fluidColors = new Object2IntOpenHashMap<>();

    @WorldRestart
    @Comment("The colors for fluids used when rendering their result bounding box\n" +
        "by fluid tag. See `blockColors` for format entries have to be in.")
    @KeyValueTypes(keyType = ResourceLocation.class, valueType = int.class,
        valueSerializer = @CustomSerializer(serializer = "toHexString", deserializer = "fromHexString"))
    public static Object2IntMap<ResourceLocation> fluidTagColors = Util.make(new Object2IntOpenHashMap<>(), c -> {
        c.put(FluidTags.WATER.location(), MapColor.WATER.col);
        c.put(FluidTags.LAVA.location(), MapColor.TERRACOTTA_ORANGE.col);
    });

    @SuppressWarnings("unused")
    public static String toHexString(final Object value) {
        return "0x" + Integer.toHexString((int) value);
    }

    @SuppressWarnings("unused")
    public static Object fromHexString(final String value) {
        return Integer.decode(value);
    }
}
