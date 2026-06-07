package com.example.begger.system;

import com.example.begger.settings.Setting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsManager {
    private final Map<Module, List<Setting>> settingsMap = new HashMap<>();

    public void addSetting(Setting s, Module m) {
        settingsMap.computeIfAbsent(m, k -> new ArrayList<>()).add(s);
    }

    public List<Setting> getValuesByMod(Module m) {
        return settingsMap.getOrDefault(m, new ArrayList<>());
    }
}
