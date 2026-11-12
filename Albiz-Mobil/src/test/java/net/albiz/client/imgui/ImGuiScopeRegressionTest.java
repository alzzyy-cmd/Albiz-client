package net.albiz.client.imgui;

import imgui.ImGui;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real desktop ImGui JNI, no OpenGL/display. Exercises the assertion path seen in the log. */
class ImGuiScopeRegressionTest {
    private imgui.ImFont defaultFont;
    @BeforeEach void setup() {
        ImGui.createContext();
        ImGui.getIO().setIniFilename(null);
        ImGui.getIO().setDisplaySize(1280, 720);
        ImGui.getIO().setDeltaTime(1.0F / 60.0F);
        defaultFont = ImGui.getIO().getFonts().addFontDefault();
        assertTrue(ImGui.getIO().getFonts().build());
        assertEquals(0, GuardedImGui.scopeMark());
        ImGui.newFrame();
    }
    @AfterEach void cleanup() { ImGui.destroyContext(); }
    @Test void thrownSettingRendererUnwindsChildAndWindowBeforeEndFrame() {
        int mark = GuardedImGui.scopeMark();
        try {
            GuardedImGui.pushStyleVar(0, 1.0F);
            GuardedImGui.begin("module");
            GuardedImGui.beginChild("settings", 300, 300, false, 0);
            GuardedImGui.pushID("binding");
            throw new IllegalStateException("simulated GLFW-stub failure");
        } catch (IllegalStateException expected) {
            assertEquals(4, GuardedImGui.recoverScopes(mark));
        }
        assertEquals(0, GuardedImGui.scopeMark());
        assertDoesNotThrow(ImGui::render);
    }
    @Test void recoveryPreservesOuterThemeAndFontScopes() {
        GuardedImGui.pushStyleVar(0, 0.9F);
        GuardedImGui.pushFont(defaultFont);
        int mark = GuardedImGui.scopeMark();
        GuardedImGui.begin("card");
        GuardedImGui.pushStyleColor(0, -1);
        GuardedImGui.beginChild("body", 200, 200, false, 0);
        GuardedImGui.pushID(1);
        GuardedImGui.recoverScopes(mark);
        assertEquals(mark, GuardedImGui.scopeMark());
        GuardedImGui.popFont(); GuardedImGui.popStyleVar();
        assertDoesNotThrow(ImGui::render);
        assertEquals(0, GuardedImGui.scopeMark());
    }
    @Test void normalBalancedCodeDoesNotRequireRecovery() {
        GuardedImGui.begin("normal");
        GuardedImGui.pushID(1); GuardedImGui.popID();
        GuardedImGui.beginChild("child", 100, 100, false, 0); GuardedImGui.endChild();
        GuardedImGui.end();
        assertEquals(0, GuardedImGui.scopeMark());
        assertDoesNotThrow(ImGui::render);
    }
    @Test void wrongEndIsDetectedBeforeNativeAssertion() {
        GuardedImGui.begin("parent");
        GuardedImGui.beginChild("child", 100, 100, false, 0);
        assertThrows(IllegalStateException.class, GuardedImGui::end);
        assertEquals(2, GuardedImGui.recoverScopes(0));
        assertDoesNotThrow(ImGui::render);
    }
    @Test void unmatchedPopBecomesJavaErrorInsteadOfNativeAssertion() {
        assertThrows(IllegalStateException.class, GuardedImGui::popFont);
        assertDoesNotThrow(ImGui::render);
    }
    @Test void nativeWidgetIdsStayStableAcrossLocales() {
        GuardedImGui.begin("test-localization");
        net.albiz.client.i18n.AlbizI18n.setLanguage("tr_tr");
        int tr=ImGui.getID(net.albiz.client.i18n.AlbizI18n.label("Save all"));
        net.albiz.client.i18n.AlbizI18n.setLanguage("en_us");
        int en=ImGui.getID(net.albiz.client.i18n.AlbizI18n.label("Save all"));
        assertEquals(tr,en);
        GuardedImGui.end();ImGui.render();
        net.albiz.client.i18n.AlbizI18n.setLanguage("tr_tr");
    }
    @Test void translatingInputHintDoesNotTranslateTypedBuffer() {
        GuardedImGui.begin("test-input");
        imgui.type.ImString value=new imgui.type.ImString("Buy Rockets",128);
        GuardedImGui.inputTextWithHint("Name","Search",value);
        assertEquals("Buy Rockets",value.get());
        GuardedImGui.end();ImGui.render();
    }
    @Test void newTurkicIconsProduceNativeDrawGeometry() {
        var dl=ImGui.getBackgroundDrawList();
        for(var action:new net.albiz.client.gui.mainmenu.MenuAction[]{net.albiz.client.gui.mainmenu.MenuAction.ALBIZ,
                net.albiz.client.gui.mainmenu.MenuAction.MODS,net.albiz.client.gui.mainmenu.MenuAction.CREDITS}) {
            net.albiz.client.gui.mainmenu.MenuIcons.draw(dl,action,64+action.ordinal()*24,64,10,-1,1.5F);
        }
        ImGui.render();assertTrue(ImGui.getDrawData().getTotalVtxCount()>30);
    }
    @Test void spacedMenuTextMeasuresTranslatedWholeWordBeforeSplittingGlyphs() {
        net.albiz.client.i18n.AlbizI18n.setLanguage("tr_tr");String text=net.albiz.client.i18n.AlbizI18n.caption("MULTIPLAYER");
        float expected=2F*(text.length()-1);
        for(int j=0;j<text.length();j++) expected+=net.albiz.client.gui.SteppeDraw.textWidth(defaultFont,String.valueOf(text.charAt(j)));
        assertEquals(expected,net.albiz.client.gui.SteppeDraw.spacedWidth(defaultFont,"MULTIPLAYER",2F),.01F);
    }
    @Test void portableRgbaControlsSanitizeValuesAndBalanceIds() {
        float[] color={Float.NaN,Float.POSITIVE_INFINITY,-1,2};
        GuardedImGui.begin("colors");
        assertTrue(net.albiz.client.gui.SafeColorEditor.draw("test",color,0));
        assertArrayEquals(new float[]{1,1,0,1},color);
        assertFalse(net.albiz.client.gui.SafeColorEditor.draw("bad",new float[2],0));
        GuardedImGui.end();assertEquals(0,GuardedImGui.scopeMark());
    }
    @Test void itemEspPaletteWith1505EntriesOnlySubmitsVisibleRows() {
        var entries=new java.util.ArrayList<net.albiz.client.module.setting.ItemColorListSetting.ItemEntry>();
        for(int i=0;i<1505;i++)entries.add(new net.albiz.client.module.setting.ItemColorListSetting.ItemEntry("minecraft:test_"+i,"Item "+i,new imgui.type.ImBoolean(false),new float[]{1,0,0,1}));
        var palette=new net.albiz.client.module.setting.ItemColorListSetting("Item Colors",entries,new float[]{1,1,1,1});
        var editor=new net.albiz.client.gui.ItemPaletteEditor((dl,id,x,y,size)->{});editor.open(palette);editor.render();
        assertTrue(editor.renderedRows()>0);assertTrue(editor.renderedRows()<30,"must not submit all 1505 rows");
        assertEquals(0,GuardedImGui.scopeMark());editor.close();assertFalse(editor.isOpen());
    }
    @Test void itemPaletteCanFilterAndReopenWithoutLeakedNativeScopes() throws Exception {
        var entries=new java.util.ArrayList<net.albiz.client.module.setting.ItemColorListSetting.ItemEntry>();
        for(int i=0;i<1505;i++)entries.add(new net.albiz.client.module.setting.ItemColorListSetting.ItemEntry("minecraft:test_"+i,"Item "+i,new imgui.type.ImBoolean(false),new float[]{1,0,0,1}));
        var palette=new net.albiz.client.module.setting.ItemColorListSetting("Item Colors",entries,new float[]{1,1,1,1});
        var editor=new net.albiz.client.gui.ItemPaletteEditor((dl,id,x,y,size)->{});editor.open(palette);editor.render();
        ImGui.render();ImGui.newFrame();
        var field=editor.getClass().getDeclaredField("search");field.setAccessible(true);((imgui.type.ImString)field.get(editor)).set("1499");
        editor.render();assertEquals(1,editor.renderedRows());assertEquals(0,GuardedImGui.scopeMark());
        editor.close();editor.open(palette);editor.render();assertTrue(editor.renderedRows()<30);assertEquals(0,GuardedImGui.scopeMark());editor.close();
    }
    @Test void paletteLongNamesKeepSeparateIconRowsAndOnlyRenderVisibleIcons() {
        var entries=new java.util.ArrayList<net.albiz.client.module.setting.ItemColorListSetting.ItemEntry>();
        for(int i=0;i<1505;i++)entries.add(new net.albiz.client.module.setting.ItemColorListSetting.ItemEntry("minecraft:test_"+i,"Extremely long item name ".repeat(12),new imgui.type.ImBoolean(false),new float[]{1,0,0,1}));
        var count=new java.util.concurrent.atomic.AtomicInteger();
        float[] previousBottom={-Float.MAX_VALUE};
        var editor=new net.albiz.client.gui.ItemPaletteEditor((dl,id,x,y,size)->{
            assertEquals(32,size);assertTrue(y>=previousBottom[0]);previousBottom[0]=y+size;
            count.incrementAndGet();dl.addRectFilled(x,y,x+size,y+size,0xffffffff);
        });
        editor.open(new net.albiz.client.module.setting.ItemColorListSetting("Item Colors",entries,new float[]{1,1,1,1}));editor.render();
        assertTrue(count.get()>0 && count.get()<30);assertEquals(editor.renderedRows(),count.get());
        assertEquals(0,GuardedImGui.scopeMark());editor.close();
    }
}
