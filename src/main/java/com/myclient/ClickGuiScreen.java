package com.myclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final float GUI_SCALE = 0.78F;
    private static final int EDGE_MARGIN = 0;
    private static final int TOP_Y = 12;

    private static final DockPanel SERVER_DOCK = new DockPanel(Module.Scope.SERVER, true);
    private static final DockPanel SINGLE_DOCK = new DockPanel(Module.Scope.SINGLE, false);
    private static boolean initialized = false;

    public ClickGuiScreen() {
        super(Component.literal("ProHacker UI"));
        if (!initialized) {
            SERVER_DOCK.rebuildCategories();
            SINGLE_DOCK.rebuildCategories();
            initialized = true;
        }
    }

    private int getScaledWidth() {
        return Math.max(320, (int) (this.width / GUI_SCALE));
    }

    private static void playClickSound(boolean isLeverAction) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) {
            return;
        }
        if (isLeverAction) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.LEVER_CLICK, 1.0F));
        } else {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        double sx = mouseX / GUI_SCALE;
        double sy = mouseY / GUI_SCALE;
        int scaledW = getScaledWidth();

        SERVER_DOCK.updateDockPosition(scaledW, EDGE_MARGIN, TOP_Y);
        SINGLE_DOCK.updateDockPosition(scaledW, EDGE_MARGIN, TOP_Y);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(GUI_SCALE, GUI_SCALE, 1.0F);

        SERVER_DOCK.render(guiGraphics, this.font, (int) sx, (int) sy);
        SINGLE_DOCK.render(guiGraphics, this.font, (int) sx, (int) sy);

        guiGraphics.pose().popPose();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double sx = mouseX / GUI_SCALE;
        double sy = mouseY / GUI_SCALE;
        int scaledW = getScaledWidth();

        SERVER_DOCK.updateDockPosition(scaledW, EDGE_MARGIN, TOP_Y);
        SINGLE_DOCK.updateDockPosition(scaledW, EDGE_MARGIN, TOP_Y);

        if (SINGLE_DOCK.mouseClickedOpenMenus(sx, sy, button))
            return true;
        if (SERVER_DOCK.mouseClickedOpenMenus(sx, sy, button))
            return true;

        if (SINGLE_DOCK.mouseClickedCategories(sx, sy, button))
            return true;
        if (SERVER_DOCK.mouseClickedCategories(sx, sy, button))
            return true;

        if (SINGLE_DOCK.mouseClickedDock(sx, sy, button))
            return true;
        if (SERVER_DOCK.mouseClickedDock(sx, sy, button))
            return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        SERVER_DOCK.mouseReleased(button);
        SINGLE_DOCK.mouseReleased(button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double sx = mouseX / GUI_SCALE;
        double sy = mouseY / GUI_SCALE;

        if (SINGLE_DOCK.mouseDragged(sx, sy, button))
            return true;
        if (SERVER_DOCK.mouseDragged(sx, sy, button))
            return true;

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

    private static boolean isHovered(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx <= x + w && my <= y + h;
    }

    // Non-Server scope
    private static boolean isSingleOnlyCategory(Module.Category category) {
        return category.name().equalsIgnoreCase("DARKMAGIC")
                || category.title.equalsIgnoreCase("DarkMagic")
                || category.name().equalsIgnoreCase("GRIEFING")
                || category.title.equalsIgnoreCase("Griefing");
    }

    private static class DockPanel {
        private final Module.Scope scope;
        private final boolean dockLeft;
        private int x, y;
        private final int width = 84;
        private final int dockHeight = 15;
        private final int catBtnWidth = 84;
        private final int catBtnHeight = 13;
        private final int catGap = 2;

        private boolean expanded = false;
        private final List<CategoryNode> categoryNodes = new ArrayList<>();

        public DockPanel(Module.Scope scope, boolean dockLeft) {
            this.scope = scope;
            this.dockLeft = dockLeft;
        }

        public void updateDockPosition(int scaledWidth, int margin, int topY) {
            this.y = topY;
            if (dockLeft) {
                this.x = margin;
            } else {
                this.x = Math.max(width + 20, scaledWidth - this.width - margin);
            }
            syncCategoryPositions();
        }

        public void rebuildCategories() {
            List<CategoryNode> oldNodes = new ArrayList<>(categoryNodes);
            categoryNodes.clear();

            for (Module.Category category : Module.Category.values()) {
                boolean singleOnly = isSingleOnlyCategory(category);
                if (scope == Module.Scope.SINGLE && !singleOnly) {
                    continue;
                }
                if (scope == Module.Scope.SERVER && singleOnly) {
                    continue;
                }

                CategoryNode existing = null;
                for (CategoryNode old : oldNodes) {
                    if (old.category == category) {
                        existing = old;
                        break;
                    }
                }

                CategoryNode node = (existing != null)
                        ? existing
                        : new CategoryNode(scope, category, catBtnWidth, catBtnHeight);
                categoryNodes.add(node);
            }
            syncCategoryPositions();
        }

        private void syncCategoryPositions() {
            int curY = y + dockHeight + catGap;
            int defaultMenuX = dockLeft ? (x + width + 8) : Math.max(4, x - 112);

            for (CategoryNode node : categoryNodes) {
                node.setButtonAnchor(x, curY, defaultMenuX, y + dockHeight + 8);
                curY += catBtnHeight + catGap;
            }
        }

        public void render(GuiGraphics g, Font font, int mouseX, int mouseY) {
            boolean isServer = (scope == Module.Scope.SERVER);
            int bg = isServer ? 0xEE0D2738 : 0xEE2B1238;
            int accent = isServer ? 0xFF00FFFF : 0xFFFF66FF;
            String label = isServer ? "§b§lSERVER" : "§d§lSINGLE";
            String arrow = expanded ? "§7[-]" : "§7[+]";

            g.fill(x, y, x + width, y + dockHeight, bg);
            if (dockLeft) {
                g.fill(x, y, x + 2, y + dockHeight, accent);
            } else {
                g.fill(x + width - 2, y, x + width, y + dockHeight, accent);
            }
            g.fill(x, y, x + width, y + 1, accent);

            g.drawString(font, label, x + 5, y + 4, 0xFFFFFF);
            g.drawString(font, arrow, x + width - 19, y + 4, 0xFFFFFF);

            syncCategoryPositions();

            if (expanded) {
                for (CategoryNode node : categoryNodes) {
                    node.renderCategoryButton(g, font, mouseX, mouseY);
                }
            }

            for (CategoryNode node : categoryNodes) {
                node.renderOpenMenu(g, font, mouseX, mouseY);
            }
        }

        public boolean mouseClickedOpenMenus(double mouseX, double mouseY, int button) {
            for (CategoryNode node : categoryNodes) {
                if (node.mouseClickedMenu(mouseX, mouseY, button)) {
                    return true;
                }
            }

            return false;
        }

        public boolean mouseClickedCategories(double mouseX, double mouseY, int button) {
            if (!expanded) {
                return false;
            }
            syncCategoryPositions();
            for (CategoryNode node : categoryNodes) {
                if (node.mouseClickedButton(mouseX, mouseY, button)) {
                    return true;
                }
            }

            return false;
        }

        public boolean mouseClickedDock(double mouseX, double mouseY, int button) {
            if (button == 0 && isHovered(mouseX, mouseY, x, y, width, dockHeight)) {
                expanded = !expanded;
                if (expanded) {
                    rebuildCategories();
                }
                playClickSound(false);
                return true;
            }
            return false;
        }

        public void mouseReleased(int button) {
            for (CategoryNode node : categoryNodes) {
                node.mouseReleased(button);
            }
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button) {
            for (CategoryNode node : categoryNodes) {
                if (node.mouseDragged(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class CategoryNode {
        private final Module.Scope scope;
        private final Module.Category category;
        private int btnX, btnY;
        private final int btnWidth;
        private final int btnHeight;

        private int menuX, menuY;
        private boolean menuPosInitialized = false;
        private final int menuWidth = 104;
        private final int menuHeaderHeight = 13;
        private final int itemHeight = 11;

        private boolean menuOpen = false;
        private boolean menuCollapsed = true;
        private boolean draggingMenu = false;
        private int dragOffsetX, dragOffsetY;

        public CategoryNode(Module.Scope scope, Module.Category category, int btnWidth, int btnHeight) {
            this.scope = scope;
            this.category = category;
            this.btnWidth = btnWidth;
            this.btnHeight = btnHeight;
        }

        public void setButtonAnchor(int btnX, int btnY, int defaultMenuX, int defaultMenuY) {
            this.btnX = btnX;
            this.btnY = btnY;
            if (!menuPosInitialized) {
                this.menuX = defaultMenuX;
                this.menuY = defaultMenuY;
                this.menuPosInitialized = true;
            }
        }

        private boolean matches(Module m) {
            if (m.getCategory() != category) {
                return false;
            }
            if (scope == Module.Scope.SINGLE && isSingleOnlyCategory(category)) {
                return true;
            }

            return m.getScope() == scope;
        }

        public void renderCategoryButton(GuiGraphics g, Font font, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY, btnX, btnY, btnWidth, btnHeight);
            int btnBg = menuOpen ? 0xDD1F4E5F : (hovered ? 0xDD2C2C2C : 0xCC181818);
            int accent = (scope == Module.Scope.SERVER) ? 0xFF00FFFF : 0xFFFF66FF;

            g.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, btnBg);
            g.fill(btnX, btnY, btnX + 2, btnY + btnHeight, accent);

            String title = (menuOpen ? "§f§l" : "§7") + category.title;
            g.drawString(font, title, btnX + 4, btnY + 3, 0xFFFFFF);
            g.drawString(font, menuOpen ? "§a●" : "§8○", btnX + btnWidth - 9, btnY + 3, 0xFFFFFF);
        }

        public void renderOpenMenu(GuiGraphics g, Font font, int mouseX, int mouseY) {
            if (!menuOpen) {
                return;
            }

            int accent = (scope == Module.Scope.SERVER) ? 0xFF00FFFF : 0xFFFF66FF;
            String scopeTag = (scope == Module.Scope.SERVER) ? "§b" : "§d";

            g.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeaderHeight, 0xEE102535);
            g.fill(menuX, menuY, menuX + menuWidth, menuY + 1, accent);
            g.drawString(font, scopeTag + "§l" + category.title, menuX + 4, menuY + 3, 0xFFFFFF);
            g.drawString(font, menuCollapsed ? "§e[~]§c[x]" : "§7[~]§c[x]", menuX + menuWidth - 30, menuY + 3, 0xFFFFFF);

            if (menuCollapsed) {
                g.fill(menuX, menuY + menuHeaderHeight, menuX + menuWidth, menuY + menuHeaderHeight + 1, accent);
                return;
            }

            int curY = menuY + menuHeaderHeight;
            for (Module m : MyClient.modules) {
                if (!matches(m)) {
                    continue;
                }

                boolean itemHovered = isHovered(mouseX, mouseY, menuX, curY, menuWidth, itemHeight);
                int itemBg;
                if (m.isEnabled()) {
                    itemBg = itemHovered ? 0xDD00AAAA : 0xCC008888;
                } else {
                    itemBg = itemHovered ? 0xDD2A2A2A : 0xCC181818;
                }

                g.fill(menuX, curY, menuX + menuWidth, curY + itemHeight, itemBg);
                String modText = (m.isEnabled() ? "§f" : "§7") + m.getDisplayLabel();
                g.drawString(font, modText, menuX + 4, curY + 2, 0xFFFFFF);

                curY += itemHeight;
            }

            g.fill(menuX, curY, menuX + menuWidth, curY + 1, accent);
        }

        public boolean mouseClickedButton(double mouseX, double mouseY, int button) {
            if (!isHovered(mouseX, mouseY, btnX, btnY, btnWidth, btnHeight)) {
                return false;
            }
            if (button == 0) {
                menuOpen = !menuOpen;
                if (menuOpen) {
                    menuCollapsed = true;
                } else {
                    draggingMenu = false;
                }
                playClickSound(false);

                return true;
            } else if (button == 1 && menuOpen) {
                menuCollapsed = !menuCollapsed;
                playClickSound(false);

                return true;
            }

            return false;
        }

        public boolean mouseClickedMenu(double mouseX, double mouseY, int button) {
            if (!menuOpen)
                return false;

            if (button == 0 && isHovered(mouseX, mouseY, menuX + menuWidth - 13, menuY, 13, menuHeaderHeight)) {
                menuOpen = false;
                draggingMenu = false;
                playClickSound(false);

                return true;
            }

            if ((button == 0 && isHovered(mouseX, mouseY, menuX + menuWidth - 26, menuY, 13, menuHeaderHeight))
                    || (button == 1 && isHovered(mouseX, mouseY, menuX, menuY, menuWidth, menuHeaderHeight))) {
                menuCollapsed = !menuCollapsed;
                draggingMenu = false;
                playClickSound(false);

                return true;
            }

            if (button == 0 && isHovered(mouseX, mouseY, menuX, menuY, menuWidth - 26, menuHeaderHeight)) {
                draggingMenu = true;
                dragOffsetX = (int) mouseX - menuX;
                dragOffsetY = (int) mouseY - menuY;
                playClickSound(false);

                return true;
            }

            if (menuCollapsed) {
                return false;
            }

            if (button == 0 || button == 1) {
                int curY = menuY + menuHeaderHeight;
                for (Module m : MyClient.modules) {
                    if (!matches(m)) {
                        continue;
                    }

                    if (isHovered(mouseX, mouseY, menuX, curY, menuWidth, itemHeight)) {
                        if (button == 0) {
                            m.toggle();
                            playClickSound(true);
                        } else {
                            if (m instanceof MyClient.NameSpooferModule spoofer) {
                                spoofer.cycleMode();
                                playClickSound(true);

                                return true;
                            } else if (m instanceof MyClient.MultiBuilderModule builder) {
                                builder.cycleMode();
                                playClickSound(true);

                                return true;
                            } else if (m instanceof MyClient.MinimapModule minimap) {
                                minimap.cycleMode();
                                playClickSound(true);

                                return true;
                            }
                        }

                        return true;
                    }
                    curY += itemHeight;
                }
            }

            return false;
        }

        public void mouseReleased(int button) {
            if (button == 0) {
                draggingMenu = false;
            }
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button) {
            if (draggingMenu && button == 0) {
                this.menuX = (int) mouseX - dragOffsetX;
                this.menuY = (int) mouseY - dragOffsetY;

                return true;
            }

            return false;
        }
    }
}