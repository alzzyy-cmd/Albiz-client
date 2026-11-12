# Albiz Client 1.0 — Friends / Evidence PC / Mobile + Larp

## Kurulum
Minecraft **Java 1.21.11**, **Java 21**, Fabric Loader **0.18.4+**, Fabric API **0.140.0+1.21.11** gerekir. Bedrock/MCPE paketi değildir.

1. Mevcut mods ve config dizinini yedekle.
2. PC veya Mobile klasöründeki **Albiz-Client.jar** dosyalarından yalnız birini mods içine koy. İkisini birlikte kurma; mod ID aynıdır.
3. İstersen **Larp addon.jar** dosyasını aynı mods klasörüne ekle. Bu addon her iki Albiz profiline karşı derlenmiştir; Meteor gerektirmez.
4. İlk menü açılışında **solda Türkçe/Türkiye bayrağı**, **sağda English/UK bayrağı** bulunur. Seçim `config/albiz/language-choice.txt` içinde kaydedilir; sonraki açılışta tekrar sorulmaz. Modül adları İngilizce kalır. Minecraft'ın kendi dil ayarı zorla değiştirilmez.

## Bu revizyonda
- **Friend Labels** (Render): `Show Friend Labels` ile aç/kapat. Arkadaş listesine elle eklenen oyuncuların üzerinde kırmızı gerçek Albiz logosu ve dile göre **Albiz Kullanıcısı / Albiz User** gösterir. Modülü ayrıca açmalısın. Varsayılan menzil 64 blok, aynı karede en fazla 16 etiket. Görünen/yüklenmiş oyuncularla sınırlıdır. **Albiz kullandığını doğrulamaz**, yalnız senin arkadaş anotasyonundur. Sohbet mesajı, gizli karakter, ayrı servis veya istemci tespit paketi göndermez. Karşılıklı görmek için iki tarafın da diğerini arkadaş eklemesi ve modülü açması gerekir.
- **Wolf Crest**: Wolf, Altai, Red Moon, Fang Helmet, Night Helmet farklı geometriler; **Wolf Set** kurt maskesi ve omuz/göğüs parçası. Sekiz ayrı yerel pelerin: Albiz, kalp, gülücük, taç, şimşek, ay, pati, kılıç. Bunlar fonta bağlı Unicode emoji değil, geometrik piksel motifleridir. Albiz pelerininde verilen gerçek ikonun konturu kullanılır. Pelerinler katı, düşük-poligon gövde kozmetiğidir; vanilla kumaş fiziği veya gerçek zırh avantajı değildir. Hepsi **yalnız kendi ekranında kendi karakterinde** görünür; arkadaşlara eski On Friends seçeneğiyle kozmetik bindirme kapatıldı. Diğer kullanıcılarla paylaşım yapılmaz.
- **Staff HUD / HUD Staff List**: yalnız elle yazılan tam kullanıcı adları, büyük/küçük harf duyarsız eşleme. Rank/etiket tabanlı otomatik algılama ve isim öğrenme kaldırıldı. Varsayılan sunucu isimleri artık eklenmez. Önceki özel kayıtlarda kalan isimleri elle temizleyebilirsin; yeni isim eklenmez. Bu liste çevrimiçi tab bilgisidir, vanish tespiti değildir.
- **Kırmızı logo**: dünya logo işaretleri ve wolf/logo konturları kırmızıya sabitlendi. Renk modu (logo değil normal kutu) hâlâ seçilen rengi kullanır.
- **Abyz Debug**: alınan spawner/trial spawner ve redstone bileşenleri taraması; redstone durum değişimlerinden geçici etkinlik izi. Bu iki blok türü için Y<0 şartı yoktur; kelp/crafter/ses Y<0 kalır. Doğal spawner olabilir, tek bileşen base kanıtı değildir. Geçici etkinlik izleri zaman aşımına uğrar; dünya değişiminde temizlenir, mobilde 512/PC'de 2048 kayıt sınırı vardır. PacketEvents bir veri kaynağı değildir; sunucunun göndermediği blokları geri getirmez, anti-xray bypass garantisi yoktur.
- **Abyz uluması**: `Discovery Howl` ile aç/kapat. Yeni bulguda Sound Feedback modülü açıkken ulur. `Sound Feedback → Howl Key` PC tuş atamasıdır; `Play Wolf Howl` mobilde de dokunulabilen ayar düğmesidir. Bu düğme uluma çalar; dinleme/etkinlik taramasını açmaz. Manuel ve otomatik sesler aynı 8 saniye beklemeyi paylaşır; Sound Feedback kapalı/ses sıfırken duyulmaz. Finder/Chunk Scanner'ın Chunk Alerts ayarı Abyz ve manuel sesi kapatmaz.
- **Kaldırılanlar**: Media Base Finder iki profilde de kaldırıldı (kaynak/sınıf dahil). Spotify mobilde modül kaydından ve HUD Editor önizlemesinden kaldırıldı; mobilde yoklama/iş parçacığı başlamaz. PC Spotify korunur. Ortak sınıf ABI'sini bozmamak için Spotify yardımcı sınıfları iki JAR'da kalabilir. Diğer sistemleri oyun içinde test etmeden “çalışmıyor” diye tahminen silmedim.

## Eklenen / düzeltilen
- **Abyz Debug** (Base Raid): yukarıdaki yeni alınan spawner/redstone sinyallerine ek olarak Y<0 kelp ve kelp_plant; crafter crafting/triggered durumu ve alınan crafter sesleri; diğer alınan yeraltı konumsal sesleri. Konum/renk, menzil, kendi yakın seslerini dışlama, hafıza, bildirim ve tracer ayarları. Ses bir ipucudur, kesin oyuncu/başarılı üretim kanıtı değildir; sunucunun göndermediği bloklar görülemez.
- **Chunk Scanner / Hive Finder:** kare yerine gerçek yüklenen Albiz wolf/sword ikonundan türetilmiş dönen logo. Logo/renk veya logo aç/kapat; dönme seçimi. Chunk Scanner mevcut Y64 işaretleme düzlemini kullanır; Hive Finder yüzey konumunu kullanır.
- **Block Outlines:** Marker→Color / Albiz Logo; renk ve dönme ayarı. Renk modu önceki blokçizimini korur; logo modu üst ve yan yüzlerde kullanıcı ikonunun kırmızı çizgi konturunu gösterir.
- **Ses:** GitHub'daki `dragon-studio-howling-wolves-515977.mp3` tam kaydı OGG Vorbis'e dönüştürüldü (7,56s,53.472bayt). Finder bulunca çalar; Discovery Sound / Sound ve genel Sound Feedback→Chunk Alerts / Volume ile kontrol edilir. Ortak8s bekleme üst üste çalmayı engeller. Uyarı kapalıyken rastgele vanilla sesine düşmez. Duyarak veya oyun içinde test edilmedi.
- **HUD:** Chat artık HUD düzenlemeyi tetiklemez. Right Shift→HUD→**Open HUD Editor / HUD Editor Aç**. Bir dünyada olmalısın. Öğeyi seç/sürükle; kaydırarak ölçekle; sağda ölçek, konum, görünürlük, öğeyi etkinleştirme ve sıfırlama. Escape önceki ekrana döner, kayıt yapılır. Native ekranın press/drag/release olayları kullanılır; mobilde GLFW butonu basılı kalmış varsayımına dayanmaz. Düzenleyicide gizli/kapalı öğeler önizleme olarak seçilebilir. Target HUD dünyaya bağlıdır; sürüklenen sabit kart yapılmamıştır. Ayrıntılı içerik ayarları standart modül panelinde kalır.
- İlk bağımsız Anubis panelleri, native HUD, Dusk, multiplayer yönlendirmeleri, totem/pickaxe görselleri ve korumalı input/native/Guts kaynakları korunur.

## Mobil ve genel kod düzeltmeleri
- Mobile JAR artık launcher otomatik algılamasından bağımsız **mobil profilini** uygular. Masaüstü JVM'de açılması veya yanlış platform seçimi mobil bütçeleri kaldırmaz.
- Render3D eklenen/portlanan modüller için aynıframe içinde ortak1600 mobil /6000 PC primitive bütçesi kullanır; her modülde bütçeyi yeniden sıfırlama kaldırıldı. Çok sayıda gerçeklogo konturu bütçeyi doldurursa bazı işaretler çizilmeyebilir.
- Hive Finder bildirimhafızası uzaklaşılan chunklar için temizlenir. Abyz/Larp örnekleri ve geçmişi sınırlıdır; dünyadeğişince temizlenir.
- Kaynaktan kaynakları geri çıkarırken fabric.mod.json içindeki üretilmiş nested-JAR kayıtları silinir; Loom'un aynı kütüphaneyi ikikere gömmesine yol açan derleme hatası giderildi.
- Mobilde masaüstü ImGui nativeJAR'ları çıkarılmıştır; AndroidARM64 native korunur. Büyük sky dokuları768px, GUI/menu960px sınırına küçültülür. Fontlar, karakterler, Dusk sanatının kendisi, item/totem görselleri korunur. Ağırkozmetikler isteğe bağlı kalır; zayıf cihazlarda kapalı tut.
-0,75ms tarama bütçesi kooperatiftir; tek işbirimi aşabilir. “Sıfırlag” veya ölçülmüş FPS artışı iddiası yoktur.

## Larp yeniden ele alındı
V5'e section bazlı kümeler, paket/ses/loot/resend kanıtları, türbazlı sinerji ve azalma eklendi. V6'nın **8×8CHUNK** alanı ve pillar varsayılanı düzeltildi. V7/Finder artık genel depokümesi dedektörü değil; alınan spawnerları oyuncu/varlık belirtilerinden ayrı değerlendirir. Ayrıntı: LARP_BEHAVIOR_AUDIT.md ve addonun iç README'si.

**Bu addon hâlâ bağımsız Albiz uygulamasıdır; orijinal Meteor JAR'ı veya birebir davranış garantisi değildir.** Referansın ağır brute-force taraması ve yanıltıcı anti-ESP iddiaları kopyalanmadı. İmza listeleri farklı olabilir; doğal oluşumlar/ortam sesleri yanlışpozitif üretebilir.

## Testler / dürüst sınırlar
Derleme, CPU kuralları, kaynakbağlantıları, ZIP CRC, ABI, gerçek sesdosyası, logo geometri sınırları ve11 korumalı dosya hash'i kontrol edilir. Güncel sayılar CHECKS.json ve loglarda.

**Yapılmayan:** Minecraft açılışı, Android/Zalith cihaztesti, oyun içi HUD sürükleme, OpenGL/Vulkan shader, multiplayer sunucu, FPS ve ekranparitesi testi. Dolayısıyla “mobildeki bütün hatalar bitti” denmiyor. Runtime sorununda latest.log + tamcrashreport + launcher/renderer/cihaz ayrıntısını gönder.

## Kaynak ve geri dönüş
Albiz-1.0-source.zip + yanındaki PC/Albiz-Client.jar birlikte tüm buildgirdisidir. Kaynağı çıkar, Albiz dizininde `python restore_resources.py ../PC/Albiz-Client.jar` çalıştır; Java 21 ile `bash gradlew test build addonClasspath`. Addon: `bash larp-addon/build.sh`. Mobilpaketleme PythonPillow kullanır. İlkderleme internete ihtiyaç duyar. Konturverisi Java'da pişmiştir; OpenCV normalderleme için gerekli değildir.

Rollback.zip eklenmişse içindeki script yeni paketlerden önceki teslimi farklıdizine geriüretir; kurulu mods/config dosyalarını kendiliğinden değiştirmez. Dilseçimini yenilemek için önce config'i yedekle ve yalnız language-choice.txt dosyasını kaldır. Kullanıcı configleri/hesapları/sunucuları pakete alınmaz.

Lisanslar: LICENSE, NOTICE.md, THIRD_PARTY_PORTS.md, THIRD_PARTY_CHARACTERS.md ve licenses/. Kullanıcının sağladığı kurtkaydı için mevcut/orijinal kaynak lisansı geçerlidir; depo konumu tekbaşına yeni bir mülkiyet iddiası değildir. CodeXARR/Bassa veya orijinal Larpmodül bytecode'u eklenmedi.
