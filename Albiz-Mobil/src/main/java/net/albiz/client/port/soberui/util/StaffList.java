package net.albiz.client.port.soberui.util;

import com.mojang.authlib.GameProfile;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.modules.Hud;
import net.albiz.client.util.staff.StaffMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1934;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;

public final class StaffList {
    private static final Pattern STAFF_TAG = Pattern.compile("(?i)(?<![a-z])(?:owner|admin(?:istrator)?|sr[ ._-]?mod(?:erator)?|mod(?:erator)?|helper|staff|support|developer|dev|operator)(?![a-z])");
    private static final Pattern ACCOUNT_NAME = Pattern.compile("[.*]?[A-Za-z0-9_]{1,16}");
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int SETTLE_TICKS = 60;
    private static final int GROUP_THRESHOLD = 3;
    private static List<Member> cache = List.of();
    private static Object cacheHandler;
    private static String cacheConfig;
    private static long scanned;
    private static StaffMatcher ranks;
    private static String ranksConfig;
    private static Object alertHandler;
    private static Map<UUID, Member> tracked;
    private static int settleTicks;
    private static int ticksToScan;
    private static boolean primed;

    private StaffList() {
    }

    public static List<Member> online(class_310 mc, Hud hud) {
        if (mc.method_1562() == null || hud == null) {
            return List.of();
        }
        if(net.albiz.client.module.impl.misc.StaffHudModule.shouldShowHud()){
            java.util.List<Member> direct=new java.util.ArrayList<>();
            for(var row:net.albiz.client.module.impl.misc.StaffHudModule.hudRows().rows()){var entry=mc.method_1562().method_2871(row.id());if(entry!=null)direct.add(new Member(entry,row.presence().name(),0));}
            return java.util.List.copyOf(direct);
        }
        long now = System.currentTimeMillis();
        String config = hud.staffNames.get();
        if (cacheHandler == mc.method_1562() && cacheConfig.equals(config) && now - scanned < 1000L) {
            return cache;
        }
        HashSet<String> known = new HashSet<>();
        for (String part : config.split("[,;\\s]+")) {
            if (!part.isBlank()) known.add(part.trim().toLowerCase(Locale.ROOT));
        }
        ArrayList<Member> result = new ArrayList<>();
        for (class_640 entry : mc.method_1562().method_45732()) {
            String name = entry.method_2966().name();
            if (name != null && known.contains(name.toLowerCase(Locale.ROOT)))
                result.add(new Member(entry, "Staff", 0));
        }
        result.sort(Comparator.comparing(m -> m.entry().method_2966().name(), String.CASE_INSENSITIVE_ORDER));
        cacheHandler = mc.method_1562();
        cacheConfig = config;
        scanned = now;
        cache = List.copyOf(result);
        return cache;
    }

    public static void tick(class_310 mc, Hud hud) {
        class_634 connection = mc.method_1562();
        if (connection == null || mc.field_1724 == null || hud == null || !hud.staffList.get().booleanValue() || !hud.staffAlerts.get().booleanValue()) {
            tracked.clear();
            alertHandler = null;
            primed = false;
            return;
        }
        if (connection != alertHandler) {
            alertHandler = connection;
            tracked.clear();
            settleTicks = 60;
            primed = false;
        }
        if (settleTicks > 0) {
            --settleTicks;
        }
        if (--ticksToScan > 0) {
            return;
        }
        ticksToScan = 10;
        LinkedHashMap<UUID, Member> current = new LinkedHashMap<UUID, Member>();
        for (Member member : StaffList.online(mc, hud)) {
            current.put(member.entry().method_2966().id(), member);
        }
        ArrayList<Member> joined = new ArrayList<Member>();
        ArrayList<Member> left = new ArrayList<Member>();
        for (Map.Entry entry : current.entrySet()) {
            if (tracked.containsKey(entry.getKey())) continue;
            joined.add((Member)entry.getValue());
        }
        for (Map.Entry<UUID, Member> entry : tracked.entrySet()) {
            if (current.containsKey(entry.getKey())) continue;
            left.add((Member)entry.getValue());
        }
        tracked = current;
        if (!primed) {
            if (settleTicks == 0) {
                primed = true;
            }
            return;
        }
        StaffList.announce(joined, "joined", Notifications.Type.WARNING);
        StaffList.announce(left, "left", Notifications.Type.SUCCESS);
    }

    private static void announce(List<Member> group, String verb, Notifications.Type type) {
        if (group.isEmpty()) {
            return;
        }
        if (group.size() > 3) {
            ArrayList<String> labels = new ArrayList<String>(group.size());
            for (Member member : group) {
                labels.add(StaffList.label(member));
            }
            Notifications.push("Staff", group.size() + " staff " + verb + ": " + String.join((CharSequence)", ", labels), type);
            return;
        }
        for (Member member : group) {
            Notifications.push("Staff", StaffList.label(member) + " " + verb, type);
        }
    }

    private static String label(Member member) {
        return member.name() + " (" + member.role() + ")";
    }

    private static StaffMatcher matcher(String config) {
        if (ranks == null || !config.equals(ranksConfig)) {
            ranks = new StaffMatcher("Ranks + Markers + Team", "owner, co-owner, manager, admin, administrator, developer, dev, sr-admin, sr-mod, senior-mod, moderator, mod, trial-mod, sr-helper, senior-helper, helper, trial-helper, support, staff", "U+2605 U+2606 U+2726 U+2727 U+272A U+272F U+2730 U+2B50", false, 5, config, "");
            ranksConfig = config;
        }
        return ranks;
    }

    private static boolean byRank(StaffMatcher.Match match) {
        String rank = match.rank();
        return !rank.equals("Staff") && !rank.matches("\\d{2}");
    }

    private static boolean isAccount(UUID id, String name) {
        return name != null && id.version() != 2 && ACCOUNT_NAME.matcher(name).matches();
    }

    private static String plain(class_2561 text) {
        return text == null ? null : text.getString();
    }

    private static String labels(class_310 mc, class_640 entry) {
        class_268 team;
        StringBuilder labels = new StringBuilder(mc.field_1705.method_1750().method_1918(entry).getString());
        if (entry.method_2971() != null) {
            labels.append(' ').append(entry.method_2971().getString());
        }
        if ((team = entry.method_2955()) != null) {
            labels.append(' ').append(team.method_1144().getString()).append(' ').append(team.method_1136().getString()).append(' ').append(team.method_1197());
        }
        return labels.toString();
    }

    private static String legacyRole(String labels) {
        Matcher match = STAFF_TAG.matcher(labels);
        if (!match.find()) {
            return "Staff";
        }
        String role = match.group().toLowerCase(Locale.ROOT);
        if (role.contains("owner")) {
            return "Owner";
        }
        if (role.contains("admin")) {
            return "Admin";
        }
        if (role.contains("mod")) {
            return "Mod";
        }
        if (role.contains("helper") || role.contains("support")) {
            return "Helper";
        }
        if (role.contains("dev")) {
            return "Dev";
        }
        return "Staff";
    }

    static {
        cacheConfig = "";
        tracked = new LinkedHashMap<UUID, Member>();
    }

    public record Member(class_640 entry, String role, int seniority) {
        public String name() {
            return this.entry.method_2966().name();
        }
    }
}

