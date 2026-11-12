package net.albiz.client.donutbets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.albiz.client.chat.ModuleChat;
import net.albiz.client.event.EventPriority;
import net.albiz.client.event.Stage;
import net.albiz.client.event.impl.TickEvent;
import net.albiz.client.gui.ChallengeToast;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_124;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_9334;
import net.minecraft.class_2558.class_10608;
import net.minecraft.class_2568.class_10613;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class DonutBetsFeed {
   private static final Logger LOGGER = LoggerFactory.getLogger("Albiz");
   private static final URI FEED = URI.create("https://donutbets.net/api/client/feed");
   private static final String SITE = "donutbets.net";
   static final int MAX_BYTES = 65536;
   private static final int MAX_EVENTS = 20;
   private static final long POLL_MILLIS = 55000L;
   private static final long POLL_JITTER_MILLIS = 10000L;
   private static final long FIRST_POLL_MILLIS = 15000L;
   private static final long READ_DEADLINE_SECONDS = 12L;
   private static final int HURT_QUIET_TICKS = 200;
   private static final int ID_MAX = 80;
   private static final int TITLE_MAX = 100;
   private static final int TEXT_MAX = 300;
   private static final int SEEN_MAX = 300;
   private static final String TOP_CENTER = "Top Center";
   private static final String TOP_LEFT = "Top Left";
   private static final String TOP_RIGHT = "Top Right";
   private static final long SOUND_GAP_MILLIS = 10000L;
   private static final String FANFARE = "Fanfare";
   private static final String LEVEL_UP = "Level Up";
   private static final String BELL = "Bell";
   private static final String AMETHYST = "Amethyst";
   private static final String SILENT = "Off";
   private static final String CARD_AT = "Top Center";
   private static final String SOUND = "Level Up";
   private static final int PER_HOUR = 3;
   private static final String NAME = "Albız Bets";
   private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(Redirect.NEVER).build();
   private final Gson gson = new Gson();
   private volatile Thread worker;
   private volatile long nextPoll;
   private String etag;
   private final Deque<DonutBetsFeed.Event> queue = new ArrayDeque<>();
   private final Set<String> seen = new LinkedHashSet<>();
   private final Deque<Long> shownAt = new ArrayDeque<>();
   private int ticksSinceHurt = 200;
   private long lastSoundAt;
   private static final DonutBetsFeed INSTANCE = new DonutBetsFeed();

   private DonutBetsFeed() {
   }

   public static void start() {
      INSTANCE.begin();
   }

   static DonutBetsFeed get() {
      return INSTANCE;
   }

   private synchronized void begin() {
      if (this.worker == null) {
         this.loadSeen();
         this.nextPoll = System.currentTimeMillis() + 15000L;
         LOGGER.info(
            "[DonutBets] Reading DonutBets event texts from {} about once a minute. Text only: nothing about the player is sent, nothing is downloaded or run. Part of the client, always on.",
            FEED
         );
         Thread thread = new Thread(this::run, "Albiz DonutBets event feed (text only)");
         thread.setDaemon(true);
         this.worker = thread;
         thread.start();
         TickEvent.TYPE.listen(this, Stage.POST, EventPriority.NORMAL, event -> this.tick());
      }
   }

   private void run() {
      while (this.worker == Thread.currentThread()) {
         if (System.currentTimeMillis() >= this.nextPoll) {
            this.poll();
            this.nextPoll = System.currentTimeMillis() + 55000L + ThreadLocalRandom.current().nextLong(10000L);
         }

         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var2) {
            return;
         }
      }
   }

   private void poll() {
      try {
         Builder request = HttpRequest.newBuilder(FEED)
            .timeout(Duration.ofSeconds(8L))
            .header("Accept", "application/json")
            .header("User-Agent", "Albiz-Client/" + version() + " (DonutBets event feed, text only)")
            .GET();
         if (this.etag != null) {
            request.header("If-None-Match", this.etag);
         }

         HttpResponse<InputStream> response = this.http.send(request.build(), BodyHandlers.ofInputStream());

         try (InputStream body = response.body()) {
            CompletableFuture.runAsync(() -> closeQuietly(body), CompletableFuture.delayedExecutor(12L, TimeUnit.SECONDS));
            if (response.statusCode() == 304) {
               return;
            }

            String type = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
            if (response.statusCode() == 200 && type.contains("json")) {
               byte[] bytes = body.readNBytes(65537);
               if (bytes.length > 65536) {
                  LOGGER.debug("[DonutBets] feed is bigger than {} bytes, not read", 65536);
                  return;
               }

               this.etag = response.headers().firstValue("ETag").filter(DonutBetsFeed::plainHeader).orElse(null);
               this.accept(new String(bytes, StandardCharsets.UTF_8));
               return;
            }

            LOGGER.debug("[DonutBets] feed answered {} {}", response.statusCode(), type);
         }

         return;
      } catch (InterruptedException var8) {
         Thread.currentThread().interrupt();
      } catch (Exception var9) {
         LOGGER.debug("[DonutBets] feed did not answer: {}", var9.toString());
      }
   }

   void accept(String body) {
      List<DonutBetsFeed.Event> fresh;
      try {
         fresh = parse(body, Instant.now());
      } catch (StackOverflowError | RuntimeException var7) {
         LOGGER.debug("[DonutBets] feed is not valid JSON: {}", var7.toString());
         return;
      }

      synchronized (this.queue) {
         for (DonutBetsFeed.Event event : fresh) {
            if (this.queue.size() >= 20) {
               break;
            }

            if (!this.seen.contains(event.id()) && !this.queue.stream().anyMatch(waiting -> waiting.id().equals(event.id()))) {
               this.queue.add(event);
            }
         }
      }
   }

   static List<DonutBetsFeed.Event> parse(String body, Instant now) {
      List<DonutBetsFeed.Event> out = new ArrayList<>();
      JsonElement root = JsonParser.parseString(body);
      if (root.isJsonObject() && root.getAsJsonObject().has("events") && root.getAsJsonObject().get("events").isJsonArray()) {
         for (JsonElement element : root.getAsJsonObject().getAsJsonArray("events")) {
            if (out.size() >= 20) {
               break;
            }

            if (element.isJsonObject()) {
               JsonObject event = element.getAsJsonObject();
               String id = clean(text(event, "id"), 80);
               String title = clean(text(event, "title"), 100);
               String text = clean(text(event, "text"), 300);
               if (!id.isEmpty() && (!title.isEmpty() || !text.isEmpty())) {
                  Instant starts = time(event, "startsAt");
                  Instant ends = time(event, "endsAt");
                  if ((starts == null || !now.isBefore(starts)) && (ends == null || now.isBefore(ends))) {
                     out.add(new DonutBetsFeed.Event(id, title, text, safeLink(text(event, "url"))));
                  }
               }
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private void tick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         this.ticksSinceHurt = mc.field_1724.field_6235 > 0 ? 0 : this.ticksSinceHurt + 1;
         if (mc.field_1755 == null && this.ticksSinceHurt >= 200) {
            long now = System.currentTimeMillis();

            while (!this.shownAt.isEmpty() && now - this.shownAt.peekFirst() > 3600000L) {
               this.shownAt.pollFirst();
            }

            if (this.shownAt.size() < 3) {
               DonutBetsFeed.Event event;
               synchronized (this.queue) {
                  event = this.queue.pollFirst();
               }

               if (event != null) {
                  this.shownAt.addLast(now);
                  this.seen.add(event.id());

                  while (this.seen.size() > 300) {
                     this.seen.remove(this.seen.iterator().next());
                  }

                  this.saveSeen();
                  this.show(mc, event, now);
               }
            }
         }
      }
   }

   private void show(class_310 mc, DonutBetsFeed.Event event, long now) {
      ModuleChat.Body body = ModuleChat.body();
      if (!event.title().isEmpty()) {
         body.append(class_2561.method_43473().method_54663(16777215).method_10852(styled(event.title(), false)));
      }

      if (!event.text().isEmpty()) {
         if (!event.title().isEmpty()) {
            body.text(": ");
         }

         body.append(styled(event.text(), false));
      }

      String link = safeLink(event.url());
      if (link != null) {
         body.text(" ")
            .append(
               class_2561.method_43470("[open]")
                  .method_27694(
                     style -> style.method_10977(class_124.field_1065)
                        .method_30938(true)
                        .method_10958(new class_10608(URI.create(link)))
                        .method_10949(new class_10613(class_2561.method_43470(link)))
                  )
            );
      }

      ModuleChat.sendAs("Albız Bets", body);
      class_2561 title = event.title().isEmpty() ? class_2561.method_43470("Albız Bets") : styled(event.title(), true);
      class_2561 message = event.text().isEmpty() ? class_2561.method_43470("donutbets.net") : styled(event.text(), true);
      boolean quiet = now - this.lastSoundAt < 10000L;
      if (!quiet) {
         this.lastSoundAt = now;
      }

      mc.method_1566().method_1999(new DonutBetsFeed.BetsToast(title, message, quiet ? "Off" : "Level Up", "Top Center"));
      LOGGER.info("[DonutBets] showed event {}", event.id());
   }

   static String safeLink(String url) {
      if (url != null && !url.isBlank() && url.length() <= 300) {
         try {
            URI uri = new URI(url.trim());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getRawUserInfo() != null || uri.getPort() != -1) {
               return null;
            } else {
               return !host.equals("donutbets.net") && !host.endsWith(".donutbets.net") ? null : uri.toASCIIString();
            }
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   static class_5250 styled(String text, boolean liveRainbow) {
      class_5250 out = class_2561.method_43473();
      StringBuilder run = new StringBuilder();
      class_2583 style = class_2583.field_24360;
      boolean rainbow = false;
      int i = 0;

      while (i < text.length()) {
         char c = text.charAt(i);
         if ((c == '&' || c == 167) && i + 1 < text.length()) {
            char next = Character.toLowerCase(text.charAt(i + 1));
            if (next == '&') {
               run.append('&');
               i += 2;
               continue;
            }

            if (text.regionMatches(true, i + 1, "rainbow", 0, 7)) {
               flush(out, run, style, rainbow, liveRainbow);
               style = class_2583.field_24360;
               rainbow = true;
               i += 8;
               continue;
            }

            if (next == '#' && i + 8 <= text.length() && text.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}")) {
               flush(out, run, style, rainbow, liveRainbow);
               style = class_2583.field_24360.method_27703(class_5251.method_27717(Integer.parseInt(text.substring(i + 2, i + 8), 16)));
               rainbow = false;
               i += 8;
               continue;
            }

            class_124 format = class_124.method_544(next);
            if (format != null) {
               flush(out, run, style, rainbow, liveRainbow);
               if (format == class_124.field_1070) {
                  style = class_2583.field_24360;
                  rainbow = false;
               } else if (format.method_543()) {
                  style = class_2583.field_24360.method_10977(format);
                  rainbow = false;
               } else {
                  style = style.method_27706(format);
               }

               i += 2;
               continue;
            }
         }

         run.append(c);
         i++;
      }

      flush(out, run, style, rainbow, liveRainbow);
      return out;
   }

   private static void flush(class_5250 out, StringBuilder run, class_2583 style, boolean rainbow, boolean liveRainbow) {
      if (!run.isEmpty()) {
         String text = run.toString();
         run.setLength(0);
         if (!rainbow) {
            out.method_10852(class_2561.method_43470(text).method_10862(style));
         } else if (liveRainbow) {
            out.method_10852(class_2561.method_43470(text).method_10862(style.method_10975("albiz:rainbow")));
         } else {
            int count = text.codePointCount(0, text.length());
            int at = 0;
            int i = 0;

            while (i < text.length()) {
               int c = text.codePointAt(i);
               i += Character.charCount(c);
               float hue = (float)(at++) / Math.max(count, 7) * 0.85F;
               out.method_10852(
                  class_2561.method_43470(Character.toString(c))
                     .method_10862(style.method_27703(class_5251.method_27717(class_3532.method_15369(hue, 0.75F, 1.0F))))
               );
            }
         }
      }
   }

   static String clean(String text, int max) {
      if (text == null) {
         return "";
      } else {
         StringBuilder out = new StringBuilder();
         int i = 0;

         while (i < text.length()) {
            int c = text.codePointAt(i);
            i += Character.charCount(c);
            if (c == 167) {
               c = 38;
            }

            int type = Character.getType(c);
            if (!Character.isISOControl(c) && type != 16 && type != 13 && type != 14 && type != 18 && type != 19 && type != 0) {
               if (out.length() + Character.charCount(c) > max) {
                  break;
               }

               out.appendCodePoint(c);
            }
         }

         return out.toString().trim();
      }
   }

   private static boolean plainHeader(String value) {
      if (!value.isEmpty() && value.length() <= 200) {
         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < ' ' || c > '~') {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static void closeQuietly(InputStream stream) {
      try {
         stream.close();
      } catch (IOException var2) {
      }
   }

   private static String text(JsonObject object, String key) {
      try {
         return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
      } catch (RuntimeException var3) {
         return null;
      }
   }

   private static Instant time(JsonObject object, String key) {
      String value = text(object, key);
      if (value != null && !value.isBlank()) {
         try {
            return Instant.parse(value.trim());
         } catch (DateTimeParseException var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String version() {
      return FabricLoader.getInstance().getModContainer("albiz").map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("dev");
   }

   private static Path seenFile() {
      return net.albiz.client.config.ConfigMigration.root(FabricLoader.getInstance().getConfigDir()).resolve("donutbets_seen.json");
   }

   private void loadSeen() {
      this.seen.clear();

      try {
         Path file = seenFile();
         if (!Files.exists(file) || Files.size(file) > 65536L) {
            return;
         }

         String[] ids = (String[])this.gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), String[].class);
         if (ids == null) {
            return;
         }

         for (String id : ids) {
            String clean = clean(id, 80);
            if (!clean.isEmpty() && this.seen.size() < 300) {
               this.seen.add(clean);
            }
         }
      } catch (RuntimeException | IOException var8) {
         LOGGER.debug("[DonutBets] could not read the shown events: {}", var8.toString());
      }
   }

   private void saveSeen() {
      try {
         Path file = seenFile();
         Files.createDirectories(file.getParent());
         Files.writeString(file, this.gson.toJson(this.seen.toArray(new String[0])), StandardCharsets.UTF_8);
      } catch (IOException var2) {
         LOGGER.debug("[DonutBets] could not save the shown events: {}", var2.toString());
      }
   }

   @Environment(EnvType.CLIENT)
   static final class BetsToast extends ChallengeToast {
      private static final long SHOWN_MILLIS = 30000L;
      private static final float RAINBOW_MILLIS = 3000.0F;
      private static final int BACK_SHIFT = 6;
      private static final float BACK_LEAN = (float)Math.toRadians(22.0);
      private static final float BACK_SCALE = 0.85F;
      private static final class_1799 BACK = new class_1799(class_1802.field_8477);
      private final String position;
      private final String sound;

      BetsToast(class_2561 title, class_2561 message, String sound, String position) {
         super(glinting(new class_1799(class_1802.field_8477)), title, message, !"Off".equals(sound));
         this.sound = sound;
         this.position = position;
      }

      @Override
      public class_3414 method_66530() {
         String var1 = this.sound;

         return switch (var1) {
            case "Fanfare" -> class_3417.field_15195;
            case "Bell" -> (class_3414)class_3417.field_14793.comp_349();
            case "Amethyst" -> class_3417.field_26980;
            case "Off" -> null;
            default -> class_3417.field_14709;
         };
      }

      private static class_1799 glinting(class_1799 stack) {
         stack.method_57379(class_9334.field_49641, true);
         return stack;
      }

      public float method_71809(int screenWidth, float visiblePortion) {
         if ("Top Left".equals(this.position)) {
            return -this.method_29049() * (1.0F - visiblePortion);
         } else {
            return "Top Right".equals(this.position) ? super.method_71809(screenWidth, visiblePortion) : (screenWidth - this.method_29049()) / 2.0F;
         }
      }

      @Override
      protected void renderIcon(class_332 graphics, class_1799 icon) {
         leaning(graphics, 2, -BACK_LEAN);
         leaning(graphics, 14, BACK_LEAN);
         graphics.method_51445(icon, 8, 8);
      }

      private static void leaning(class_332 graphics, int x, float angle) {
         graphics.method_51448().pushMatrix();
         graphics.method_51448().translate(x + 8, 16.0F);
         graphics.method_51448().rotate(angle);
         graphics.method_51448().scale(0.85F, 0.85F);
         graphics.method_51448().translate(-8.0F, -8.0F);
         graphics.method_51445(BACK, 0, 0);
         graphics.method_51448().popMatrix();
      }

      @Override
      protected long displayMillis() {
         return 30000L;
      }

      @Override
      public void method_1986(class_332 graphics, class_327 font, long visibleFor) {
         super.method_1986(graphics, font, visibleFor);
         frame(graphics, this.cardRight(), this.cardBottom(), (float)(class_156.method_658() % 3000L) / 3000.0F);
      }

      private static void frame(class_332 graphics, int right, int bottom, float turn) {
         int top = 1;
         int across = right - 4;
         int down = bottom - top - 4;
         float perimeter = 2.0F * (across + down) + 8.0F;
         float at = 0.0F;

         for (int x = 2; x < right - 2; x++) {
            dot(graphics, x, top, at++ / perimeter + turn);
         }

         dot(graphics, right - 2, top + 1, at++ / perimeter + turn);

         for (int y = top + 2; y < bottom - 2; y++) {
            dot(graphics, right - 1, y, at++ / perimeter + turn);
         }

         dot(graphics, right - 2, bottom - 2, at++ / perimeter + turn);

         for (int x = right - 3; x >= 2; x--) {
            dot(graphics, x, bottom - 1, at++ / perimeter + turn);
         }

         dot(graphics, 1, bottom - 2, at++ / perimeter + turn);

         for (int y = bottom - 3; y >= top + 2; y--) {
            dot(graphics, 0, y, at++ / perimeter + turn);
         }

         dot(graphics, 1, top + 1, at++ / perimeter + turn);
      }

      private static void dot(class_332 graphics, int x, int y, float hue) {
         graphics.method_25294(x, y, x + 1, y + 1, 0xFF000000 | class_3532.method_15369(hue - (float)Math.floor(hue), 0.8F, 1.0F));
      }
   }

   @Environment(EnvType.CLIENT)
   record Event(String id, String title, String text, String url) {
   }
}
