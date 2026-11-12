package net.albiz.client.module.impl.misc;
import java.util.*;import net.albiz.client.event.*;import net.albiz.client.event.impl.TickEvent;import net.albiz.client.module.*;import net.albiz.client.module.setting.*;import net.albiz.client.util.staff.StaffMatcher;import net.minecraft.*;
/** Generic tab evidence, not a server-specific list or vanished-player assertion. */
public final class StaffHudModule extends ClientModule {
 private final StringSetting names=addSetting("names",new StringSetting("Staff Names","").hint("name1,name2"));
 private final StringSetting ignored=addSetting("ignored",new StringSetting("Ignored Names",""));
 private List<HudRow> rows=List.of();private Object world;private int ticks;
 public StaffHudModule(){super("staff-hud","Staff HUD","Lists only explicitly configured staff names visible in the tab list; does not prove vanish.",ModuleCategory.MISC);}
 protected void onDisable(){rows=List.of();world=null;}
 @EventHandler public void tick(TickEvent event){
  var mc=class_310.method_1551();if(world!=mc.field_1687){world=mc.field_1687;rows=List.of();ticks=0;}if(mc.field_1687==null||mc.method_1562()==null){rows=List.of();return;}if(ticks++%10!=0)return;
  var known=manualNames(names.get());var excluded=manualNames(ignored.get());List<HudRow> out=new ArrayList<>();
  for(var info:mc.method_1562().method_2880()){
   var profile=info.method_2966();String name=profile.name();var team=info.method_2955();
   if(!known.contains(name.toLowerCase(Locale.ROOT))||excluded.contains(name.toLowerCase(Locale.ROOT)))continue;
   out.add(new HudRow(name,profile.id(),info.method_2958()==class_1934.field_9219?Presence.SPECTATOR:Presence.ONLINE,false,0,false,0,0,false));
  }out.sort(Comparator.comparing(HudRow::name,String.CASE_INSENSITIVE_ORDER));rows=List.copyOf(out);
 }
 private static Set<String> manualNames(String text){Set<String> out=new HashSet<>();for(String n:text.split("[,;\\s]+"))if(!n.isBlank())out.add(n.trim().toLowerCase(Locale.ROOT));return out;}
 private static String plain(class_2561 t){return t==null?"":t.getString();}
 public static boolean shouldShowHud(){var m=ModuleRegistry.get(StaffHudModule.class);return m!=null&&m.isEnabled();}
 public static HudList hudRows(){var m=ModuleRegistry.get(StaffHudModule.class);if(m==null||!m.isEnabled())return new HudList(List.of(),0);int n=Math.min(8,m.rows.size());return new HudList(m.rows.subList(0,n),m.rows.size()-n);}
 public enum Presence{ONLINE,SPECTATOR,VANISHED}
 public record HudList(List<HudRow> rows,int more){}
 public record HudRow(String name,UUID id,Presence presence,boolean watching,long watchingForMs,boolean nearby,float distance,float alertLevel,boolean nameHidden){public String watchingFor(){return "";}public String distanceLabel(){return "";}}
}
