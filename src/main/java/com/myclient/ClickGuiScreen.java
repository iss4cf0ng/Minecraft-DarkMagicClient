package com.myclient;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final float GUI_SCALE = 0.85F;

    private static final ScopeRoot SERVER_ROOT = new ScopeRoot(Module.Scope.SERVER, 12, 12);
    private static final ScopeRoot SINGLE_ROOT = new ScopeRoot(Module.Scope.SINGLE, 240, 12);
    private static boolean initialized = false;

    public ClickGuiScreen() {
        super(Component.literal("MyClient ClickGUI"));
        if (!initialized) {
            SERVER_ROOT.rebuildCategories();
            SINGLE_ROOT.rebuildCategories();
            initialized = true;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        double sx = mouseX / GUI_SCALE;
        double sy = mouseY / GUI_SCALE;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(GUI_SCALE, GUI_SCALE, 1.0F);

        SERVER_ROOT.render(guiGraphics, this.font, (int) sx, (int) sy);
        SINGLE_ROOT.render(guiGraphics, this.font, (int) sx, (int) sy);

        guiGraphics.pose().popPose();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double sx = mouseX / GUI_SCALE;
        double sy = mouseY / GUI_SCALE;

        if (SINGLE_ROOT.mouseClicked(sx, sy, button))
            return true;
        if (SERVER_ROOT.mouseClicked(sx, sy, button))
            return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        SERVER_ROOT.mouseReleased(button);
        SINGLE_ROOT.mouseReleased(button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double sx = mouseX / GUI_SCALE;
        double sy = mouseY / GUI_SCALE;

        if (SINGLE_ROOT.mouseDragged(sx, sy, button)) return true;
        if (SERVER_ROOT.mouseDragged(sx, sy, button)) return true;

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

    private static class ScopeRoot {
        private final Module.Scope scope;
        private int x, y;
        private final int width = 104;
        private final int rootHeight = 15;
        private final int catBtnHeight = 13;
        private final int catGap = 2;

        private boolean expanded = false;
        private boolean dragging = false;
        private int dragOffsetX, dragOffsetY;

        private final List<CategoryNode> categoryNodes = new ArrayList<>();

        public ScopeRoot(Module.Scope scope, int x, int y) {
            this.scope = scope;
            this.x = x;
            this.y = y;
        }

        public void rebuildCategories() {
            categoryNodes.clear();
            int curY = y + rootHeight + 2;
            for (Module.Category category : Module.Category.values()) {
                boolean hasModule = false;
                for (Module m : MyClient.modules) {
                    if (m.getScope() == scope && m.getCategory() == category) {
                        hasModule = true;
                        break;
                    }
                }
                if (!hasModule)
                    continue;

                CategoryNode node = new CategoryNode(scope, category, x, curY, width);
                categoryNodes.add(node);
                curY += catBtnHeight + catGap;
            }
            syncCategoryPositions();
        }

        private void syncCategoryPositions() {
            int curY = y + rootHeight + 2;
            for (CategoryNode node : categoryNodes) {
                node.setAnchor(x, curY);
                curY += catBtnHeight + catGap;
            }
        }

        public void render(GuiGraphics g, Font font, int mouseX, int mouseY) {
            boolean isServer = (scope == Module.Scope.SERVER);
            int bg = isServer ? 0xEE0D2738 : 0xEE2B1238;
            int border = isServer ? 0xFF00FFFF : 0xFFFF66FF;
            String label = (isServer ? "§b§lSERVER" : "§d§lSINGLE");
            String arrow = expanded ? "§7[-]" : "§7[+]";

            g.fill(x, y, x + width, y + rootHeight, bg);
            g.fill(x, y, x + width, y + 1, border);
            g.drawString(font, label, x + 5, y + 4, 0xFFFFFF);
            g.drawString(font, arrow, x + width - 20, y + 4, 0xFFFFFF);

            if (!expanded) return;

            syncCategoryPositions();
            for (CategoryNode node : categoryNodes) {
                node.renderButtonAndMenu(g, font, mouseX, mouseY);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (expanded) {
                for (CategoryNode node : categoryNodes) {
                    if (node.mouseClickedMenu(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }

            if (isHovered(mouseX, mouseY, x, y, width, rootHeight)) {
                if (button == 0) {
                    expanded = !expanded;
                    if (expanded) {
                        rebuildCategories();
                    }
                    return true;
                } else if (button == 1) {
                    dragging = true;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                    return true;
                }
            }

            if (expanded) {
                for (CategoryNode node : categoryNodes) {
                    if (node.mouseClickedButton(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public void mouseReleased(int button) {
            if (button == 1) {
                dragging = false;
            }
            for (CategoryNode node : categoryNodes) {
                node.mouseReleased(button);
            }
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button) {
            if (dragging && button == 1) {
                this.x = (int) mouseX - dragOffsetX;
                this.y = (int) mouseY - dragOffsetY;
                syncCategoryPositions();
                return true;
            }
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
        private final int btnHeight = 13;

        private int menuX, menuY;
        private boolean menuPosInitialized = false;
        private final int menuWidth = 96;
        private final int menuHeaderHeight = 13;
        private final int itemHeight = 12;

        private boolean menuOpen = false;
        private boolean draggingMenu = false;
        private int dragOffsetX, dragOffsetY;

        public CategoryNode(Module.Scope scope, Module.Category category, int btnX, int btnY, int btnWidth) {
            this.scope = scope;
            this.category = category;
            this.btnX = btnX;
            this.btnY = btnY;
            this.btnWidth = btnWidth;
        }

        public void setAnchor(int x, int y) {
            this.btnX = x;
            this.btnY = y;
            if (!menuPosInitialized) {
                this.menuX = x + btnWidth + 8;
                this.menuY = y;
                this.menuPosInitialized = true;
            }
        }

        private boolean matches(Module m) {
            return m.getScope() == scope && m.getCategory() == category;
        }

        public void renderButtonAndMenu(GuiGraphics g, Font font, int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY, btnX, btnY, btnWidth, btnHeight);
            int btnBg = menuOpen ? 0xDD1F4E5F : (hovered ? 0xDD2C2C2C : 0xCC181818);
            int accent = (scope == Module.Scope.SERVER) ? 0xFF00FFFF : 0xFFFF66FF;

            g.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, btnBg);
            g.fill(btnX, btnY, btnX + 2, btnY + btnHeight, accent);

            String title = (menuOpen ? "§f§l" : "§7") + category.title;
            g.drawString(font, title, btnX + 5, btnY + 3, 0xFFFFFF);
            g.drawString(font, menuOpen ? "§b>" : "§8+", btnX + btnWidth - 9, btnY + 3, 0xFFFFFF);

            if (!menuOpen)
                return;

            g.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeaderHeight, 0xEE102535);
            g.fill(menuX, menuY, menuX + menuWidth, menuY + 1, accent);
            g.drawString(font, "§b§l" + category.title, menuX + 4, menuY + 3, 0xFFFFFF);

            int curY = menuY + menuHeaderHeight;
            for (Module m : MyClient.modules) {
                if (!matches(m)) continue;

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
            if (button == 0 && isHovered(mouseX, mouseY, btnX, btnY, btnWidth, btnHeight)) {
                menuOpen = !menuOpen;
                return true;
            }
            return false;
        }

        public boolean mouseClickedMenu(double mouseX, double mouseY, int button) {
            if (!menuOpen)
                return false;

            if (button == 0 && isHovered(mouseX, mouseY, menuX, menuY, menuWidth, menuHeaderHeight)) {
                draggingMenu = true;
                dragOffsetX = (int) mouseX - menuX;
                dragOffsetY = (int) mouseY - menuY;
                return true;
            }

            if (button == 0 || button == 1) {
                int curY = menuY + menuHeaderHeight;
                for (Module m : MyClient.modules) {
                    if (!matches(m)) continue;

                    if (isHovered(mouseX, mouseY, menuX, curY, menuWidth, itemHeight)) {
                        if (button == 0) {
                            m.toggle();
                        } else {
                            if (m instanceof MyClient.NameSpooferModule spoofer) {
                                spoofer.cycleMode();
                                return true;
                            } else if (m instanceof MyClient.MultiBuilderModule builder) {
                                builder.cycleMode();
                                return true;
                            } else if (m instanceof MyClient.MinimapModule minimap) {
                                minimap.cycleMode();
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