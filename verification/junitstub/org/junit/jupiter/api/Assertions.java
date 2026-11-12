package org.junit.jupiter.api;
import java.util.Objects;
public final class Assertions {
 public interface Executable { void execute() throws Throwable; }
 private static void fail(String m){throw new AssertionError(m);}
 public static void assertTrue(boolean c){if(!c)fail("expected true");}
 public static void assertTrue(boolean c,String m){if(!c)fail(m);}
 public static void assertFalse(boolean c){if(c)fail("expected false");}
 public static void assertFalse(boolean c,String m){if(c)fail(m);}
 public static void assertNull(Object o){if(o!=null)fail("expected null but "+o);}
 public static void assertNotNull(Object o){if(o==null)fail("expected non-null");}
 public static void assertEquals(Object e,Object a){if(!Objects.equals(e,a))fail("expected <"+e+"> but was <"+a+">");}
 public static void assertEquals(Object e,Object a,String m){if(!Objects.equals(e,a))fail(m+": expected <"+e+"> but was <"+a+">");}
 public static void assertEquals(long e,long a){if(e!=a)fail("expected <"+e+"> but was <"+a+">");}
 public static void assertEquals(long e,long a,String m){if(e!=a)fail(m+": expected <"+e+"> but was <"+a+">");}
 public static void assertEquals(double e,double a,double d){if(Math.abs(e-a)>d)fail("expected <"+e+"> but was <"+a+">");}
 public static void assertEquals(float e,float a,float d){if(Math.abs(e-a)>d)fail("expected <"+e+"> but was <"+a+">");}
 public static void assertNotEquals(Object e,Object a){if(Objects.equals(e,a))fail("expected not equal <"+a+">");}
 public static void assertSame(Object e,Object a){if(e!=a)fail("expected same");}
 public static <T extends Throwable> T assertThrows(Class<T> t,Executable e){try{e.execute();}catch(Throwable x){if(t.isInstance(x))return t.cast(x);throw new AssertionError("unexpected "+x,x);}fail("expected "+t.getName());return null;}
}
