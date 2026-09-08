#!/usr/bin/env bash
# Собирает готовые к установке сборки клиента:
#   dist/WantedVisuals-Prism.zip  — инстанс для Prism Launcher / MultiMC
#   dist/WantedVisuals.mrpack     — пак для Modrinth App / ATLauncher / Prism
#   dist/WantedVisuals-mods.zip   — просто папка mods для официального лаунчера
set -euo pipefail

MC_VERSION="1.21.1"
LOADER_VERSION="0.16.5"
PACK_VERSION="${1:-1.0.0}"

root="$(cd "$(dirname "$0")/.." && pwd)"
dist="$root/dist"
work="$root/build/pack"

rm -rf "$work" "$dist"
mkdir -p "$work" "$dist"

mod_jar="$(ls "$root"/build/libs/*.jar | grep -v -- '-sources' | head -n1)"
echo "mod jar: $mod_jar"

# Fabric API нужен моду как зависимость — кладём его в сборку, чтобы всё работало из коробки.
echo "Загружаю Fabric API для $MC_VERSION…"
api_url="$(curl -sSf \
  "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=%5B%22$MC_VERSION%22%5D&loaders=%5B%22fabric%22%5D" \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)[0]["files"][0]["url"])')"
curl -sSfL "$api_url" -o "$work/fabric-api.jar"
echo "fabric api: $api_url"

# ---------- Prism / MultiMC ----------
prism="$work/prism"
mkdir -p "$prism/.minecraft/mods"
cp "$mod_jar" "$prism/.minecraft/mods/"
cp "$work/fabric-api.jar" "$prism/.minecraft/mods/"

cat > "$prism/instance.cfg" <<CFG
InstanceType=OneSix
name=Wanted Visuals $PACK_VERSION
iconKey=default
notes=Визуальный клиент Wanted Visuals для Minecraft $MC_VERSION. ClickGUI — RIGHT SHIFT.
OverrideMemory=true
MinMemAlloc=1024
MaxMemAlloc=4096
CFG

cat > "$prism/mmc-pack.json" <<PACK
{
  "components": [
    {
      "important": true,
      "uid": "net.minecraft",
      "version": "$MC_VERSION"
    },
    {
      "uid": "net.fabricmc.fabric-loader",
      "version": "$LOADER_VERSION"
    }
  ],
  "formatVersion": 1
}
PACK

(cd "$prism" && zip -qr "$dist/WantedVisuals-Prism.zip" .)

# ---------- Modrinth pack ----------
mr="$work/mrpack"
mkdir -p "$mr/overrides/mods"
cp "$mod_jar" "$mr/overrides/mods/"
cp "$work/fabric-api.jar" "$mr/overrides/mods/"

cat > "$mr/modrinth.index.json" <<INDEX
{
  "formatVersion": 1,
  "game": "minecraft",
  "versionId": "$PACK_VERSION",
  "name": "Wanted Visuals",
  "summary": "Визуальный клиент: японские шляпы, смена неба, ESP и своё главное меню.",
  "files": [],
  "dependencies": {
    "minecraft": "$MC_VERSION",
    "fabric-loader": "$LOADER_VERSION"
  }
}
INDEX

(cd "$mr" && zip -qr "$dist/WantedVisuals.mrpack" .)

# ---------- Просто mods/ ----------
plain="$work/plain/mods"
mkdir -p "$plain"
cp "$mod_jar" "$plain/"
cp "$work/fabric-api.jar" "$plain/"
(cd "$work/plain" && zip -qr "$dist/WantedVisuals-mods.zip" .)

ls -la "$dist"
