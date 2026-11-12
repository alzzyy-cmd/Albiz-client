# Albız 1.0 — arayüz ve görsel yenileme

**Hedef:** Minecraft 1.21.11 / Fabric / Java 21. Desktop ve Android/Zalith için
kaynakta uyarlamalar var; oyun/Android çalışma doğrulaması yapılmadı.

## Dosyalar ve kurulum
- `albiz-1.0.jar`: tam istemci JAR'ı; eski Albız JAR'ını kaldırıp bunu `mods` içine koy.
  Aynı modun eski ve yeni JAR'ını birlikte bırakma. Minecraft 1.21.11, Fabric Loader
  ve Fabric API gerekir. Fabric API 0.140.0+1.21.11 derleme bağımlılığıdır.
- `albiz-1.0-source.zip`: Gradle wrapper, kaynak, kaynak varlıkları, isteğe bağlı
  entegrasyon derleme bağımlılıkları ve model üretim kaynakları. Özel config ve
  paket kayıtları dağıtılmaz. Derleme: Java 21 ile `bash gradlew build`.
- `SHA256SUMS.txt`: indirdiğin dosyaların doğrulama değerleri.

Mevcut ayarlarını önceden yedekle. Ayar klasörü `albız`, UTF-8 desteklemeyen
ortamlarda `albiz` geri dönüşü korunur. Eski kaldırılmış modül kayıtları, modüllerin
tekrar görünür olmasını sağlamaz. İlk açılışta yeni görsel modüller kendiliğinden
hepsini açmaz: istediğini GUI'den etkinleştir.

## Bu yapıda olanlar
| Alan | Gerçek değişiklik |
|---|---|
| GUI | Tek, sınırlandırılmış kategori/modül tarayıcısı; yuvarlatılmış kartlar, arama, Favoriler `+`, her modülde dokunulabilir `...` ayar düğmesi. Mevcut tema seçenekleri korunur. |
| Mobil | GUI ölçeği hem genişlik hem yükseklikle sınırlı; ayar kartları/inspector konumları sınırlandırıldı, HUD sekme genişliği sınırlandı. Matematik testleri var; gerçek dokunmatik ekran testi yok. |
| Picker | Önceki gerçek item/blok görselleri, alt sayfa/Geri akışı ve korumalı giriş sistemi korunur. |
| Multiplayer | Albız'a ait 1–3 sütunlu sunucu kartları, arama, LAN satırları. Bağlan/ekle/düzenle/sil/doğrudan bağlantı işlevleri vanilla üzerinden; sınırlı arka plan ping kuyruğu. Birebir Sober ekranı değildir. |
| Staff HUD | Eski sunucuya özel Staff List yerine yeni `Staff HUD`: kullanıcı isimleri, rütbe sözcükleri, hariç tutma listesi; sekme verisine dayanır. Kaybolan oyuncuyu otomatik olarak “vanished” ilan etmez. |
| Sky | Sober renderer/gölgelendiricilerinden 19 seçilebilir efekt; kalite, hız, parlaklık, kutup ışığı rengi, sis ve bulut ayarları. Mobilde düşük çözünürlük bütçesi; başarısız shader için normal gökyüzüne dönüş yolu. |
| Creamy Keys | Aula F75 / FB67 / 60HE V2 / Frosted 65; 116 klavye/fare kaydı OGG'ye dönüştürüldü. Normal Minecraft ses motoru; Java masaüstü ses API'si veya yeni native hook yok. |
| Custom Totems | Default + 97 stil; yerel item-model bileşeni üzerinden el/envanter/patlama görünümü. Sunucuya item modeli göndermez. |
| Crosshair | Mevcut özelleştirilebilir sistem genişletildi: Square, Diamond, Chevron, Ring Cross. Sober'ın crosshair sınıfı aynen aktarılmadı. |
| Glint | Renk/hız/ölçek sistemi korunup Vanilla, Stripes, Wave, Stars, Dots desenleri eklendi. Desenler Albız için üretildi; Sober glint renderer'ı aktarılmadı. |
| 3D Character | Önceki Guts/Subaru/CJ/Neighbor ANC dosyaları değişmedi. Sober referansından Banana, Robot, Cat, üç detay seviyesiyle eklendi. Bu üç statik modelin eklem ayrımı yaklaşık; referansın oyuncu-model değiştirme sistemi değildir. |
| Spawner Beam | Gerçek Sober algoritması Albız olay/render/ayar sistemine uyarlandı. Işık sütunu, renk, kutu, bildirim ayarları; ayrı yeni spawner modülü. |
| Block Entity ESP | Eski debug/storage eşdeğerleri yerine referanstan tür/renk/menzil filtreli yeni modül; mobil tarama/işaret sınırları. Beam açıksa ikinci spawner kutusu çizmez. |
| Hive Finder | Referansın “ChunkFinder” algoritması doğru isimle sunulur: kovan/arı yuvası bulur, her üs türünü bulduğu iddia edilmez. Birleştirme, iç/dış halka ve yalnızca dolu kovan ayarları. |
| Suspicious ESP | Referansın varlık/büyüme işaretleri; istemci-thread kuyruğu, yüklü chunk kimliği kontrolü, mobil tarama ve çizim sınırları. İşaretler üs kanıtı değildir. |

## Kaldırılanlar ve korunanlar
- Donut kategorisindeki eski modüller kaldırıldı; **Auto Relog → Misc**. Boş kategori
  görünmez. Yaşayan modül adlarından Donut ifadesi kaldırıldı; teknik sunucu algılama
  dizeleri topluca bozulmadı. Ayrı Macro kategorisi korunur.
- Floating Relics, Totem Spirit, Hand Aura, Color Grading kaldırıldı.
- Eski Block Entity Debug, Storage ESP, eski Suspicious ESP, Better Sus Chunk,
  Chunk Finder ve örtüşen Sus Chunk Finder kayıtları kaldırıldı. Bağımlılık güvenliği
  için bazı eski sınıflar kaynakta/JAR'da bulunabilir; menüye kaydedilmez.
- **3D Hand, Hand Pose, Hit Particles, Guts Sword** korunur. Önceki input/native,
  ses geri bildirimi, slider sesi, temalar, oturum ve yasal dosyalar korunur.
- Packet Capture korunur: varsayılan kapalı, varsayılan süre 5 dakika.
- Sırf referans var diye ilgisiz Combat/Movement modülleri topluca eklenmedi.

## Aktarım sınırı — neyi iddia etmiyoruz?
Bu, Sober'ın bütün GUI/HUD sınıflarının ya da Larp'ın eksiksiz kopyası değildir.
Yeni GUI/multiplayer ve Staff HUD Albız uyarlamalarıdır; mevcut ana HUD editörü ve
yerleşim sistemi korunup dar ekran düzeltmeleri yapıldı. Sober'ın bütün Inventory /
Target HUD alternatifleri veya `CustomModels` item-model paketi aktarılmadı.
Larp V7 etiketli arşivde V5/V6/Finder sınıfları vardı. Larp kodu doğrudan alınmadı;
Hive Finder'daki halka/kovan seçenekleri gözlenen V6 davranışından esinlenir.
Ayrı bir “tam V7”, V5 istihbarat sistemi veya CodeX aktarımı yoktur.

**119 Java + 8 Python testi başarılı.** HSV renk dönüşümü, dar/uzun ekran ölçek
hesabı, glint desenleri, yeni karakter LOD/koordinatları, 97 totem kaynak zinciri,
116 OGG kaydı, shader include zinciri ve modül kaldırma/kayıtları denetlendi.
Statik yerelleştirme denetimi geçti. 11 korumalı kaynak/native/yasal dosya ile eski
4 ANC karakter varlığı önceki teslimle byte-byte aynı.

**Minecraft/OpenGL içinde, Zalith/SM-A155F üzerinde veya canlı sunucuda test yok.**
Derleme/test başarısı GPU shader, ses, dokunmatik kullanım, sunucu ping/bağlantı
veya bütün modüllerin oyun içinde hatasız çalıştığının kanıtı değildir. FPS,
AntiESPBypass veya gizli blok/unsent chunk tespiti garantisi verilmez. Sunucunun
istemciye göndermediği gerçek bloklar bu tarayıcılarla ortaya çıkarılamaz.

Yasal/provenans ayrıntıları kaynak ZIP'i ve JAR'ın `assets/albiz/licenses/` dizininde.
Guts varlığına ait CC-BY-NC-SA-4.0 koşulları ayrıca geçerlidir. GitHub'a push yapılmadı.
