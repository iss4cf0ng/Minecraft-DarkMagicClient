package com.myclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final List<CategoryPanel> panels = new ArrayList<>();

    public ClickGuiScreen() {
        super(Component.literal("MyClient ClickGUI"));
        if (panels.isEmpty()) {
            int startX = 12;
            int startY = 18;
            int stepX = 96;

            for (Module.Category category : Module.Category.values()) {
                if (startX > 380) {
                    startX = 12;
                    startY += 115;
                }
                panels.add(new CategoryPanel(category, startX, startY));
                startX += stepX;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (CategoryPanel panel : panels) {
            panel.render(guiGraphics, this.font, mouseX, mouseY);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // nothing here
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class CategoryPanel {
        private final Module.Category category;
        private int x, y;
        private final int width = 105;
        private final int headerHeight = 16;
        private final int itemHeight = 15;

        private boolean expanded = true;
        private boolean dragging = false;
        private int dragOffsetX, dragOffsetY;

        public CategoryPanel(Module.Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }

        private boolean isHovered(double mouseX, double mouseY, int rectX, int rectY, int rectW, int rectH) {
            return mouseX >= rectX && mouseY >= rectY && !(mouseX > rectX + rectW) && !(mouseY > rectY + rectH);
        }

        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
            g.fill(x, y, x + width, y + headerHeight, 0xEE102535);
            g.fill(x, y, x + width, y + 1, 0xFF00FFFF);

            String title = "§b§l" + category.title;
            g.drawString(font, title, x + 5, y + 4, 0xFFFFFF);
            String indicator = expanded ? "§7-" : "§7+";
            g.drawString(font, indicator, x + width - 10, y + 4, 0xFFFFFF);

            if (!expanded) return;

            int currentY = y + headerHeight;
            for (Module m : MyClient.modules) {
                if (m.getCategory() != this.category) continue;

                boolean hovered = isHovered(mouseX, mouseY, x, currentY, width, itemHeight);

                int bgColor;
                if (m.isEnabled()) {
                    bgColor = hovered ? 0xDD00AAAA : 0xCC008888;
                } else {
                    bgColor = hovered ? 0xDD2A2A2A : 0xCC181818;
                }

                g.fill(x, currentY, x + width, currentY + itemHeight, bgColor);

                String modText = (m.isEnabled() ? "§f" : "§7") + m.getDisplayLabel();
                g.drawString(font, modText, x + 6, currentY + 4, 0xFFFFFF);

                currentY += itemHeight;
            }

            g.fill(x, currentY, x + width, currentY + 1, 0xFF00AAAA);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered(mouseX, mouseY, x, y, width, headerHeight)) {
                if (button == 0) {
                    dragging = true;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                    return true;
                } else if (button == 1) {
                    expanded = !expanded;
                    return true;
                }
            }

            if (expanded && (button == 0 || button == 1)) {
                int currentY = y + headerHeight;
                for (Module m : MyClient.modules) {
                    if (m.getCategory() != this.category) continue;

                    if (isHovered(mouseX, mouseY, x, currentY, width, itemHeight)) {
                        if (button == 0) {
                            m.toggle();
                        } else if (button == 1 && m instanceof MyClient.NameSpooferModule spoofer) {
                            spoofer.cycleMode();
                        }
                        return true;
                    }
                    currentY += itemHeight;
                }
            }

            return false;
        }

        public void mouseReleased(int button) {
            if (button == 0) {
                dragging = false;
            }
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button) {
            if (dragging && button == 0) {
                this.x = (int) mouseX - dragOffsetX;
                this.y = (int) mouseY - dragOffsetY;
                return true;
            }
            return false;
        }
    }
}
