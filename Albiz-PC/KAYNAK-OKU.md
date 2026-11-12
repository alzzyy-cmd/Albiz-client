# Güncel Albiz 1.0 kaynakları — PC
Bu klasör en son Friends/Evidence revizyonudur: 176 Java testi, 45 Python kontrolü, 26 addon CPU kontrolü. Albiz modülleri ve Larp addon kaynakları birlikte içeridedir. Orijinal proje kaynaklarıdır; Albiz JAR'ını decompile ederek kayıp isimler üretmedik.

## Yapı ve gerekenler
PC ve Mobil aynı Java kodunu paylaşır; bu kopyanın profil girdisi `pc` olarak src/main/resources/albiz-profile.properties içindedir. Kaynak kodlar ayrı klasörlerde bağımsız bulunur.
Java 21 gerekir. Gradle ilk çalıştırmada bağımlılık indirir.

## İkili kaynaklar
Bu ZIP kaynak teslimidir, hazır istemci JAR'ı değildir. Büyük PNG/font/OGG/native/model dosyaları tekrar eklenmemiştir; JSON/shader/metin kaynakları dahil edilmiştir. Kaynaklar tek başına çalışır oyun paketi değildir. İlgili güncel JAR daha önce teslim edilen `Albiz-1.0-Friends/PC/Albiz-Client.jar` dosyasıdır. Eski revizyonun JAR'ını kullanma.

Bu klasörde çalıştır:
`python restore_resources.py /tam/yol/Albiz-1.0-Friends/PC/Albiz-Client.jar`
`bash gradlew test build addonClasspath --max-workers=1`

restore_resources.py profil properties dosyasını silmez; generated nested-JAR kayıtlarını metadata'dan çıkarır. Mobil runtime profili korunur. Release küçültmeleri/native ayırma için tools/package_revision.py kullanılır; bu araç kaynak ağacından hem PC hem mobil teslim üretir. Normal gradlew build tek başına mobil resim küçültmesini uygulamaz. Larp kaynakları larp-addon/ içindedir. Lisansları ve NOTICE dosyalarını koru.

Geri kurulum için gereken JAR SHA256: 3b1296548aaaddca80f27c237bc626570f795c6777771eb2b9d03363e06ba3ba
Minecraft/Android/renderer/FPS testi yapılmadı; ayrıntılar README.md.
