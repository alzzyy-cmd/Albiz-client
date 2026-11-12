import unittest,re,hashlib,json,zipfile
from pathlib import Path
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/net/albiz/client';GUI=J/'gui/ClickGuiRenderer.java'
class OriginalAnubisGui(unittest.TestCase):
 def test_primary_module_page_calls_original_panel_renderer(self):
  text=GUI.read_text();branch=text.split('case MODULES:',1)[1].split('case HUD:',1)[0]
  self.assertIn('this.renderModulePanels(dt, alpha);',branch);self.assertIn('this.renderModuleCard',branch)
  for obsolete in ['renderLibrary','##albiz_library','libraryCategory','libraryFavorites']:self.assertNotIn(obsolete,text)
 def test_independent_categories_search_folding_and_saved_drag_positions(self):
  text=GUI.read_text();panels=text.split('private void renderModulePanels(',1)[1].split('private ClickGuiRenderer.CategoryPanel[] visiblePanels()',1)[0]
  for call in ['this.visiblePanels()','this.fitPanels(panels)','this.renderCategoryPanel(panel, dt, alpha)','this.renderSearchPanel(dt, alpha)']:self.assertIn(call,panels)
  category=text.split('private void renderCategoryPanel(',1)[1].split('private float unfolded(',1)[0]
  for call in ['this.beginPanel(panel.id(), alpha)','this.renderFoldControl','this.renderRowList']:self.assertIn(call,category)
  self.assertIn('setPanelHomes(this.panelHomes)',text);self.assertIn('GuardedImGui.isItemClicked(1)',text)
 def test_original_bottom_tabs_and_current_native_hud_are_both_retained(self):
  text=GUI.read_text();tabs=text.split('private static enum Tab {',1)[1]
  for name in ['MODULES','HUD','FRIENDS','THEME','CONFIG']:self.assertIn(name+'(',tabs)
  self.assertIn('nativeHudControls',text);self.assertIn('renderNativeMenuOptions(pad,contentW)',text)
if __name__=='__main__':unittest.main()
