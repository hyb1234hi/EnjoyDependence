package com.youzan.mobile.lib_common;

import com.youzan.mobile.lib_common.baseInterface.IPlugin;

import java.util.HashMap;

public class Register {

    private Register(){}

    public static final Register register = new Register();
    private HashMap<String, IPlugin> plugins = new HashMap<>();

    public void regis(String pluginName, IPlugin plugin) {
        plugins.put(pluginName, plugin);
    }

    public IPlugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }
}
