package com.myclient;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gui.GuiGraphics;

public abstract class Module {
    public enum Category {
        COMBAT("Combat"),
        RENDER("Render"),
        MOVEMENT("Movement"),
        MISC("Misc"),
        GRIEFING("Griefing"),
        DARK_MAGIC("DarkMagic");

        public final String title;
        Category(String title) { this.title = title; }
    }

    private final String name;
    private final Category category;
    private final int key;
    private boolean enabled = false;

    public Module(String name, Category category, int key) {
        this.name = name;
        this.category = category;
        this.key = key;
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void onTick() {}
    public void onRender3D(WorldRenderContext context) {}
    public void onRender2D(GuiGraphics guiGraphics) {}

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public int getKey() { return key; }
    public boolean isEnabled() { return enabled; }

    public String getDisplayLabel() { return name; }

    public void onEnable() {}

    public void onDisable() {}

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
}