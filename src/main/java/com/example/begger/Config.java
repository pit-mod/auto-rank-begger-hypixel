package com.example.begger;

import com.example.begger.system.Rank;
import com.example.begger.system.Module;
import com.example.begger.settings.Setting;
import com.example.begger.settings.impl.*;
import com.example.begger.utils.WebhookUtil;
import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static Configuration config;

    public static Map<Rank, List<String>> rankMessages = new HashMap<>();

    public static void init(File file) {
        config = new Configuration(file);
        for (Rank rank : Rank.values()) {
            rankMessages.put(rank, new ArrayList<>());
        }
        load();
    }

    public static void load() {
        config.load();

        if (RankBegger.moduleManager != null) {
            for (Module m : RankBegger.moduleManager.getModules()) {
                boolean savedState = config.getBoolean("toggled", m.getName(), m.toggled, "Is module enabled");
                if (savedState != m.toggled) {
                    m.toggled = savedState;
                    if (m.toggled) m.onEnable();
                    else m.onDisable();
                }
                if (RankBegger.settingsManager != null) {
                    for (Setting s : RankBegger.settingsManager.getValuesByMod(m)) {
                        if (s instanceof BooleanSetting) {
                            ((BooleanSetting) s).setEnabled(config.getBoolean(s.getName(), m.getName(), ((BooleanSetting) s).isEnabled(), ""));
                        } else if (s instanceof NumberSetting) {
                            ((NumberSetting) s).setValue(config.getFloat(s.getName(), m.getName(), (float) ((NumberSetting) s).getValue(), -10000f, 10000f, ""));
                        } else if (s instanceof SimpleModeSetting) {
                            ((SimpleModeSetting) s).setSelected(config.getString(s.getName(), m.getName(), ((SimpleModeSetting) s).getSelected(), ""));
                        } else if (s instanceof BindSetting) {
                            ((BindSetting) s).setKeyCode(config.getInt(s.getName(), m.getName(), ((BindSetting) s).getKeyCode(), -1000, 1000, ""));
                        } else if (s instanceof ColorSetting) {
                            int rgb = config.getInt(s.getName(), m.getName(), ((ColorSetting) s).getColor().getRGB(), Integer.MIN_VALUE, Integer.MAX_VALUE, "");
                            ((ColorSetting) s).setColor(new java.awt.Color(rgb, true));
                        } else if (s instanceof InputSetting) {
                            InputSetting input = (InputSetting) s;
                            String raw = config.getString(s.getName(), m.getName(), input.getContent(), "");
                            String value = raw;
                            if ("Webhook URL".equals(s.getName())) {
                                value = WebhookUtil.normalizeWebhookUrl(raw);
                                if (!value.equals(raw)) {
                                    config.get(m.getName(), s.getName(), "").set(value);
                                }
                            }
                            input.setContent(value);
                        }
                    }
                }
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        if (RankBegger.moduleManager != null) {
            for (Module m : RankBegger.moduleManager.getModules()) {
                config.get(m.getName(), "toggled", false).set(m.toggled);
                if (RankBegger.settingsManager != null) {
                    for (Setting s : RankBegger.settingsManager.getValuesByMod(m)) {
                        if (s instanceof BooleanSetting) {
                            config.get(m.getName(), s.getName(), false).set(((BooleanSetting) s).isEnabled());
                        } else if (s instanceof NumberSetting) {
                            config.get(m.getName(), s.getName(), 0.0).set(((NumberSetting) s).getValue());
                        } else if (s instanceof SimpleModeSetting) {
                            config.get(m.getName(), s.getName(), "").set(((SimpleModeSetting) s).getSelected());
                        } else if (s instanceof BindSetting) {
                            config.get(m.getName(), s.getName(), 0).set(((BindSetting) s).getKeyCode());
                        } else if (s instanceof ColorSetting) {
                            config.get(m.getName(), s.getName(), 0).set(((ColorSetting) s).getColor().getRGB());
                        } else if (s instanceof InputSetting) {
                            config.get(m.getName(), s.getName(), "").set(((InputSetting) s).getContent());
                        }
                    }
                }
            }
        }

        config.save();
    }
}
