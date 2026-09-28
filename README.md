# Custom LineageOS GSI for the Lenovo Tab P11 (TB-J606L)

A home-built Android 16 (LineageOS 23.2, TrebleDroid GSI) tuned for the **Lenovo Tab P11 TB-J606L**
(Snapdragon 662 / `bengal`, 4 GB RAM, Lenovo Android 11 vendor), plus the tooling to build, customize,
flash and benchmark it, and to go back to the stock firmware.

> Project notes and measurement logs under `notes/` are written in French.

## Results

| | Home screen: janky frames | Standby (unplugged) |
|---|---|---|
| Stock LineageOS 22.2 GSI (Android 15) | 49 % | – |
| MisterZtr LineageOS 23.2 GSI (Android 16) | 18-20 % | ~0.2 %/h |
| **This build + root tuning** | **~2-3 %** | ~0.2 %/h |

Details and methodology: [`notes/journal.md`](notes/journal.md), [`notes/bench.csv`](notes/bench.csv).

## What differs from a stock GSI

- **Qualcomm CPU boost** ([`patches/a16/frameworks__base/0001`](patches/a16/frameworks__base)): Lenovo's power HAL
  reports the touch and launch boosts as unsupported, so a GSI never boosts. The patch calls
  `vendor.qti.hardware.perf` IPerf directly from `system_server` using the vendor's own hints
  (`0x1083` animation boost on touch, `0x1081` on app launch). See [`notes/boost-framework.md`](notes/boost-framework.md).
  Can be turned off with `setprop persist.sys.nico.qtiboost false` (root).
- **0.5x animation scales by default** (patch `0002`, SettingsProvider).
- **phh remote debugging removed** (`patches/a16/device__phh__treble`): a reverse SSH tunnel exposing adb.
- **Unneeded apps stripped** from the image ([`custom/config-a16.json`](custom/config-a16.json)): Messaging, SIM Toolkit,
  printing, live wallpapers, cell broadcast…
- **Root tuning at boot** ([`root/nico-tuning.sh`](root/nico-tuning.sh), Magisk `service.d`):
  `schedtune` top-app boost 10 + `prefer_idle`, `noop` I/O scheduler, zram `page-cluster=0`.
- **Disabled packages** ([`notes/disabled-packages.txt`](notes/disabled-packages.txt)): Qualcomm QMS telemetry,
  Seedvault, Dialer… (`pm enable <package>` to revert).

## Layout

```
scripts/     build, customize, flash, restore, benchmark
patches/     local patches per Android version (a15/, a16/), one directory per project (path with / -> __)
custom/      customize-gsi.py configs (paths to remove, props, APKs to bundle from custom/apps/)
root/        Magisk tuning script
notes/       project log, measurements, analyses (French)
car/         OBD2 data of the car (abandoned side project)
```

Not tracked (see `.gitignore`): `build/` (~120 GB of sources + ~100 GB of output), `out/`, `stock/`, images and APKs.

## Building

Requirements (Arch Linux): `repo`, `git-lfs`, `gperf`, `ccache`, `ncurses5-compat-libs`, Java 17, ~250 GB free.

```bash
# LineageOS 23.2 sources + MisterZtr's TrebleDroid manifest and patches
mkdir -p build/src16 && cd build/src16
repo init -u https://github.com/LineageOS/android.git -b lineage-23.2 --git-lfs --depth=1
git clone https://github.com/MisterZtr/treble_manifest.git .repo/local_manifests -b lineage-23.2
repo sync -c --force-sync --optimized-fetch --no-tags --no-clone-bundle -j8 --retry-fetches=5
bash LineageOS_gsi/patches/apply-patches.sh .

# Local patches
for d in ../../patches/a16/*/; do
  git -C "$(basename "$d" | sed 's#__#/#g')" am --3way "$d"*.patch
done
cd ../..

# Build (~3 h the first time, a few minutes afterwards), then customize
scripts/build-gsi16.sh userdebug
scripts/customize-gsi.py custom/config-a16.json      # -> out/system-custom-a16.img
```

## Flashing

Requires an unlocked bootloader. **Wipe data after any change to the system image**
(a dirty flash led to "Can't load Android system").

```bash
scripts/flash-gsi.sh out/system-custom-a16.img   # first time: vbmeta without verification, product_a removed, wipe
# afterwards: adb reboot fastboot ; fastboot flash system_a out/system-custom-a16.img ; fastboot -w ; fastboot reboot
```

Root: patch the `boot.img` **from the exact firmware version** (S320435) with Magisk, then
`fastboot flash boot_a magisk_patched.img`.

## Back to the Lenovo firmware

Official firmware `TB-J606L_S320435_250303_ROW` (mirror: lolinet). The QPST package splits `super` into chunks;
`super.img` is rebuilt from the LP metadata offsets (details in `notes/journal.md`), then:

```bash
scripts/restore-stock.sh   # boot, recovery, dtbo, vbmeta, super + wipe
```

## Benchmarking

```bash
scripts/launcherjank.sh           # home screen smoothness (gfxinfo)
scripts/bench.sh "<label>"        # scrolling + cold app launches -> notes/bench.csv
scripts/sleep-report.sh 69 10     # deep sleep after an unplugged test (start level %, hours)
```

## Credits

[LineageOS](https://lineageos.org), [TrebleDroid](https://github.com/TrebleDroid) (phhusson),
[MisterZtr/LineageOS_gsi](https://github.com/MisterZtr/LineageOS_gsi),
[AndyCGYan](https://github.com/AndyCGYan/lineage_build_unified), [Magisk](https://github.com/topjohnwu/Magisk).
The patches modify AOSP code licensed under Apache 2.0.
