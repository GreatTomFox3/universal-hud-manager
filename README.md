# Universal HUD Manager

A Minecraft Forge 1.20.1 mod that lets you freely reposition HUD elements (Health, Food, Armor, XP, etc.) via config file, with a visual editor screen.

**Created by GreatTomFox & Sola (AI Assistant)**

---

## Features

- **Reposition HUD elements** — Health, Food, Armor, XP bar, Hotbar, Air bar, and more
- **Visual HUD Editor** — Press `H` to open the editor screen and see your layout in real time
- **Toggle individual HUDs** — Enable/disable each element independently via config
- **Vehicle HUD support** — Horse health and jump bar repositioning
- **Boss Bar support** — Repositionable boss bar
- **AppleSkin integration** — Saturation overlay, exhaustion underlay, hunger/health prediction (built-in, no AppleSkin required)
- **Mod compatibility** — Works alongside mods that cancel vanilla overlays (e.g. TACZ, Dynamic Crosshair)

---

## ⚠️ Limitations

**UHM can only reposition vanilla Minecraft HUD elements.**
HUD elements added by other mods (e.g. weapon displays, custom health bars, ability gauges) are drawn independently by those mods and cannot be moved by UHM.

If a mod's HUD is not moving, it is likely drawn by that mod itself — not by vanilla Minecraft.
Each mod requires individual compatibility work to support repositioning.

Currently confirmed compatible mods are listed in the [Mod Compatibility](#mod-compatibility) section below.

---

## Mod Compatibility

### TACZ (Timeless and Classics Zero)
Holding a TACZ gun no longer causes UHM HUDs to disappear.
UHM uses `IGuiOverlay` registered with `registerAboveAll`, making it fully independent from vanilla overlay events.

### AppleSkin
AppleSkin functionality is built directly into UHM.
You do **not** need AppleSkin installed — saturation, exhaustion, and food prediction are displayed on UHM's repositioned food bar automatically.

> AppleSkin code ported from [squeek502/AppleSkin](https://github.com/squeek502/AppleSkin) (public domain).

---

## Installation

1. Install [Minecraft Forge 1.20.1](https://files.minecraftforge.net/)
2. Drop the mod JAR into your `mods/` folder
3. Launch the game

---

## Usage

### HUD Editor
Press **`H`** in-game to open the visual HUD editor.

### Config File
Located at: `config/universalhudmanager-client.toml`

Each HUD element has:
- `enabled` — Show/hide the element
- `offsetX` / `offsetY` — Pixel offset from the default position

#### Config Sections

| Section | Contents |
|---------|----------|
| `hud_enabled` | Toggle each HUD element on/off |
| `hud_positions` | X/Y offset for each element |
| `health_display` | Health bar display options |
| `food_display` | Food bar display options |
| `armor_display` | Armor bar display options |
| `vehicle_hud` | Vehicle health/jump bar options |
| `appleskin_compat` | Saturation / exhaustion / hunger prediction toggles |

#### AppleSkin Compat Options

| Key | Default | Description |
|-----|---------|-------------|
| `show_saturation_overlay` | `true` | Yellow sparkle overlay on food bar |
| `show_exhaustion_underlay` | `true` | Shadow underlay showing exhaustion level |
| `show_hunger_restored` | `true` | Preview of hunger restored when holding food |
| `show_health_restored` | `true` | Preview of HP restored when holding food |

---

## Building from Source

Requires JDK 17.

```bash
./gradlew build
```

Output JAR will be in `build/libs/`.

---

## License

MIT License — see [LICENSE](LICENSE) for details.

AppleSkin portions are public domain (credit: squeek502).
