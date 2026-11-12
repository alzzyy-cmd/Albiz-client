package net.albiz.client.util.staff;

import java.util.ArrayDeque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1113;
import net.minecraft.class_1144;
import net.minecraft.class_310;
import net.minecraft.class_3414;

@Environment(EnvType.CLIENT)
public final class StaffAlarm {
   public static final String BEEP = "Beep";
   public static final String BELL = "Bell";
   public static final String GUARDIAN = "Guardian";
   public static final String OFF = "Off";
   private static final int SIREN_INTERVAL = 4;
   private static final float SIREN_HIGH = 1.5F;
   private static final float SIREN_LOW = 1.0F;
   private static final int BEEP_INTERVAL = 3;
   private static final float BEEP_PITCH = 1.6F;
   private static final int BELL_INTERVAL = 10;
   private static final int GUARDIAN_INTERVAL = 60;
   private static final int MAX_RINGING = 8;
   private String mode = "Off";
   private float volume;
   private int ticksLeft;
   private int step;
   private final ArrayDeque<class_1113> ringing = new ArrayDeque<>(8);

   public void start(String mode, float seconds, float volume) {
      if (mode != null && !"Off".equals(mode) && !(volume <= 0.0F) && !(seconds <= 0.0F)) {
         int ticks = Math.max(1, Math.round(seconds * 20.0F));
         this.volume = Math.min(1.0F, volume);
         if (this.isPlaying() && mode.equals(this.mode)) {
            this.ticksLeft = Math.max(this.ticksLeft, ticks);
         } else {
            this.stop();
            this.mode = mode;
            this.ticksLeft = ticks;
            this.step = 0;
         }
      }
   }

   public void tick() {
      if (this.ticksLeft > 0) {
         int interval = interval(this.mode);
         if (this.step % interval == 0) {
            this.play(this.step / interval);
         }

         this.step++;
         if (--this.ticksLeft == 0) {
            this.step = 0;
         }
      }
   }

   public void stop() {
      this.ticksLeft = 0;
      this.step = 0;
      if (!this.ringing.isEmpty()) {
         class_1144 sounds = class_310.method_1551().method_1483();
         if (sounds != null) {
            this.ringing.forEach(sounds::method_4870);
         }

         this.ringing.clear();
      }
   }

   public boolean isPlaying() {
      return this.ticksLeft > 0;
   }

   private void play(int note) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: invalid constant type: Lnet/minecraft/class_1113; with value Beep
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.toJava(ConstExprent.java:356)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement.toJava(SwitchStatement.java:171)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:829)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:829)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:238)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1306)
      //
      // Bytecode:
      // 000: invokestatic net/minecraft/class_310.method_1551 ()Lnet/minecraft/class_310;
      // 003: invokevirtual net/minecraft/class_310.method_1483 ()Lnet/minecraft/class_1144;
      // 006: astore 2
      // 007: aload 2
      // 008: ifnonnull 00c
      // 00b: return
      // 00c: aload 0
      // 00d: getfield net/albiz/client/util/staff/StaffAlarm.mode Ljava/lang/String;
      // 010: astore 5
      // 012: bipush -1
      // 013: istore 6
      // 015: aload 5
      // 017: invokevirtual java/lang/String.hashCode ()I
      // 01a: lookupswitch 79 3 -1465377359 66 2066510 34 2066723 50
      // 03c: aload 5
      // 03e: ldc "Beep"
      // 040: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 043: ifeq 069
      // 046: bipush 0
      // 047: istore 6
      // 049: goto 069
      // 04c: aload 5
      // 04e: ldc "Bell"
      // 050: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 053: ifeq 069
      // 056: bipush 1
      // 057: istore 6
      // 059: goto 069
      // 05c: aload 5
      // 05e: ldc "Guardian"
      // 060: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 063: ifeq 069
      // 066: bipush 2
      // 067: istore 6
      // 069: iload 6
      // 06b: tableswitch 62 0 2 25 42 52
      // 084: getstatic net/minecraft/class_3417.field_18311 Lnet/minecraft/class_6880$class_6883;
      // 087: invokevirtual net/minecraft/class_6880$class_6883.comp_349 ()Ljava/lang/Object;
      // 08a: checkcast net/minecraft/class_3414
      // 08d: astore 3
      // 08e: ldc 1.6
      // 090: fstore 4
      // 092: goto 0c1
      // 095: getstatic net/minecraft/class_3417.field_17265 Lnet/minecraft/class_3414;
      // 098: astore 3
      // 099: fconst_1
      // 09a: fstore 4
      // 09c: goto 0c1
      // 09f: getstatic net/minecraft/class_3417.field_15203 Lnet/minecraft/class_3414;
      // 0a2: astore 3
      // 0a3: fconst_1
      // 0a4: fstore 4
      // 0a6: goto 0c1
      // 0a9: getstatic net/minecraft/class_3417.field_14622 Lnet/minecraft/class_6880$class_6883;
      // 0ac: invokevirtual net/minecraft/class_6880$class_6883.comp_349 ()Ljava/lang/Object;
      // 0af: checkcast net/minecraft/class_3414
      // 0b2: astore 3
      // 0b3: iload 1
      // 0b4: bipush 2
      // 0b5: irem
      // 0b6: ifne 0be
      // 0b9: ldc 1.5
      // 0bb: goto 0bf
      // 0be: fconst_1
      // 0bf: fstore 4
      // 0c1: new net/minecraft/class_1109
      // 0c4: dup
      // 0c5: aload 3
      // 0c6: invokevirtual net/minecraft/class_3414.comp_3319 ()Lnet/minecraft/class_2960;
      // 0c9: getstatic net/minecraft/class_3419.field_15250 Lnet/minecraft/class_3419;
      // 0cc: aload 0
      // 0cd: getfield net/albiz/client/util/staff/StaffAlarm.volume F
      // 0d0: fload 4
      // 0d2: invokestatic net/minecraft/class_1113.method_43221 ()Lnet/minecraft/class_5819;
      // 0d5: bipush 0
      // 0d6: bipush 0
      // 0d7: getstatic net/minecraft/class_1113$class_1114.field_5478 Lnet/minecraft/class_1113$class_1114;
      // 0da: dconst_0
      // 0db: dconst_0
      // 0dc: dconst_0
      // 0dd: bipush 1
      // 0de: invokespecial net/minecraft/class_1109.<init> (Lnet/minecraft/class_2960;Lnet/minecraft/class_3419;FFLnet/minecraft/class_5819;ZILnet/minecraft/class_1113$class_1114;DDDZ)V
      // 0e1: astore 5
      // 0e3: aload 0
      // 0e4: getfield net/albiz/client/util/staff/StaffAlarm.ringing Ljava/util/ArrayDeque;
      // 0e7: invokevirtual java/util/ArrayDeque.size ()I
      // 0ea: bipush 8
      // 0ec: if_icmplt 0f7
      // 0ef: aload 0
      // 0f0: getfield net/albiz/client/util/staff/StaffAlarm.ringing Ljava/util/ArrayDeque;
      // 0f3: invokevirtual java/util/ArrayDeque.removeFirst ()Ljava/lang/Object;
      // 0f6: pop
      // 0f7: aload 0
      // 0f8: getfield net/albiz/client/util/staff/StaffAlarm.ringing Ljava/util/ArrayDeque;
      // 0fb: aload 5
      // 0fd: invokevirtual java/util/ArrayDeque.addLast (Ljava/lang/Object;)V
      // 100: aload 2
      // 101: aload 5
      // 103: invokevirtual net/minecraft/class_1144.method_4873 (Lnet/minecraft/class_1113;)Lnet/minecraft/class_1140$class_11518;
      // 106: pop
      // 107: return
   }

   private static int interval(String mode) {
      return switch (mode) {
         case "Beep" -> 3;
         case "Bell" -> 10;
         case "Guardian" -> 60;
         default -> 4;
      };
   }
}
