package net.albiz.client.imgui;

import imgui.*;
import imgui.type.*;
import imgui.callback.*;
import net.albiz.client.i18n.AlbizI18n;

/** Tracks only Albız-owned ImGui scopes. Recovery calls public imgui-java 1.90 APIs. */
public final class GuardedImGui extends imgui.ImGui {
    private static int[] scopes = new int[256];
    private static int size;
    private GuardedImGui() {}
    public static int scopeMark() { return size; }
    private static void pushed(int scope) {
        if (size == scopes.length) scopes = java.util.Arrays.copyOf(scopes, size * 2);
        scopes[size++] = scope;
    }
    private static void checkPop(int scope, int count) {
        if (scope >= 1 && scope <= 4) {
            for (int i = size - 1; i >= 0; i--) if (scopes[i] >= 1 && scopes[i] <= 4) {
                if (scopes[i] != scope) throw new IllegalStateException("Wrong ImGui window end: " + scope + " while " + scopes[i] + " is open");
                break;
            }
        }
        if (count < 0) throw new IllegalArgumentException("Negative ImGui pop count");
        int available = 0;
        for (int i = size - 1; i >= 0; i--) if (scopes[i] == scope) available++;
        if (count > available) throw new IllegalStateException("Albız ImGui scope underflow: " + scope);
    }
    private static void popped(int scope, int count) {
        for (int n = 0; n < count; n++) {
            for (int i = size - 1; i >= 0; i--) if (scopes[i] == scope) {
                System.arraycopy(scopes, i + 1, scopes, i, size - i - 1); size--; break;
            }
        }
    }
    /** Call inside the render catch, before outside fonts/styles are popped. */
    public static int recoverScopes(int mark) {
        if (mark < 0 || mark > size) throw new IllegalArgumentException("Invalid ImGui scope mark");
        int recovered = size - mark;
        while (size > mark) {
            int scope = scopes[--size];
            switch (scope) {
                case 1 -> imgui.ImGui.end();
                case 2 -> imgui.ImGui.endChild();
                case 3 -> imgui.ImGui.endPopup();
                case 4 -> imgui.ImGui.endTooltip();
                case 5 -> imgui.ImGui.endDisabled();
                case 6 -> imgui.ImGui.popStyleColor();
                case 7 -> imgui.ImGui.popStyleVar();
                case 8 -> imgui.ImGui.popFont();
                case 9 -> imgui.ImGui.popID();
                case 10 -> imgui.ImGui.popTextWrapPos();
                case 11 -> imgui.ImGui.popClipRect();
                default -> throw new IllegalStateException("Unknown scope");
            }
        }
        return recovered;
    }
    public static boolean begin(final String title) {
        boolean visible = imgui.ImGui.begin(AlbizI18n.label(title));
        pushed(1);
        return visible;
    }
    public static boolean begin(final String title, final ImBoolean pOpen) {
        boolean visible = imgui.ImGui.begin(AlbizI18n.label(title), pOpen);
        pushed(1);
        return visible;
    }
    public static boolean begin(final String title, final ImBoolean pOpen, final int imGuiWindowFlags) {
        boolean visible = imgui.ImGui.begin(AlbizI18n.label(title), pOpen, imGuiWindowFlags);
        pushed(1);
        return visible;
    }
    public static boolean begin(final String title, final int imGuiWindowFlags) {
        boolean visible = imgui.ImGui.begin(AlbizI18n.label(title), imGuiWindowFlags);
        pushed(1);
        return visible;
    }
    public static void end() {
        checkPop(1, 1);
        imgui.ImGui.end();
        popped(1, 1);
    }
    public static boolean beginChild(final String strId) {
        boolean visible = imgui.ImGui.beginChild(strId);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final ImVec2 size) {
        boolean visible = imgui.ImGui.beginChild(strId, size);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final float sizeX, final float sizeY) {
        boolean visible = imgui.ImGui.beginChild(strId, sizeX, sizeY);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final ImVec2 size, final int childFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, size, childFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final float sizeX, final float sizeY, final int childFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, sizeX, sizeY, childFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final ImVec2 size, final int childFlags, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, size, childFlags, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final float sizeX, final float sizeY, final int childFlags, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, sizeX, sizeY, childFlags, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final int childFlags, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, childFlags, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id) {
        boolean visible = imgui.ImGui.beginChild(id);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final ImVec2 size) {
        boolean visible = imgui.ImGui.beginChild(id, size);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final float sizeX, final float sizeY) {
        boolean visible = imgui.ImGui.beginChild(id, sizeX, sizeY);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final ImVec2 size, final int childFlags) {
        boolean visible = imgui.ImGui.beginChild(id, size, childFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final float sizeX, final float sizeY, final int childFlags) {
        boolean visible = imgui.ImGui.beginChild(id, sizeX, sizeY, childFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final ImVec2 size, final int childFlags, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(id, size, childFlags, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final float sizeX, final float sizeY, final int childFlags, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(id, sizeX, sizeY, childFlags, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final int childFlags, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(id, childFlags, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(id, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final ImVec2 size, final boolean border) {
        boolean visible = imgui.ImGui.beginChild(strId, size, border);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final float sizeX, final float sizeY, final boolean border) {
        boolean visible = imgui.ImGui.beginChild(strId, sizeX, sizeY, border);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final ImVec2 size, final boolean border, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, size, border, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final String strId, final float sizeX, final float sizeY, final boolean border, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(strId, sizeX, sizeY, border, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final ImVec2 size, final boolean border) {
        boolean visible = imgui.ImGui.beginChild(id, size, border);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final float sizeX, final float sizeY, final boolean border) {
        boolean visible = imgui.ImGui.beginChild(id, sizeX, sizeY, border);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final ImVec2 size, final boolean border, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(id, size, border, windowFlags);
        pushed(2);
        return visible;
    }
    public static boolean beginChild(final int id, final float sizeX, final float sizeY, final boolean border, final int windowFlags) {
        boolean visible = imgui.ImGui.beginChild(id, sizeX, sizeY, border, windowFlags);
        pushed(2);
        return visible;
    }
    public static void endChild() {
        checkPop(2, 1);
        imgui.ImGui.endChild();
        popped(2, 1);
    }
    public static void pushFont(final ImFont font) {
        imgui.ImGui.pushFont(font);
        pushed(8);
    }
    public static void popFont() {
        checkPop(8, 1);
        imgui.ImGui.popFont();
        popped(8, 1);
    }
    public static void pushStyleColor(int imGuiCol, int r, int g, int b, int a) {
        imgui.ImGui.pushStyleColor(imGuiCol, r, g, b, a);
        pushed(6);
    }
    public static void pushStyleColor(final int imGuiCol, final ImVec4 col) {
        imgui.ImGui.pushStyleColor(imGuiCol, col);
        pushed(6);
    }
    public static void pushStyleColor(final int imGuiCol, final float colX, final float colY, final float colZ, final float colW) {
        imgui.ImGui.pushStyleColor(imGuiCol, colX, colY, colZ, colW);
        pushed(6);
    }
    public static void pushStyleColor(final int imGuiCol, final int col) {
        imgui.ImGui.pushStyleColor(imGuiCol, col);
        pushed(6);
    }
    public static void popStyleColor() {
        checkPop(6, 1);
        imgui.ImGui.popStyleColor();
        popped(6, 1);
    }
    public static void popStyleColor(final int count) {
        checkPop(6, count);
        imgui.ImGui.popStyleColor(count);
        popped(6, count);
    }
    public static void pushStyleVar(final int imGuiStyleVar, final float val) {
        imgui.ImGui.pushStyleVar(imGuiStyleVar, val);
        pushed(7);
    }
    public static void pushStyleVar(final int imGuiStyleVar, final ImVec2 val) {
        imgui.ImGui.pushStyleVar(imGuiStyleVar, val);
        pushed(7);
    }
    public static void pushStyleVar(final int imGuiStyleVar, final float valX, final float valY) {
        imgui.ImGui.pushStyleVar(imGuiStyleVar, valX, valY);
        pushed(7);
    }
    public static void popStyleVar() {
        checkPop(7, 1);
        imgui.ImGui.popStyleVar();
        popped(7, 1);
    }
    public static void popStyleVar(final int count) {
        checkPop(7, count);
        imgui.ImGui.popStyleVar(count);
        popped(7, count);
    }
    public static void pushTextWrapPos() {
        imgui.ImGui.pushTextWrapPos();
        pushed(10);
    }
    public static void pushTextWrapPos(final float wrapLocalPosX) {
        imgui.ImGui.pushTextWrapPos(wrapLocalPosX);
        pushed(10);
    }
    public static void popTextWrapPos() {
        checkPop(10, 1);
        imgui.ImGui.popTextWrapPos();
        popped(10, 1);
    }
    public static void pushID(final String strId) {
        imgui.ImGui.pushID(strId);
        pushed(9);
    }
    public static void pushID(final String strIdBegin, final String strIdEnd) {
        imgui.ImGui.pushID(strIdBegin, strIdEnd);
        pushed(9);
    }
    public static void pushID(final long ptrId) {
        imgui.ImGui.pushID(ptrId);
        pushed(9);
    }
    public static void pushID(final int intId) {
        imgui.ImGui.pushID(intId);
        pushed(9);
    }
    public static void popID() {
        checkPop(9, 1);
        imgui.ImGui.popID();
        popped(9, 1);
    }
    public static void beginTooltip() {
        imgui.ImGui.beginTooltip();
        pushed(4);
    }
    public static void endTooltip() {
        checkPop(4, 1);
        imgui.ImGui.endTooltip();
        popped(4, 1);
    }
    public static boolean beginPopup(final String strId) {
        boolean visible = imgui.ImGui.beginPopup(strId);
        if (visible) pushed(3);
        return visible;
    }
    public static boolean beginPopup(final String strId, final int imGuiWindowFlags) {
        boolean visible = imgui.ImGui.beginPopup(strId, imGuiWindowFlags);
        if (visible) pushed(3);
        return visible;
    }
    public static void endPopup() {
        checkPop(3, 1);
        imgui.ImGui.endPopup();
        popped(3, 1);
    }
    public static void beginDisabled() {
        imgui.ImGui.beginDisabled();
        pushed(5);
    }
    public static void beginDisabled(final boolean disabled) {
        imgui.ImGui.beginDisabled(disabled);
        pushed(5);
    }
    public static void endDisabled() {
        checkPop(5, 1);
        imgui.ImGui.endDisabled();
        popped(5, 1);
    }
    public static void pushClipRect(final ImVec2 clipRectMin, final ImVec2 clipRectMax, final boolean intersectWithCurrentClipRect) {
        imgui.ImGui.pushClipRect(clipRectMin, clipRectMax, intersectWithCurrentClipRect);
        pushed(11);
    }
    public static void pushClipRect(final float clipRectMinX, final float clipRectMinY, final float clipRectMaxX, final float clipRectMaxY, final boolean intersectWithCurrentClipRect) {
        imgui.ImGui.pushClipRect(clipRectMinX, clipRectMinY, clipRectMaxX, clipRectMaxY, intersectWithCurrentClipRect);
        pushed(11);
    }
    public static void popClipRect() {
        checkPop(11, 1);
        imgui.ImGui.popClipRect();
        popped(11, 1);
    }

    // Presentation boundary: stored strings and ImGui IDs remain unchanged.
    public static void textUnformatted(final String text) { imgui.ImGui.textUnformatted(AlbizI18n.display(text)); }
    public static void text(final String text) { imgui.ImGui.text(AlbizI18n.display(text)); }
    public static void textDisabled(final String text) { imgui.ImGui.textDisabled(AlbizI18n.display(text)); }
    public static boolean button(final String label) { return imgui.ImGui.button(AlbizI18n.label(label)); }
    public static boolean button(final String label, final ImVec2 size) { return imgui.ImGui.button(AlbizI18n.label(label), size); }
    public static boolean button(final String label, final float sizeX, final float sizeY) { return imgui.ImGui.button(AlbizI18n.label(label), sizeX, sizeY); }
    public static boolean inputText(final String label, final ImString text) { return imgui.ImGui.inputText(AlbizI18n.label(label), text); }
    public static boolean inputText(final String label, final ImString text, final int imGuiInputTextFlags) { return imgui.ImGui.inputText(AlbizI18n.label(label), text, imGuiInputTextFlags); }
    public static boolean inputText(final String label, final ImString text, final int imGuiInputTextFlags, final ImGuiInputTextCallback callback) { return imgui.ImGui.inputText(AlbizI18n.label(label), text, imGuiInputTextFlags, callback); }
    public static boolean inputTextWithHint(final String label, final String hint, final ImString text) { return imgui.ImGui.inputTextWithHint(AlbizI18n.label(label), AlbizI18n.display(hint), text); }
    public static boolean inputTextWithHint(final String label, final String hint, final ImString text, final int imGuiInputTextFlags) { return imgui.ImGui.inputTextWithHint(AlbizI18n.label(label), AlbizI18n.display(hint), text, imGuiInputTextFlags); }
    public static boolean inputTextWithHint(final String label, final String hint, final ImString text, final int imGuiInputTextFlags, final ImGuiInputTextCallback callback) { return imgui.ImGui.inputTextWithHint(AlbizI18n.label(label), AlbizI18n.display(hint), text, imGuiInputTextFlags, callback); }
    public static boolean colorEdit4(final String label, final float[] col) { return net.albiz.client.gui.SafeColorEditor.draw(label,col,0); }
    public static boolean colorEdit4(final String label, final float[] col, final int imGuiColorEditFlags) { return net.albiz.client.gui.SafeColorEditor.draw(label,col,imGuiColorEditFlags); }
    public static boolean colorPicker4(final String label, final float[] col) { return net.albiz.client.gui.SafeColorEditor.draw(label,col,0); }
    public static boolean colorPicker4(final String label, final float[] col, final int imGuiColorEditFlags) { return net.albiz.client.gui.SafeColorEditor.draw(label,col,imGuiColorEditFlags); }
    public static boolean colorPicker4(final String label, final float[] col, final int imGuiColorEditFlags, final float[] refCol) { return net.albiz.client.gui.SafeColorEditor.draw(label,col,imGuiColorEditFlags); }
    public static boolean colorPicker4(final String label, final float[] col, final float[] refCol) { return net.albiz.client.gui.SafeColorEditor.draw(label,col,0); }
    public static boolean selectable(final String label) { return imgui.ImGui.selectable(AlbizI18n.label(label)); }
    public static boolean selectable(final String label, final boolean selected) { return imgui.ImGui.selectable(AlbizI18n.label(label), selected); }
    public static boolean selectable(final String label, final boolean selected, final int imGuiSelectableFlags) { return imgui.ImGui.selectable(AlbizI18n.label(label), selected, imGuiSelectableFlags); }
    public static boolean selectable(final String label, final boolean selected, final int imGuiSelectableFlags, final ImVec2 size) { return imgui.ImGui.selectable(AlbizI18n.label(label), selected, imGuiSelectableFlags, size); }
    public static boolean selectable(final String label, final boolean selected, final int imGuiSelectableFlags, final float sizeX, final float sizeY) { return imgui.ImGui.selectable(AlbizI18n.label(label), selected, imGuiSelectableFlags, sizeX, sizeY); }
    public static boolean selectable(final String label, final int imGuiSelectableFlags, final ImVec2 size) { return imgui.ImGui.selectable(AlbizI18n.label(label), imGuiSelectableFlags, size); }
    public static boolean selectable(final String label, final int imGuiSelectableFlags, final float sizeX, final float sizeY) { return imgui.ImGui.selectable(AlbizI18n.label(label), imGuiSelectableFlags, sizeX, sizeY); }
    public static boolean selectable(final String label, final ImVec2 size) { return imgui.ImGui.selectable(AlbizI18n.label(label), size); }
    public static boolean selectable(final String label, final float sizeX, final float sizeY) { return imgui.ImGui.selectable(AlbizI18n.label(label), sizeX, sizeY); }
    public static boolean selectable(final String label, final boolean selected, final ImVec2 size) { return imgui.ImGui.selectable(AlbizI18n.label(label), selected, size); }
    public static boolean selectable(final String label, final boolean selected, final float sizeX, final float sizeY) { return imgui.ImGui.selectable(AlbizI18n.label(label), selected, sizeX, sizeY); }
    public static boolean selectable(final String label, final ImBoolean pSelected) { return imgui.ImGui.selectable(AlbizI18n.label(label), pSelected); }
    public static boolean selectable(final String label, final ImBoolean pSelected, final int imGuiSelectableFlags) { return imgui.ImGui.selectable(AlbizI18n.label(label), pSelected, imGuiSelectableFlags); }
    public static boolean selectable(final String label, final ImBoolean pSelected, final int imGuiSelectableFlags, final ImVec2 size) { return imgui.ImGui.selectable(AlbizI18n.label(label), pSelected, imGuiSelectableFlags, size); }
    public static boolean selectable(final String label, final ImBoolean pSelected, final int imGuiSelectableFlags, final float sizeX, final float sizeY) { return imgui.ImGui.selectable(AlbizI18n.label(label), pSelected, imGuiSelectableFlags, sizeX, sizeY); }
    public static boolean selectable(final String label, final ImBoolean pSelected, final ImVec2 size) { return imgui.ImGui.selectable(AlbizI18n.label(label), pSelected, size); }
    public static boolean selectable(final String label, final ImBoolean pSelected, final float sizeX, final float sizeY) { return imgui.ImGui.selectable(AlbizI18n.label(label), pSelected, sizeX, sizeY); }
    public static ImVec2 calcTextSize(final String text) { return imgui.ImGui.calcTextSize(AlbizI18n.display(text)); }
    public static void calcTextSize(final ImVec2 dst, final String text) { imgui.ImGui.calcTextSize(dst, AlbizI18n.display(text)); }
    public static ImVec2 calcTextSize(final String text, final boolean hideTextAfterDoubleHash) { return imgui.ImGui.calcTextSize(AlbizI18n.display(text), hideTextAfterDoubleHash); }
    public static void calcTextSize(final ImVec2 dst, final String text, final boolean hideTextAfterDoubleHash) { imgui.ImGui.calcTextSize(dst, AlbizI18n.display(text), hideTextAfterDoubleHash); }
    public static ImVec2 calcTextSize(final String text, final boolean hideTextAfterDoubleHash, final float wrapWidth) { return imgui.ImGui.calcTextSize(AlbizI18n.display(text), hideTextAfterDoubleHash, wrapWidth); }
    public static void calcTextSize(final ImVec2 dst, final String text, final boolean hideTextAfterDoubleHash, final float wrapWidth) { imgui.ImGui.calcTextSize(dst, AlbizI18n.display(text), hideTextAfterDoubleHash, wrapWidth); }
    public static ImVec2 calcTextSize(final String text, final float wrapWidth) { return imgui.ImGui.calcTextSize(AlbizI18n.display(text), wrapWidth); }
    public static void calcTextSize(final ImVec2 dst, final String text, final float wrapWidth) { imgui.ImGui.calcTextSize(dst, AlbizI18n.display(text), wrapWidth); }
}
