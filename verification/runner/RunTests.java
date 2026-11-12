import java.lang.reflect.*;
public class RunTests { public static void main(String[] a) throws Exception { int pass=0,fail=0;
 for(String cn:a){Class<?> c=Class.forName(cn);for(Method m:c.getDeclaredMethods()){ if(!m.isAnnotationPresent(org.junit.jupiter.api.Test.class))continue; m.setAccessible(true);
   try{Constructor<?> k=c.getDeclaredConstructor();k.setAccessible(true);m.invoke(k.newInstance());pass++;}catch(InvocationTargetException e){fail++;System.out.println("FAIL "+cn+"."+m.getName()+": "+e.getCause());}}}
 System.out.println("tests passed="+pass+" failed="+fail); if(fail>0)System.exit(1);} }
