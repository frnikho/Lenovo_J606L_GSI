#!/usr/bin/env bash
# Build natif du GSI LineageOS 23.2 (Android 16) : patches TrebleDroid de MisterZtr + patches maison.
# Préparation (une fois) : repo init lineage-23.2 + local manifest MisterZtr/treble_manifest, repo sync,
#   bash LineageOS_gsi/patches/apply-patches.sh . , puis git am des patches de /lab/lenovo/patches/a16.
# Usage : build-gsi16.sh [userdebug|user]
set -euo pipefail

VARIANT="${1:-userdebug}"
SRC=/lab/lenovo/build/src16
OUT_DIR=/lab/lenovo/out
TARGET=lineage_arm64_bvN4   # vanilla, ext4 (le noyau 4.19 Lenovo ne lit pas forcément EROFS)
RELEASE=bp4a

export USE_CCACHE=1 CCACHE_EXEC=/usr/bin/ccache CCACHE_DIR=/lab/lenovo/build/ccache CCACHE_COMPRESS=1

cd "$SRC"
# TrebleApp.apk est un prébuilt attendu par le build : il se compile à part avec Gradle (Java 17)
if [ ! -f treble_app/TrebleApp.apk ]; then
    (cd treble_app && bash build.sh release)
fi
set +u
source build/envsetup.sh >/dev/null
breakfast "${TARGET}-${RELEASE}-${VARIANT}"
set -u
ccache -M 80G >/dev/null
make installclean
make -j"$(nproc)" systemimage
mkdir -p "$OUT_DIR"
dest="$OUT_DIR/lineage-23.2-$(date -u +%Y%m%d)-nico-arm64_bvN4-${VARIANT}.img"
cp "$OUT/system.img" "$dest"
echo "Image : $dest"
