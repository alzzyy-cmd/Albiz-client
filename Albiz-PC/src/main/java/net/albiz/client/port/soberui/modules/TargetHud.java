// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;

public class TargetHud
extends Module {
    private static volatile TargetHud instance;
    public final Setting<Boolean> mobs = this.add(new BoolSetting("Mobs", "Also shows the card for mobs you fight, not only players.", false));
    public final Setting<Integer> size = this.add(new IntSetting("Size", "Size of the card in percent.", 100, 60, 160));
    public final Setting<Integer> linger = this.add(new IntSetting("Linger", "Seconds the card stays after the last hit.", 6, 2, 15));
    private class_1309 target;
    private boolean fighting;
    private long lastFight;
    private int hits;
    private int taken;
    private int combo;
    private long comboAt;
    private float health = -1.0f;
    private float damage;
    private long damageAt;
    private boolean eliminated;
    private long eliminatedAt;

    public TargetHud() {
        super("Target HUD", "A card pinned right beside whoever you fight, with health, weapon, distance, combo and hits.", Category.VISUALS);
        instance = this;
    }

    public static TargetHud active() {
        TargetHud module = instance;
        return module != null && module.isActive() ? module : null;
    }

    @Override
    public void onDisable() {
        this.target = null;
        this.fighting = false;
    }

    @Override
    public void onGameLeft() {
        this.target = null;
        this.fighting = false;
    }

    private boolean accepts(class_1297 entity) {
        return entity instanceof class_1657 || entity instanceof class_1309 && this.mobs.get() != false;
    }

    private void focus(class_1309 living) {
        if (this.target != living) {
            this.target = living;
            this.combo = 0;
            this.taken = 0;
            this.hits = 0;
            this.health = living.method_6032();
            this.damage = 0.0f;
            this.damageAt = 0L;
            this.eliminated = false;
        }
        this.fighting = true;
        this.lastFight = System.currentTimeMillis();
    }

    public static void onDamage(int victim, int attacker) {
        class_1309 living;
        class_1297 attackerEntity;
        TargetHud module = TargetHud.active();
        class_310 mc = class_310.method_1551();
        if (module == null || mc.field_1724 == null || mc.field_1687 == null || attacker < 0) {
            return;
        }
        int me = mc.field_1724.method_5628();
        if (attacker == me && victim != me) {
            class_1309 living22;
            class_1297 class_12972 = mc.field_1687.method_8469(victim);
            if (class_12972 instanceof class_1309 && module.accepts((class_1297)(living22 = (class_1309)class_12972))) {
                module.focus(living22);
                ++module.hits;
                ++module.combo;
                module.comboAt = System.currentTimeMillis();
            }
        } else if (victim == me && (attackerEntity = mc.field_1687.method_8469(attacker)) instanceof class_1309 && module.accepts((class_1297)(living = (class_1309)attackerEntity))) {
            boolean free;
            boolean bl = free = module.target == null || !module.fighting || module.eliminated;
            if (free || module.target == living) {
                module.focus(living);
                ++module.taken;
                module.combo = 0;
            }
        }
    }

    @Override
    public void onTick() {
        if (this.target == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.target.method_31481() && !this.eliminated) {
            this.fighting = false;
            return;
        }
        float hp = this.target.method_6032();
        if (this.health >= 0.0f && hp < this.health - 0.05f) {
            this.damage = this.health - hp;
            this.damageAt = now;
        }
        this.health = hp;
        if (!this.eliminated && (this.target.method_29504() || hp <= 0.0f)) {
            this.eliminated = true;
            this.eliminatedAt = now;
        }
        if (this.eliminated && now - this.eliminatedAt > 3400L) {
            this.fighting = false;
        }
        if (!this.eliminated && now - this.lastFight > (long)this.linger.get().intValue() * 1000L) {
            this.fighting = false;
        }
    }

    public class_1309 target() {
        return this.target;
    }

    public boolean fighting() {
        return this.fighting && this.target != null;
    }

    public void release() {
        if (!this.fighting) {
            this.target = null;
        }
    }

    public int hits() {
        return this.hits;
    }

    public int taken() {
        return this.taken;
    }

    public int combo() {
        return this.combo;
    }

    public long comboAt() {
        return this.comboAt;
    }

    public float damage() {
        return this.damage;
    }

    public long damageAt() {
        return this.damageAt;
    }

    public boolean eliminated() {
        return this.eliminated;
    }

    public long eliminatedAt() {
        return this.eliminatedAt;
    }

    @Override
    public boolean showInHud() {
        return false;
    }
}

