package net.albiz.client.port.soberui;
import java.util.*;import net.albiz.client.port.soberui.module.Module;import net.albiz.client.port.soberui.module.Category;
public final class UiModules{
 private final Map<Class<?>,Module> typed=new HashMap<>();
 private final List<Module> modules=new ArrayList<>(),hidden=new ArrayList<>();public void addHidden(Module m){hidden.add(m);typed.putIfAbsent(m.getClass(),m);}
 public List<Module> all(){return List.copyOf(modules);}public List<Module> inCategory(Category c){return modules.stream().filter(x->x.category==c).toList();}
 public <T>T get(Class<T> c){if(c==net.albiz.client.module.impl.client.SpotifyHudModule.class)return c.cast(net.albiz.client.module.ModuleRegistry.get(net.albiz.client.module.impl.client.SpotifyHudModule.class));var known=typed.get(c);if(known!=null)return c.cast(known);for(Module m:modules)if(c.isInstance(m)){typed.put(c,m);return c.cast(m);}for(Module m:hidden)if(c.isInstance(m)){typed.put(c,m);return c.cast(m);}return null;}
 public Module byName(String n){return modules.stream().filter(x->x.name.equalsIgnoreCase(n)).findFirst().orElse(null);}
 public void add(Module m){modules.add(m);typed.putIfAbsent(m.getClass(),m);}public boolean isBound(int key){return modules.stream().anyMatch(m->m.getKey()==key);}
}
