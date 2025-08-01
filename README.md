# Universal HUD Manager for Minecraft Forge 1.20.1

**Created by GreatTomFox & Sora collaboration project**

> [!WARNING]  
> **🚧 EARLY ALPHA - WORK IN PROGRESS 🚧**
> 
> This mod is currently in **early development stage**. Only basic UI framework and visual preview system are implemented.
> 
> **❌ NOT YET FUNCTIONAL:**
> - Actual HUD element repositioning
> - Real drag & drop of HUD elements  
> - Position saving/loading
> 
> **✅ CURRENTLY WORKING:**
> - Edit mode toggle (H key)
> - Visual preview with green outlines (placeholder system)
> - Basic UI framework and screen management
> 
> **Expected completion:** Phase 3 implementation (Mixin-based HUD control)

A powerful mod that allows universal positioning of HUD elements from vanilla Minecraft and other mods, inspired by Giacomo's HUD Overlays Configurator and Xaero's Minimap drag-and-drop functionality.

## 🌟 Features

### ✅ Currently Implemented (Phase 1-2)
- **Press H key** to toggle HUD edit mode ✅
- **Basic UI framework** with screen management ✅
- **Visual outlines** placeholder system (green boxes) ✅

### 🚧 Planned Features (Phase 3+)
- **HUD element detection** from vanilla Minecraft and loaded mods ⏳
- **Automatic mod detection** for TerraFirmaCraft, JEI, Jade, and more ⏳  
- **Drag & Drop positioning** similar to Xaero's Minimap ⏳
- **Real-time preview** of actual HUD positions during editing ⏳
- **Position saving/loading** between game sessions ⏳
- **Intuitive interface** with full mouse interaction ⏳

### 🌍 Supported HUD Elements

#### Vanilla Minecraft (Planned)
- Health Bar
- Food Bar  
- Experience Bar
- Hotbar
- Crosshair
- Chat

#### Mod Support (Planned)
- **TerraFirmaCraft**: Thirst Bar, Temperature Display
- **Jade/WAILA**: Block Info Tooltips (if possible to override mod's own config)
- **Automatic detection**: Support will be added automatically as mods are detected

## 🚀 Installation

> [!WARNING]
> **No releases available yet!** This mod is in early development.

**For developers/testers only:**
1. Clone this repository
2. Build with `./gradlew build`  
3. Place the generated `.jar` file in your `mods/` folder
4. Launch Minecraft with Forge 1.20.1
5. Press **H** key in-game to see HUD detection (visual preview only)

## 🎯 Current Usage (Alpha)

### What Works Now
1. **Enter Edit Mode**: Press `H` key
2. **Visual Placeholder System**: Green outlines appear as placeholders (not real HUD detection)
3. **Basic Drag Interface**: Placeholder green boxes can be dragged around (demonstration only)
4. **Exit Edit Mode**: Press `H` key again

### What Doesn't Work Yet
- ❌ Actual dragging/repositioning of HUD elements
- ❌ Position saving between sessions  
- ❌ Real HUD movement (only visual preview)

### Advanced Features (Alpha Status)
- ❌ **HUD Detection**: Real HUD element detection (not implemented yet)
- ❌ **Mod Detection**: Automatic mod HUD discovery (not implemented yet)
- ❌ **Configuration**: Position saving/loading (not implemented yet)
- ✅ **Basic Framework**: UI foundation and screen management

## 🔧 Development

### Building from Source
```bash
git clone https://github.com/GreatTomFox3/universal-hud-manager.git
cd UniversalHudManager/Forge1.20.1
./gradlew build
```

### Project Structure
```
src/main/java/com/greattomfoxsora/universalhudmanager/
├── UniversalHudManager.java          # Main mod class
├── core/
│   ├── HUDElement.java               # HUD element representation
│   └── HUDRegistry.java              # Central HUD management
└── client/
    ├── HUDPositionHandler.java       # Rendering and positioning
    └── KeyBindings.java              # Key input handling
```

## 🎨 Architecture

### Core Components
- **HUDRegistry**: Central registry for all discovered HUD elements
- **HUDElement**: Represents individual HUD elements with position data
- **HUDPositionHandler**: Handles rendering modifications and edit mode
- **KeyBindings**: Manages keyboard input for edit mode toggle

### Design Philosophy
- **Non-intrusive**: No modification of other mods required
- **Universal**: Works with any mod that renders HUD elements
- **User-friendly**: Simple and intuitive interface
- **Modpack-ready**: Easy integration into existing modpacks

## 🤝 Contributing

We welcome contributions! Please feel free to:
- Report bugs via GitHub Issues
- Submit feature requests
- Create pull requests for improvements
- Add support for additional mods

## 🙏 Acknowledgments

This project draws inspiration from existing mods and frameworks:

- **Giacomo's HUD Overlays Configurator**: Primary inspiration for HUD management concept
- **Xaero's Minimap**: Inspiration for drag-and-drop positioning interface
- **TerraFirmaCraft**: Target use case demonstrating need for mod-specific HUD control
- **Minecraft Forge**: Essential framework enabling mod development

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](../../issues)
- **Modpack Integration**: This mod is designed to be modpack-friendly
- **Documentation**: See `/docs` folder for detailed technical documentation

---

**Made with ❤️ by GreatTomFox & Sora**  
*Making HUD management universal, one element at a time.*