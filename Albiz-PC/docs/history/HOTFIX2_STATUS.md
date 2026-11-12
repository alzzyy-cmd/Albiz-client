# Hotfix 2 — bildirilen hatalar ve menü ayarları

Bu paket `albiz-1.0-classic-gui-items-hotfix2.jar` dosyasıdır. Önceki Albız JAR’ını
mods klasöründen çıkar; ikisini birlikte kullanma. Config yedeği al; config silmek gerekmez.

## Somut düzeltmeler
- Türkçe sistem dilinde `PICKAXE → pıckaxe` ve `GRID → grıd` dönüşümleri artık kaynak
  kimliklerine uygulanmaz. Watery, menü ikonları ve eşya kimlikleri Locale.ROOT kullanır.
- Sprite kontur bağlama kodundaki `IllegalStateException: Decompilation failed` kalıntısı
  gerçek kontur algoritmasıyla değiştirildi. Üretimde kullanılan kontur/buffer kodu CPU
  testlerine ayrıldı; Fortnite Pack’in items.txt dosyasındaki **24 PNG’nin tamamı** kontrol edildi.
- Multiplayer’ın özel arayüz kurulumuna hata denetimi, çizimine her çıkışta matrix/scissor
  temizliği eklendi. Hata durumunda yarım kalmış kontrollere klavye/fare erişimi engellenir;
  Escape geri döner. Hata mesajı Türkçedir, stack trace latest.log içine yazılır.
  **Kullanıcının multiplayer çökmesinin tam yığın izi yok: bu çökme yeniden üretilmedi ve
  kesin kök nedeninin giderildiği iddia edilmiyor.**
- Eski GUI’de **Tema → Genel → Ana Menü → Açılış Ekranı** seçimi vardır:
  Albız, Vanilla+, Ev, Ada, Klasik. Seçim ana menüye sonraki dönüşte uygulanır ve saklanır.
  Özel menü anahtarı native menü etkinliğiyle eşleştirilir. Klasik menünün açıkça vanilla’ya
  dönüş isteği yeniden özel menüye yönlendirilmez; stil seçmek özel menüye dönüşü etkinleştirir.
- Ev/Vanilla+ menülerindeki görünür SoberClient/SOBERCLIENT/Discord reklam metinleri ve
  eski mod sürüm sorgusu Albız’a uyarlandı. İç paket/sınıf/JSON kimlikleri ve yasal Sober
  atıfları değiştirilmedi; mevcut stil kayıtları (`Sober` enum adı dahil) uyumludur.
- Alternatif menüler, arama kutuları, kart/liste eylemleri, menü tercihleri, HUD ve eşya
  ayarları için Türkçe sunum genişletildi. Teknik kimlikler ve kullanıcı girdileri korunur;
  tüm dinamik/üçüncü taraf metinlerin çevrildiği iddia edilmez.
- PNG başlığı/boyutu native decoder’dan önce doğrulanır; PNG dışı, kesilmiş ve aşırı büyük
  görüntüler reddedilir.

## Kontroller
**140 Java testi, 25 Python testi geçti.** 11 korunan kaynak SHA256 değeri değişmedi.
Yeni regresyonlar: Türkçe locale kaynak adları, kapalı/ayrık/dallanan/uzun konturlar,
24 paket görselinin sonlu geometri üretimi, PNG başlık sınırları, görünür menü markası,
ayar erişimi ve multiplayer hata durumundaki durum temizliği.
İlk test çalıştırmasında UTF-8 olmayan ortamdan kaynaklanan 4 dosya yolu testi başarısız
oldu; UTF-8 ortamıyla tekrar çalıştırılan **son testlerin hepsi geçti**. Korunan config
kodunu bu testleri geçirtmek için değiştirmedik.

**Minecraft/OpenGL/Android/FPS ve canlı sunucu testi yapılmadı.** Oyun içindeki bütün
bugların giderildiği garanti edilmez. Multiplayer yeniden çökerse aynı çalıştırmanın
`logs/latest.log` dosyasını ve `crash-reports` içindeki son raporu birlikte paylaş.
Kaynak ZIP eksiksizdir; `docs/GUI_ITEMS_CHECKS.json` güncel otomatik kontrol raporudur.

---

# Albız 1.0 — Klasik GUI + Eşya Görünümleri

Minecraft **1.21.11 · Fabric · Java 21**. Bu sürüm klasik Albız kategori/kart
arayüzüne döner; mevcut native ana menü, multiplayer ve HUD tasarımlarını korur.

## Kurulum
1. Config klasörünü yedekle; önceki Albız JAR’ını `mods` klasöründen çıkar.
2. `albiz-1.0-classic-gui-items-hotfix2.jar` dosyasını yerleştir. Aynı anda iki Albız sürümü bırakma.
3. Uygun Fabric Loader/Fabric API ile Java 21 kullan. GUI varsayılan olarak sağ Shift ile açılır.
4. Bu deneysel yapıyı önce ayrı bir oyun profilinde dene.

## Neler değişti?
- **Eski kategori/kart GUI geri geldi.** Sober Panel/Dropdown artık ana ayar ekranı değil.
- Native ana menü/multiplayer tasarımı ve mevcut HUD çizimi korundu. Eski GUI’nin HUD
  sekmesi artık eski yerleşim motorunu değil, kullanılan native HUD ayarlarını kontrol eder.
  HUD’ı taşımak/boyutlandırmak için sohbeti aç.
- Tek oyunculu/çok oyunculu arka planında projenin mevcut **Albız Dusk** resmi kullanılır.
  Açılış logosu ve ana menü logosu büyütüldü.
- Ana menü/multiplayer metinleri, dünya seçimi başlığı ve yeni eşya ayarlarına Türkçe
  sunum eklendi. Teknik adlar, kullanıcı/sunucu adları ve bazı yardım metinleri İngilizce
  kalabilir. Dil değiştirme kayıt kimliklerini ve komutları değiştirmez.
- **Custom Totems:** mevcut 97 görünüm + varsayılan; küçük resimli, aranabilir seçim ekranı.
- **CS Pickaxe:** Butterfly, Karambit, Katana; gerçek bıçak geometrisinden küçük resimler,
  malzeme/boyut, inceleme (varsayılan F), elde açılış ve savurma animasyonları.
  Bunlar yalnız görseldir; vuruş menzili, hasar veya paket davranışı eklemez.
- **Custom Item Models:** referansın yerleşik 3B modelleri, **Fortnite Pack**, **Watery**;
  STL/OBJ/PNG ve yerel PNG paketleri. Görsel Seçim düğmesi gerçek küçük resimleri açar.
  Yerel klasöre erişmek için Klasörü Aç, yeniden taramak için Modelleri Yenile kullan.
  Birinci/üçüncü şahıs ve envanter görünümü ayarları bulunur.
- Yeni eşya modülleri varsayılan kapalıdır. Aynı eşya için iki modül etkinse Custom Item
  Models önceliklidir; ardından CS Pickaxe, ardından mevcut Albız görünümü gelir.
- Onaylı Guts Sword/karakterler, SpawnerBeam ve önceki startup ses güvenliği korundu.
  Bu revizyon yeni combat/movement modülü eklemez. Native gelişmiş glint, skin veya
  aksesuar sistemlerinin tamamının aktarıldığı iddia edilmez; mevcut glint korunur.

## Performans ve güvenlik sınırları
- Seçicide yalnız görünür satırlar çizilir; arama/kaydırma vardır.
- Model/sprite önbellekleri 64 giriş, raster önizleme önbelleği 64 doku ile sınırlıdır;
  geometri küçük resimleri yaklaşık kare başına en fazla bir kez oluşturulur.
- Sprite işçisi tek thread, bekleyen harita/kuyruk 32 girişle sınırlıdır; yeniden yükleme
  eski işleri iptal eder. Yoğunlukta ana/render thread üzerinde resim çözülmez.
- Yerel tarama 128 yol, paket başına 512 PNG, dosya başına 16 MiB, model başına
  60.000 üçgen sınırındadır. PNG başlığında boyut sınırı 2048×2048’dir; sprite dokuları
  256 piksel uzun kenara hazırlanır. Referansın 128×128 paket görsellerini 512’ye
  gereksiz büyütmekten kaçınılır.
- İçe aktarılan model yolu klasör dışına veya klasör dışına işaret eden symlink’e
  çıkamaz; geçersiz modeller vanilla görünümüne düşer. Sonradan dosya/symlink değiştiren
  yerel saldırılara karşı bir dosya sistemi sandbox’ı olduğu iddia edilmez.
- İlk kullanımda önizlemeler sırayla oluşabilir. Etkin animasyonlu/karmaşık modellerin
  maliyeti vardır: **sıfır FPS etkisi garantisi yoktur.**

## Doğrulama ve sınırlar
`docs/GUI_ITEMS_CHECKS.json`, `docs/BUILD.log`, `docs/PYTHON_TESTS.log` ve
`docs/TEST_RESULTS.xml` bu revizyonun otomatik kontrol sonuçlarıdır. Önceki revizyon
raporları tarihsel bilgi olarak kalır; mevcut davranış için bu README esas alınır.
11 korunan kaynak dosyasının SHA256 kontrolü yapılır. CPU testleri yerleşik model
geometrisi, OBJ/STL ayrıştırma, geçersiz geometri ve klasör dışı yol reddini kapsar.

**Minecraft/OpenGL, Android/Zalith, canlı sunucu, ekran görüntüsü karşılaştırması ve
FPS ölçümü yapılmadı.** Derleme başarısı oyun içinde hatasız çalışma kanıtı değildir.
Ayrı profilde açılış, GUI/HUD, tek/çok oyunculu geri dönüş, görsel seçim/yeniden başlatma,
F inceleme, perspektif/eller ve kaynak paketi yenilemesi kontrol edilmelidir.

## Kaynaktan derleme
Kaynak ZIP’ini çıkar; kök dizinde Java 21 ile `bash gradlew test build` çalıştır.
Dağıtım kaynak ZIP’i bağımsızdır, cache symlink’i gerektirmez. Agent çalışma alanındaki
cache bağlantıları kaybolmuşsa `python restore_workspace.py` ile dağıtım ZIP’inden geri al.
`THIRD_PARTY_PORTS.md`, `THIRD_PARTY_CHARACTERS.md`, LICENSE ve NOTICE korunmalıdır.
Sober GPL beyanı bütün üçüncü taraf varlık haklarının bağımsız teyidi değildir;
mevcut karakter/CC-BY-NC-SA ve diğer bildirimler ayrıca geçerlidir. CodeX ARR kopyalanmadı.
