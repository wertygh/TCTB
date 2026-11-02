package net.wertygh.tctb.bridge.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolPartsHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TConSlotMapping {
    public static final String HEAD_SINGLE = "single/head";
    public static final String HEAD_SWORD = "sword/blade";
    public static final String HEAD_DOUBLE_LEFT = "double/head_left";
    public static final String HEAD_DOUBLE_RIGHT = "double/head_right";
    public static final String HANDLE_SINGLE = "single/handle";
    public static final String HANDLE_DOUBLE = "double/handle";
    public static final String HANDLE_SWORD = "sword/hilt";
    public static final String BINDING_SINGLE = "single/binding";
    public static final String BINDING_DOUBLE = "double/binding";
    public static final String BINDING_SWORD = "sword/guard";
    public static final String SWORD_FULLER = "sword/fuller";
    public static final String BOW_STAVE = "bow/stave";
    public static final String BOW_RISER = "bow/riser";
    public static final String BOW_STRING = "bow/string";

    private static final String PATH_HEAD = "head";
    private static final String PATH_HANDLE = "handle";
    private static final String PATH_BINDING = "binding";
    private static final String PATH_LIMB = "limb";
    private static final String PATH_GRIP = "grip";
    private static final String PATH_BOWSTRING = "bowstring";
    private static final String PATH_PLATING_PREFIX = "plating";

    private static final String PART_BROAD_BLADE = "broad_blade";
    private static final String PART_SMALL_BLADE = "small_blade";
    private static final String PART_ADZE_HEAD = "adze_head";
    private static final String TCON_NAMESPACE = "tconstruct";

    private static final int MAX_SWORD_MODULE_COUNT = 5;

    public static boolean isSword(ToolStack tool) {
        List<IToolPart> parts = ToolPartsHook.parts(tool.getDefinition());
        for (IToolPart part : parts) {
            if (part instanceof Item itemPart) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(itemPart);
                if (id != null && isBladePart(id.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isBladePart(String path) {
        return PART_BROAD_BLADE.equals(path) || PART_SMALL_BLADE.equals(path);
    }

    private static boolean isAdzeHead(ToolStack tool) {
        List<IToolPart> parts = ToolPartsHook.parts(tool.getDefinition());
        for (IToolPart part : parts) {
            if (part instanceof Item itemPart) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(itemPart);
                if (id != null
                        && TCON_NAMESPACE.equals(id.getNamespace())
                        && PART_ADZE_HEAD.equals(id.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRanged(ToolStack tool) {
        for (MaterialStatsId statType : ToolMaterialHook.stats(tool.getDefinition())) {
            String path = statType.getPath();
            if (PATH_LIMB.equals(path) || PATH_GRIP.equals(path) || PATH_BOWSTRING.equals(path)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getSlotKeys(ItemStack stack, ToolStack tool) {
        List<MaterialStatsId> statTypes = ToolMaterialHook.stats(tool.getDefinition());
        boolean sword = isSword(tool);
        boolean adze = isAdzeHead(tool);
        int headCount = 0;
        for (MaterialStatsId statType : statTypes) {
            if (PATH_HEAD.equals(statType.getPath())) headCount++;
        }
        String[] headSlots = assignHeadSlots(headCount, sword, adze);
        boolean doubleStyle = !sword && (headCount >= 2 || (headCount == 1 && !adze));
        List<String> keys = new ArrayList<>(statTypes.size() + 1);
        int headIndex = 0;
        Map<String, Integer> keyOccurrences = new HashMap<>();
        for (MaterialStatsId statType : statTypes) {
            String path = statType.getPath();
            String baseKey;
            if (PATH_HEAD.equals(path)) {
                baseKey = headSlots[headIndex++];
            } else {
                baseKey = mapNonHeadSlot(path, sword, doubleStyle);
            }
            int occurrence = keyOccurrences.getOrDefault(baseKey, 0);
            keyOccurrences.put(baseKey, occurrence + 1);
            keys.add(occurrence == 0? baseKey : baseKey + "#" + occurrence
            );
        }
        if (sword) {
            normalizeSwordHandles(keys);
            addFullerIfNeeded(keys);
        }
        return keys.toArray(new String[0]);
    }

    private static void normalizeSwordHandles(List<String> keys) {
        int handleCount = 0;
        for (String key : keys) {
            if (HANDLE_SWORD.equals(baseSlotKey(key))) handleCount++;
        }
        if (handleCount < 2) return;
        boolean hasGuard = false;
        for (String key : keys) {
            if (BINDING_SWORD.equals(baseSlotKey(key))) {
                hasGuard = true;
                break;
            }
        }
        if (hasGuard) return;
        int seen = 0;
        for (int i = 0; i < keys.size(); i++) {
            if (HANDLE_SWORD.equals(baseSlotKey(keys.get(i)))) {
                seen++;
                if (seen == 2) {
                    keys.set(i, BINDING_SWORD);
                    return;
                }
            }
        }
    }

    private static void addFullerIfNeeded(List<String> keys) {
        boolean hasBlade = false;
        boolean hasHandle = false;
        boolean hasFuller = false;
        for (String key : keys) {
            String base = baseSlotKey(key);
            if (HEAD_SWORD.equals(base)) {
                hasBlade = true;
            } else if (HANDLE_SWORD.equals(base)) {
                hasHandle = true;
            } else if (SWORD_FULLER.equals(base)) {
                hasFuller = true;
            }
        }
        if (!hasBlade || !hasHandle || hasFuller) return;
        if (keys.size() >= MAX_SWORD_MODULE_COUNT) return;
        keys.add(SWORD_FULLER);
    }

    private static String[] assignHeadSlots(int headCount, boolean isSword, boolean isAdze) {
        if (headCount == 0) return new String[0];
        if (isSword) {
            String[] slots = new String[headCount];
            java.util.Arrays.fill(slots, HEAD_SWORD);
            return slots;
        }
        if (headCount == 1) {
            return new String[] {isAdze ? HEAD_SINGLE : HEAD_DOUBLE_LEFT};
        }
        int pairs = headCount / 2;
        boolean hasSingle = headCount % 2 == 1;
        List<String> slots = new ArrayList<>(headCount);
        for (int p = 0; p < pairs; p++) {
            slots.add(HEAD_DOUBLE_LEFT);
            slots.add(HEAD_DOUBLE_RIGHT);
        }
        if (hasSingle) slots.add(HEAD_SINGLE);
        return slots.toArray(new String[0]);
    }

    private static String mapNonHeadSlot(String path, boolean isSword, boolean doubleStyle) {
        if (PATH_HANDLE.equals(path)) {
            return isSword ? HANDLE_SWORD : (doubleStyle ? HANDLE_DOUBLE : HANDLE_SINGLE);
        }
        if (PATH_BINDING.equals(path)) {
            return isSword ? BINDING_SWORD : (doubleStyle ? BINDING_DOUBLE : BINDING_SINGLE);
        }
        if (PATH_LIMB.equals(path)) return BOW_STAVE;
        if (PATH_GRIP.equals(path)) return BOW_RISER;
        if (PATH_BOWSTRING.equals(path)) return BOW_STRING;
        return HEAD_SINGLE;
    }

    private static String baseSlotKey(String slotKey) {
        if (slotKey == null) return "";
        int index = slotKey.indexOf('#');
        return index > 0 ? slotKey.substring(0, index) : slotKey;
    }

    public static boolean isMajor(MaterialStatsId statType) {
        String path = statType.getPath();
        return PATH_HEAD.equals(path)
                || PATH_HANDLE.equals(path)
                || PATH_LIMB.equals(path)
                || PATH_BOWSTRING.equals(path)
                || path.startsWith(PATH_PLATING_PREFIX);
    }

    public static boolean isMinor(MaterialStatsId statType) {
        String path = statType.getPath();
        return PATH_BINDING.equals(path) || PATH_GRIP.equals(path);
    }

    public static boolean isMajorSlotKey(String slotKey) {
        String base = baseSlotKey(slotKey);
        return HEAD_SINGLE.equals(base)
                || HEAD_SWORD.equals(base)
                || HEAD_DOUBLE_LEFT.equals(base)
                || HEAD_DOUBLE_RIGHT.equals(base)
                || HANDLE_SINGLE.equals(base)
                || HANDLE_DOUBLE.equals(base)
                || HANDLE_SWORD.equals(base)
                || BOW_STAVE.equals(base)
                || BOW_STRING.equals(base);
    }

    public static boolean isMinorSlotKey(String slotKey) {
        String base = baseSlotKey(slotKey);
        return BINDING_SINGLE.equals(base)
                || BINDING_DOUBLE.equals(base)
                || BINDING_SWORD.equals(base)
                || BOW_RISER.equals(base)
                || SWORD_FULLER.equals(base);
    }
}