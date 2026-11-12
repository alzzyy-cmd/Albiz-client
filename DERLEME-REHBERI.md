# Albiz — Derleme Rehberi (bir yapay zekâ ya da geliştirici için)

Bu rehber, bu ZIP'teki kaynaktan PC ve Mobil JAR'larının nasıl derleneceğini anlatır.
Proje **Maven değil, Gradle** kullanır (Fabric Loom). "Maven" yalnızca bağımlılıkların
indirildiği depoların adıdır.

**Durum (2026-11-12):** iki proje de GitHub Actions üzerinde derleniyor (`build exit=0`).
`.github/workflows/build.yml` her push'ta: `restore_assets.py` ile eksik resim/font/ses/model
dosyalarını geri yükler, iki projeyi derler, JAR'ları `dist/` klasörüne ve özet günlükleri
`ci-logs/` klasörüne yazar.

**En kısa yol (eski Albiz JAR'ı olmadan):**

```bash
python3 restore_assets.py          # Sober JAR'ı ve eski Albiz JAR'ından dosyaları geri yükler
cd Albiz-PC && bash gradlew build -x test --max-workers=2
```

`restore_assets.py` dosyaları `github.com/alzzyy-cmd/Anubis` deposundan alır ve SHA-256 ile
doğrular. `guts`/`subaru` modelleri, bayrak resimleri ve bazı Albiz sesleri bu kaynaklarda
yoktur; onlar için 3. bölümdeki `restore_resources.py` + güncel Albiz JAR'ı gerekir.

## 1. ZIP'in içeriği

| Yol | Ne |
|---|---|
| `Albiz-PC/` | PC projesi (kendi başına bir Gradle projesi) |
| `Albiz-Mobil/` | Mobil projesi (kendi başına bir Gradle projesi) |
| `verification/` | Minecraft olmadan çalışan birim testleri ve isim denetimi araçları |
| `*.md` | Analiz, özellik tablosu, değişiklik günlüğü, test raporu |

## 2. Gerekenler

- **JDK 21** (`java -version` 21 göstermeli).
- **İnternet**: şu adreslere erişim şart — `services.gradle.org`, `plugins.gradle.org`,
  `maven.fabricmc.net`, `repo.maven.apache.org`, `piston-meta.mojang.com`,
  `libraries.minecraft.net`, `resources.download.minecraft.net`.
- Gradle'ı ayrıca kurma; `gradlew` doğru sürümü (9.4.0) kendisi indirir.
- En az ~3 GB boş RAM. `gradle.properties` Gradle'a 768 MB verir; "OutOfMemory" olursa
  `org.gradle.jvmargs=-Xmx2g` yap.
- **Önceki Albiz JAR'ı** (`Albiz-1.0-Friends/PC/Albiz-Client.jar` ve
  `Albiz-1.0-Friends/Mobile/Albiz-Client.jar`). Kaynakta büyük dosyalar (PNG, font, OGG,
  model, yerel kütüphane) **yoktur**; bunlar eski JAR'dan geri yüklenir. Bu JAR olmadan kod
  derlenir ama oyun içinde resimler ve sesler eksik olur.

## 3. Derleme adımları (PC)

```bash
cd Albiz-PC

# 3.1 Yeni metin kaynaklarını yedekle. restore_resources.py eski JAR'daki assets/ klasörünü
#     üzerine yazar; yedeklemezsen yeni çeviriler ve yeni modül kayıtları kaybolur.
cp -r src/main/resources /tmp/albiz-yeni-kaynaklar

# 3.2 Büyük dosyaları eski JAR'dan geri yükle.
python restore_resources.py /tam/yol/Albiz-1.0-Friends/PC/Albiz-Client.jar

# 3.3 Yeni metin kaynaklarını geri koy (eski sürümlerin üzerine yazar, büyük dosyalara dokunmaz).
cp -r /tmp/albiz-yeni-kaynaklar/. src/main/resources/

# 3.4 Test et ve derle.
bash gradlew test build addonClasspath --max-workers=1
```

Çıktı: `build/libs/albiz-1.0.jar` (oyuna konacak JAR) ve `build/libs/albiz-1.0-sources.jar`.

Windows'ta `bash gradlew` yerine `gradlew.bat`, `cp -r` yerine klasörü elle kopyala.

## 4. Derleme adımları (Mobil)

Aynı dört adım, `Albiz-Mobil` klasöründe ve **Mobile** JAR'ı ile:

```bash
cd Albiz-Mobil
cp -r src/main/resources /tmp/albiz-mobil-yeni-kaynaklar
python restore_resources.py /tam/yol/Albiz-1.0-Friends/Mobile/Albiz-Client.jar
cp -r /tmp/albiz-mobil-yeni-kaynaklar/. src/main/resources/
bash gradlew test build addonClasspath --max-workers=1
```

3.3 / üçüncü satır Mobil'de özellikle önemlidir: `restore_resources.py` `fabric.mod.json`
içindeki adı "Albiz Client" yapar; geri kopyalama "Albiz Client Mobile" adını geri getirir.

## 5. PC ile Mobil arasındaki fark

**Bağımlılık (Maven) farkı yoktur.** İki projenin `build.gradle`, `settings.gradle`,
`gradle.properties` dosyaları ve tüm Java kodu bayt bayt aynıdır. Aynı depolar, aynı sürümler:

| Bağımlılık | Sürüm |
|---|---|
| Minecraft | 1.21.11 |
| Eşlemeler | `net.fabricmc:intermediary:1.21.11:v2` |
| Fabric Loader | 0.18.4 |
| Fabric API | 0.140.0+1.21.11 |
| Fabric Loom (eklenti) | 1.16.3 |
| imgui-java (binding, lwjgl3, natives-linux/windows/macos) | 1.90.0 |
| JUnit | 5.11.4 |
| Litematica / MaLiLib | `libs/` içindeki JAR'lar, yalnızca derleme için |

Depolar: `mavenCentral()` ve `https://maven.fabricmc.net/`.

Fark yalnızca şu üç dosyadadır:

| Dosya | PC | Mobil |
|---|---|---|
| `src/main/resources/albiz-profile.properties` | `profile=pc` | `profile=mobile` |
| `src/main/resources/fabric.mod.json` → `name` | `Albiz Client PC` | `Albiz Client Mobile` |
| `KAYNAK-OKU.md` | PC JAR yolu ve SHA256 | Mobile JAR yolu ve SHA256 |

`profile=mobile` kodun çalışma anındaki davranışını değiştirir (`BuildProfile.mobile()` ve
`PlatformInfo.isMobile()`): daha düşük tarama bütçesi, daha az çizim, daha küçük sınırlar.
Derleme komutu aynıdır.

**Paketleme farkı** (derlemeden sonra, isteğe bağlı): `tools/package_revision.py` tek bir
derlenmiş JAR'dan hem PC hem Mobil sürüm paketini üretir. Mobil paket için:
- `imgui-java-natives-*` iç JAR'larını çıkarır (Android'de masaüstü yerel kütüphaneleri çalışmaz),
- `textures/sky`, `textures/gui`, `textures/menu` altındaki büyük PNG'leri küçültür (768 / 960 piksel),
- `fabric.mod.json` içine `albiz:profile` yazar.

Normal `gradlew build` bu küçültmeleri **yapmaz**. Mobil cihazda (PojavLauncher / Zalith vb.)
düz `gradlew build` çıktısı yerine bu aracın ürettiği Mobile JAR'ı kullanılmalıdır. Araç Pillow
ister (`pip install pillow`) ve Gradle derlemesinden sonra çalıştırılır.

## 6. Hata çıkarsa

Kod şu an hatasız derleniyor. İleride değişiklik yapıp hata alırsan:

1. `bash gradlew compileJava --max-workers=1` çalıştır, hataları oku.
2. Hata büyük olasılıkla şu dosyalardadır (bu teslimde yazılan ya da değiştirilenler):
   - `module/impl/render/`: `SkeletonEspModule`, `SoundDebugModule`, `ChunkDiagnosticsModule`,
     `BedrockVoidEspModule`, `SeedChunkFinderModule`, `SusChunkModule`, `MotionScannerModule`,
     `AbyzDebugModule`
   - `module/impl/misc/AutoRespawnModule`, `module/impl/client/PerformanceModule`
   - `port/sober/modules/SuspiciousESP`, `port/soberui/SoberUi`
   - `gui/ClickGuiRenderer`, `gui/GuiPalette`, `config/ThemeStore`
   - `module/BuiltinModuleProvider`, `easyplace/builder/TunnelMinerModule`
3. Düzeltirken modül kimliklerini (`"skeleton-esp"` gibi) ve ayar kimliklerini değiştirme;
   kayıtlı ayarlar bunlara bağlıdır.
4. Her düzeltmeyi **iki projede de** yap (`Albiz-PC` ve `Albiz-Mobil` Java kodu aynı kalmalı).
5. `ClickGuiRenderer.beginPanel` içindeki pencere bayrağı `123 | 786944` imgui-java 1.90.0'a
   karşı doğrulanmadı; ayar paneli tuhaf davranırsa ilk bakılacak yer burasıdır.
6. `LocalizationTest` 162 açıklama / 153 modül adı bekler; modül ekler ya da çıkarırsan güncelle.

## 7. Minecraft olmadan çalışan denetimler

Gradle çalışmıyorsa bile şunlar çalışır (ayrıntı: `verification/README.md`):

- `verification/mytests.sh <proje>` — saf mantık birim testleri (76 test).
- `python3 tools/audit_localization.py` (proje klasöründe) — çeviri denetimi.
- `python3 tools/test_*.py` — depo kontrolleri. Dört tanesi büyük dosyalar geri yüklenmeden
  başarısız olur; bu beklenen durumdur.
- `verification/verify.py <java dosyaları>` — Minecraft adlarını 1.21.11 eşlemeleriyle
  karşılaştırır (eşleme dosyası `FabricMC/intermediary` deposundan `mappings/1.21.11.tiny`).
