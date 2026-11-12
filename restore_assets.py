#!/usr/bin/env python3
"""Restore the binary assets that the source tree is delivered without.

Usage:  python3 restore_assets.py [--anubis /path/to/Anubis-clone] [project dirs...]
Default projects: Albiz-PC Albiz-Mobil (next to this file).

Sources, both in the public repository github.com/alzzyy-cmd/Anubis and both SHA-256 verified:
  1. "SoberClient (3).zip.001/.002"  -> the Sober reference JAR; assets/sober/** is copied to
     assets/albiz/** (same relative path, which is what the Sober UI port in Albiz expects).
  2. "anubis.zip.001/.002" -> Albiz_Client_1_0_mobile.jar (an OLDER Albiz build); its
     assets/anubis/** is copied to assets/albiz/**, and the Android arm64 ImGui library is
     taken from its nested natives JAR.
Only binary files are copied and existing files are never overwritten, so the current JSON,
shader and language files stay as they are. Restored files are git-ignored.
"""
import hashlib, io, subprocess, sys, tempfile, zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
BINARY = {'.png', '.jpg', '.jpeg', '.ogg', '.ttf', '.otf', '.pcm', '.glb', '.anc', '.bin'}
SOBER_SHA = 'deb8c59fa14401d9960fe7099d9b774066e6964188fb8927640d479af16cdd64'
OLD_ALBIZ_SHA = 'df6811a7f4f2cbd17a9e8fbee32622a9cca3411bc01ddfd783bc2d5d475b07d9'
NATIVE_SHA = 'bb6ea1f6087ab0bfd8aafbbbb84a3826c1dfd293f7f535964aad094a0771d73c'
NATIVE_PATH = 'assets/albiz/natives/android-arm64/libimgui-java64.so'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def joined(repo, stem):
    return b''.join((repo / f'{stem}.{part}').read_bytes() for part in ('001', '002'))


def copy_tree(archive, prefix, projects):
    count = 0
    for name in archive.namelist():
        if not name.startswith(prefix) or name.endswith('/'):
            continue
        rel = Path(name[len(prefix):])
        if rel.is_absolute() or '..' in rel.parts or rel.suffix.lower() not in BINARY:
            continue
        data = None
        for project in projects:
            dest = project / 'src/main/resources/assets/albiz' / rel
            if dest.exists():
                continue
            data = archive.read(name) if data is None else data
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
            count += 1
    return count


def main(argv):
    anubis = None
    if '--anubis' in argv:
        i = argv.index('--anubis')
        anubis = Path(argv[i + 1])
        del argv[i:i + 2]
    projects = [Path(p).resolve() for p in argv] or [ROOT / 'Albiz-PC', ROOT / 'Albiz-Mobil']
    tmp = None
    if anubis is None:
        tmp = tempfile.TemporaryDirectory()
        anubis = Path(tmp.name) / 'Anubis'
        subprocess.run(['git', 'clone', '--depth', '1', 'https://github.com/alzzyy-cmd/Anubis.git', str(anubis)], check=True)

    sober = joined(anubis, 'SoberClient (3).zip')
    if sha(sober) != SOBER_SHA:
        raise SystemExit('Sober reference changed: SHA-256 mismatch')
    with zipfile.ZipFile(io.BytesIO(sober)) as z:
        print('from Sober JAR:', copy_tree(z, 'assets/sober/', projects), 'files written')

    with zipfile.ZipFile(io.BytesIO(joined(anubis, 'anubis.zip'))) as outer:
        old = outer.read('Albiz_Client_1_0_mobile.jar')
    if sha(old) != OLD_ALBIZ_SHA:
        raise SystemExit('Old Albiz JAR changed: SHA-256 mismatch')
    with zipfile.ZipFile(io.BytesIO(old)) as z:
        print('from old Albiz JAR:', copy_tree(z, 'assets/anubis/', projects), 'files written')
        with zipfile.ZipFile(io.BytesIO(z.read('META-INF/jars/imgui-java-natives-linux-1.90.0.jar'))) as natives:
            native = natives.read('io/imgui/java/native-bin/libimgui-java64.so')
    if sha(native) != NATIVE_SHA:
        raise SystemExit('Android native: SHA-256 mismatch')
    for project in projects:
        dest = project / 'src/main/resources' / NATIVE_PATH
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(native)
    print('Android arm64 native written, checksum verified')
    if tmp:
        tmp.cleanup()


if __name__ == '__main__':
    main(sys.argv[1:])
