package net.albiz.client.util;
/** Identity, not dimension-key equality: respawn can replace a world with the same key. */
public final class WorldIdentityGuard<T> {
 private T world;
 public boolean update(T next){if(world==next)return false;world=next;return true;}
 public boolean matches(T next){return world==next;}
}
