package j.core.ai.plugin;

import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.reflections.Reflections;

import java.util.List;
import java.util.Set;

public class Plugins {
    private static Logger log=Logger.create(Plugins.class);//日志输出

    //提供商实现类
    private static ConcurrentMap<String, Plugin> plugins=new ConcurrentMap<>();

    static {
        //所有AI服务提供商的实现类（Provider的子类）
        Reflections reflections = new Reflections("j.core.ai.plugin");
        Set<Class<? extends Plugin>> clazz = reflections.getSubTypesOf(Plugin.class);
        clazz.forEach(cls -> {
            try {
                Plugin plugin = cls.getConstructor().newInstance();
                log.log("ai message plugin => " + plugin.getPluginId(), -1);
                plugins.put(plugin.getPluginId(), plugin);
            } catch (Exception e) {
                log.log(e, Logger.LEVEL_ERROR);
            }
        });
    }

    /**
     *
     * @param plugin
     */
    public static void registerPlugin(Plugin plugin){
        log.log("ai register plugin => " + plugin.getPluginId(), -1);
        plugins.put(plugin.getPluginId(), plugin);
    }

    /**
     *
     * @param pluginId
     */
    public static void unregisterPlugin(String pluginId){
        plugins.remove(pluginId);
    }

    /**
     *
     * @return
     */
    public static List<Plugin> getPlugins(){
        return plugins.listValues();
    }

    /**
     *
     * @param pluginId
     * @return
     */
    public static Plugin getPlugin(String pluginId){
        return JUtilString.isBlank(pluginId) ? null : plugins.get(pluginId);
    }
}
