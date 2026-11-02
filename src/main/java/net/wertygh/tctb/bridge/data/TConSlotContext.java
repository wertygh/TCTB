package net.wertygh.tctb.bridge.data;

import net.wertygh.tctb.config.TCTBConfig;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class TConSlotContext {
    private static final Object NULL_SLOT = new Object();

    private static final ThreadLocal<String> CURRENT_SLOT_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Deque<Object>> SLOT_CONTEXT_STACK =
        ThreadLocal.withInitial(ArrayDeque::new);

    public static void pushSlotContext(@Nullable String slot) {
        String previous = CURRENT_SLOT_CONTEXT.get();
        SLOT_CONTEXT_STACK.get().push(previous == null ? NULL_SLOT : previous);
        setOrRemove(CURRENT_SLOT_CONTEXT, slot);
    }

    public static void popSlotContext() {
        Deque<Object> stack = SLOT_CONTEXT_STACK.get();
        if (stack.isEmpty()) {
            CURRENT_SLOT_CONTEXT.remove();
            SLOT_CONTEXT_STACK.remove();
            return;
        }
        Object previous = stack.pop();
        if (previous == NULL_SLOT) {
            CURRENT_SLOT_CONTEXT.remove();
        } else {
            CURRENT_SLOT_CONTEXT.set((String) previous);
        }
        if (stack.isEmpty()) {
            SLOT_CONTEXT_STACK.remove();
        }
    }

    public static void setSlotContext(@Nullable String slot) {
        setOrRemove(CURRENT_SLOT_CONTEXT, slot);
    }

    public static String getSpeedGroupKey(@Nullable String slotKey) {
        String base = getBaseSlotKey(slotKey);
        if (base == null) return null;
        if (TCTBConfig.COMMON.mergeDoubleHeadSlotsForSpeed.get()
                && (TConSlotMapping.HEAD_DOUBLE_LEFT.equals(base)
                    || TConSlotMapping.HEAD_DOUBLE_RIGHT.equals(base))) {
            return "double/head";
        }
        return base;
    }

    @Nullable
    public static String getSlotContext() {
        return CURRENT_SLOT_CONTEXT.get();
    }

    public static <T> T withSlotContext(@Nullable String slot, Supplier<T> action) {
        pushSlotContext(slot);
        try {
            return action.get();
        } finally {
            popSlotContext();
        }
    }

    @Nullable
    public static String getBaseSlotKey(@Nullable String slotKey) {
        if (slotKey == null) return null;
        int index = slotKey.indexOf('#');
        return index > 0 ? slotKey.substring(0, index) : slotKey;
    }

    private static void setOrRemove(ThreadLocal<String> context, @Nullable String value) {
        if (value == null) {
            context.remove();
        } else {
            context.set(value);
        }
    }
}