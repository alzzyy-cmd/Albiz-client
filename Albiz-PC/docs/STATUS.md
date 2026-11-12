# Albız 1.0 — İlk Anubis Panel GUI

**Minecraft 1.21.11 · Fabric · Java 21**

## Bu sürümdeki düzeltme
Kullanıcının verdiği Anubis deposundaki `anubis.zip.001/.002` içinden iki orijinal
JAR’ın seçili GUI sınıfları statik olarak karşılaştırıldı. İki JAR’ın GUI kodu aynı:
ana modül sayfası `renderModulePanels` kullanıyor. Önceki teslimde yanlışlıkla
sonradan eklenen `renderLibrary` tek pencere/sol sütun arayüzü etkin kalmıştı.

Bu sürümde **orijinal Anubis panel çizim yolu etkinleştirildi**, kütüphane görünümü
ve ona ait alanlar/kod kaldırıldı. Yeni bir yaklaşık GUI tasarlanmadı.

- Bağımsız, sürüklenebilir kategori panelleri.
- Katlanabilir kategori başlıkları ve ayrı arama paneli.
- Modüle sağ tıklayınca ayar kartı.
- Altta **Modüller / HUD / Arkadaşlar / Tema / Config** sekmeleri.
- Kayıtlı panel konumları ve mevcut favoriler desteği korunur.

## Korunanlar
Hotfix 2’ye göre üretim Java kaynaklarında değişen tek dosya
`gui/ClickGuiRenderer.java`dır. Diğer Java kaynakları ve bütün kaynak varlıkları
byte-byte karşılaştırıldı: değişmedi. Mevcut native HUD, ana menü, multiplayer,
Dusk, Türkçe metinler, açılış ekranı seçimi, küçük resimli totem/eşya seçicileri,
Watery kaynak yolu ve sprite kontur düzeltmeleri korunur. Korunan 11 dosyanın
SHA256 değerleri de aynı.

HUD sekmesi yine mevcut native HUD’ın ayarlarını kontrol eder; eski HUD çizim
motoruna dönülmedi. Tema → Genel → Ana Menü bölümündeki açılış ekranı seçimi kalır.

## Kurulum
1. Config klasörünü yedekle.
2. Önceki Albız JAR’ını `mods` klasöründen çıkar.
3. **`albiz-1.0-anubis-panels.jar`** dosyasını koy; aynı anda iki Albız sürümü bırakma.
4. Java 21, uygun Fabric Loader ve Fabric API kullan. Varsayılan GUI tuşu sağ Shift.

Config silmek gerekmez. Dağıtımın kaynak ZIP’i eksiksiz ve cache bağlantılarından
bağımsızdır. Java 21 ile `bash gradlew test build` çalıştırılabilir.

## Doğrulama
**140 Java testi ve 28 Python testi geçti.** Yeni statik regresyonlar ana modül
sayfasının gerçekten panel renderer’ını çağırdığını, kütüphane kodunun kaldırıldığını,
bağımsız kategori/arama/katlama/konum kaydı ve alt sekmelerin mevcut olduğunu denetler.
`docs/ANUBIS_PANEL_RESTORE.json` kaynak karşılaştırması; `docs/GUI_ITEMS_CHECKS.json`,
BUILD.log, PYTHON_TESTS.log ve TEST_RESULTS.xml güncel otomatik kanıtlardır.

**Minecraft/OpenGL/Android, oyun içi sürükleme ve ekran görüntüsü eşitliği testi
 yapılmadı.** Kaynak üzerinden orijinal panel yoluna dönülmesi, oyun içi piksel
 eşitliği veya sıfır bug/FPS maliyeti garantisi değildir. Önce ayrı profilde dene.
Önceki multiplayer çökmesinin tam logu hâlâ yok; bu sürüm onu yeniden üretip
çözdüğünü iddia etmez. Tekrar çökerse latest.log ve crash-report birlikte gerekir.

LICENSE, NOTICE.md ve THIRD_PARTY_* bildirimleri korunur. İç paket/JSON/enum
kimlikleri yeniden adlandırılmadı. `docs/history/HOTFIX2_STATUS.md` önceki
revizyonun tarihsel raporudur; güncel GUI düzeni için bu README esas alınır.
