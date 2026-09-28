#!/usr/bin/env bash
# Build natif (Arch) du GSI LineageOS 22.2 + patches TrebleDroid (AndyYan) + patches maison J606.
# Usage : build-gsi.sh [sync|patch|build|all] [userdebug|user]
#   sync  : repo sync + local manifests
#   patch : repopick + patches AndyYan + patches maison (/lab/lenovo/patches)
#   build : compile system.img -> /lab/lenovo/out/
#   all   : sync + patch + build (défaut)
set -euo pipefail

STEP="${1:-all}"
VARIANT="${2:-userdebug}"
SRC=/lab/lenovo/build/src
OUT_DIR=/lab/lenovo/out
MY_PATCHES=/lab/lenovo/patches/a15
TARGET=gsi_arm64_vN

export USE_CCACHE=1 CCACHE_EXEC=/usr/bin/ccache CCACHE_DIR=/lab/lenovo/build/ccache
# Identité git locale au build (les patches sont appliqués avec git am / cherry-pick)
export GIT_AUTHOR_NAME="nico-j606" GIT_AUTHOR_EMAIL="nico-j606@localhost"
export GIT_COMMITTER_NAME="$GIT_AUTHOR_NAME" GIT_COMMITTER_EMAIL="$GIT_AUTHOR_EMAIL"

cd "$SRC"

do_sync() {
    mkdir -p .repo/local_manifests
    cp lineage_build_unified/local_manifests_treble/*.xml .repo/local_manifests/
    repo sync -c --force-sync --no-clone-bundle --no-tags --optimized-fetch -j"$(nproc)"
}

apply_group() {
    local group_dir="$1"
    [ -d "$group_dir" ] || return 0
    echo "== patches : $group_dir"
    bash lineage_build_unified/apply_patches.sh "$group_dir"
}

do_patch() {
    set +u
    source build/envsetup.sh >/dev/null
    set -u
    repopick 321337 -r -f
    repopick 321338 -r -f
    repopick 321339 -r -f
    apply_group lineage_patches_unified/patches_platform
    apply_group lineage_patches_unified/patches_treble
    apply_my_patches
}

# Patches maison : <chemin/du/projet avec / remplacés par __>/*.patch, ex. frameworks__base/0001-x.patch.
# Pas de git reset ici (contrairement à apply_patches.sh d'AndyYan) pour garder ses patches déjà appliqués.
apply_my_patches() {
    local project_dir project patch
    shopt -s nullglob
    for project_dir in "$MY_PATCHES"/*/; do
        project="$(basename "$project_dir")"
        project="${project//__//}"
        echo "== patches maison : $project"
        for patch in "$project_dir"*.patch; do
            git -C "$project" am --3way "$patch" || { git -C "$project" am --abort; echo "Échec : $patch" >&2; exit 1; }
        done
    done
    shopt -u nullglob
}

do_build() {
    set +u
    source build/envsetup.sh >/dev/null
    source vendor/lineage/vars/aosp_target_release
    lunch "lineage_${TARGET}-${aosp_target_release}-${VARIANT}"
    set -u
    ccache -M 50G >/dev/null
    make installclean
    make -j"$(nproc)" systemimage
    mkdir -p "$OUT_DIR"
    local dest
    dest="$OUT_DIR/lineage-22.2-$(date -u +%Y%m%d)-nico-${TARGET}-${VARIANT}.img"
    cp "$OUT/system.img" "$dest"
    echo "Image : $dest"
}

case "$STEP" in
    sync)  do_sync ;;
    patch) do_patch ;;
    build) do_build ;;
    all)   do_sync; do_patch; do_build ;;
    *) echo "Étape inconnue : $STEP" >&2; exit 1 ;;
esac
