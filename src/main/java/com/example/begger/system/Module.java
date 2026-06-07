package com.example.begger.system;

import com.example.begger.RankBegger;
import net.minecraft.client.Minecraft;

public class Module {
    private String name;
    private Category category;
    public boolean toggled;
    public boolean hidden;
    protected Minecraft mc = Minecraft.getMinecraft();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public void toggle() {
        toggled = !toggled;
        if (toggled) onEnable();
        else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}

    public boolean isKeybindOnly() {
        return false;
    }
}
