package com.example.begger.system;

import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.system.impl.AutoReconnectModule;
import com.example.begger.system.impl.ClickGuiModule;
import com.example.begger.system.impl.WebhookTestModule;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        addModule(new RankBeggerModule());
        addModule(new AutoReconnectModule());
        addModule(new ClickGuiModule());
        addModule(new WebhookTestModule());
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void addModule(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module m : modules) {
            if (m.getClass() == clazz) return (T) m;
        }
        return null;
    }
}
