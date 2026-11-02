package net.wertygh.tctb.core;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TCTBTransformationService implements ITransformationService {
    private static final String PLUGIN_NAME = "tctb_itemmodularhandheld_unify";

    @Override
    public String name() {
        return "tctb_core";
    }

    @Override
    public void initialize(IEnvironment environment) {
        try {
            Launcher launcher = Launcher.INSTANCE;
            if (launcher == null) {
                System.err.println("[tctb-core] Launcher.INSTANCE is null, skip plugin injection");
                return;
            }

            Field launchPluginsField = Launcher.class.getDeclaredField("launchPlugins");
            launchPluginsField.setAccessible(true);
            LaunchPluginHandler handler = (LaunchPluginHandler) launchPluginsField.get(launcher);
            if (handler == null) {
                System.err.println("[tctb-core] LaunchPluginHandler is null, skip plugin injection");
                return;
            }

            Field pluginsField = LaunchPluginHandler.class.getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> plugins = (Map<String, ILaunchPluginService>) pluginsField.get(handler);
            if (plugins == null) {
                System.err.println("[tctb-core] plugin map is null, skip plugin injection");
                return;
            }
            if (!plugins.containsKey(PLUGIN_NAME)) {
                plugins.put(PLUGIN_NAME, new TCTBLaunchPlugin());
                System.out.println("[tctb-core] launch plugin injected: " + PLUGIN_NAME);
            }
        } catch (Throwable t) {
            System.err.println("[tctb-core] failed to inject launch plugin, falling back to mixin-only mode");
            t.printStackTrace();
        }
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
    }

    @Override
    public List<ITransformer> transformers() {
        return List.of();
    }
}