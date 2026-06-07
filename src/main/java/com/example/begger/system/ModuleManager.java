package com.example.begger.system;

import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.system.impl.AutoReconnectModule;
import com.example.begger.system.impl.ClickGuiModule;
import com.example.begger.system.impl.WebhookTestModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        addModule(new RankBeggerModule());
        addModule(new AutoReconnectModule());
        addModule(new ClickGuiModule());
        addModule(new WebhookTestModule());
    }

    public void addModule(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module m : modules) {
            if (m.getClass() == clazz) return (T) m;
        }
        return null;
    }
}
