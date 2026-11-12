#!/usr/bin/env python3
"""Add English/Turkish strings to both lang files. Usage: add_lang.py <project dir>
Labels use the key label.<sha256(English)[:14]>; existing English values are not duplicated."""
import hashlib, json, sys, pathlib
MODULES = {
 "skeleton-esp": ("Skeleton ESP",
   "Draws a skeleton on players that follows their walking, sneaking and head movement.",
   "Oyuncuların üzerine yürüyüşünü, eğilmesini ve baş hareketini takip eden bir iskelet çizer."),
}
LABELS = {
 "Max Players": "En Fazla Oyuncu",
 "Show Self In Third Person": "Üçüncü Şahısta Kendini Göster",
 "Show Invisible Players": "Görünmez Oyuncuları Göster",
 "Highlight Friends": "Arkadaşları Vurgula",
 "Sneaking Color": "Eğilme Rengi",
 "Performance Profile": "Performans Profili",
 "Quality": "Kalite",
 "Balanced": "Dengeli",
 "Chunk Verdict": "Chunk Kararı",
 "Outlines chunks by how suspicious their blocks are together.": "Chunk'ları, içlerindeki blokların birlikte ne kadar şüpheli olduğuna göre çerçeveler.",
 "Only Strong Verdicts": "Yalnızca Güçlü Kararlar",
 "Outlines only chunks with several kinds of player-made evidence.": "Yalnızca birkaç farklı türde oyuncu yapımı kanıt bulunan chunk'ları çerçeveler.",
 "Suspicious Chunk Color": "Şüpheli Chunk Rengi",
 "Chunks with a cluster or one player-made sign.": "Bir küme ya da tek bir oyuncu yapımı iz bulunan chunk'lar.",
 "Strong Chunk Color": "Güçlü Chunk Rengi",
 "Chunks with several kinds of evidence.": "Birkaç farklı türde kanıt bulunan chunk'lar.",
 "Range": "Menzil", "Line Width": "Çizgi Kalınlığı", "Through Walls": "Duvar Arkası",
 "Color": "Renk", "Friend Color": "Arkadaş Rengi", "Performance": "Performans", "Custom": "Özel",
}
root = pathlib.Path(sys.argv[1]) / "src/main/resources/assets/albiz/lang"
en = json.loads((root / "en_us.json").read_text(encoding="utf-8"))
tr = json.loads((root / "tr_tr.json").read_text(encoding="utf-8"))
added = 0
for mid, (name, d_en, d_tr) in MODULES.items():
    for k, e, t in ((f"module.{mid}.name", name, name), (f"module.{mid}.description", d_en, d_tr)):
        if k not in en: added += 1
        en[k] = e; tr[k] = t
for e, t in LABELS.items():
    k = "label." + hashlib.sha256(e.encode()).hexdigest()[:14]
    if k in en or any(key.startswith("label.") and v == e for key, v in en.items()):
        continue
    en[k] = e; tr[k] = t; added += 1
for path, data in ((root / "en_us.json", en), (root / "tr_tr.json", tr)):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("added", added, "keys; total", len(en), len(tr), "same keyset", set(en) == set(tr))
