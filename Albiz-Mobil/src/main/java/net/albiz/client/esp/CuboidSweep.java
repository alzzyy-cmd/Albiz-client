package net.albiz.client.esp;

/** Bounded resumable traversal; a full pass visits every cell exactly once. */
public final class CuboidSweep {
 private final int radius,side,total;
 private int cursor;
 public CuboidSweep(int radius){if(radius<1 || radius>64)throw new IllegalArgumentException("radius");this.radius=radius;side=2*radius+1;total=side*side*side;}
 public int total(){return total;}
 public int next(){int value=cursor;if(++cursor==total)cursor=0;return value;}
 public int x(int i){return i%side-radius;}
 public int y(int i){return i/side%side-radius;}
 public int z(int i){return i/(side*side)-radius;}
}
