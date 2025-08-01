# Universal HUD Manager for Minecraft Forge 1.20.1

**Created by GreatTomFox & Sora collaboration project**

> [!WARNING]  
> **🚧 EARLY ALPHA - WORK IN PROGRESS 🚧**
> 
> This mod is currently in **early development stage**. Only basic HUD detection and UI framework are implemented.
> 
> **❌ NOT YET FUNCTIONAL:**
> - Actual HUD element repositioning
> - Real drag & drop of HUD elements  
> - Position saving/loading
> 
> **✅ CURRENTLY WORKING:**
> - HUD element detection (12 elements)
> - Edit mode toggle (H key)
> - Visual preview with green outlines
> 
> **Expected completion:** Phase 3 implementation (Mixin-based HUD control)

A powerful mod that allows universal positioning of HUD elements from vanilla Minecraft and other mods, inspired by Xaero's Minimap drag-and-drop system.

## 🌟 Features

### ✅ Currently Implemented (Phase 1-2)
- **Detect and manage HUD elements** from vanilla Minecraft and loaded mods ✅
- **Press H key** to toggle HUD edit mode ✅
- **Visual outlines** show editable HUD elements ✅
- **Automatic mod detection** for TerraFirmaCraft, JEI, Jade, and more ✅

### 🚧 Planned Features (Phase 3+)
- **Drag & Drop positioning** similar to Xaero's Minimap ⏳
- **Real-time preview** of HUD positions during editing ⏳
- **Position saving/loading** between game sessions ⏳
- **Intuitive interface** with full mouse interaction ⏳

### 🌍 Supported HUD Elements

#### Vanilla Minecraft
- Health Bar
- Food Bar  
- Experience Bar
- Hotbar
- Crosshair
- Chat
- Debug Info (F3)

#### Mod Support
- **TerraFirmaCraft**: Thirst Bar, Temperature Display
- **JEI**: Item List, Recipe GUI
- **Jade/WAILA**: Block Info Tooltips
- **Extensible**: Easy to add support for more mods

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
2. **See HUD Elements**: Green outlines show detected elements (12 total)
3. **Visual Preview**: See which HUD elements are detected
4. **Exit Edit Mode**: Press `H` key again

### What Doesn't Work Yet
- ❌ Actual dragging/repositioning of HUD elements
- ❌ Position saving between sessions  
- ❌ Real HUD movement (only visual preview)

### Advanced Features (Alpha Status)
- ✅ **Mod Detection**: Automatically discovers HUD elements from loaded mods
- ❌ **Configuration**: Position saving/loading (not implemented yet)
- ✅ **Compatibility**: Works alongside existing HUD mods (detection only)

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

- **Xaero's Minimap**: Inspiration for drag-and-drop HUD positioning
- **Jade/WAILA**: Reference for HUD rendering techniques
- **TerraFirmaCraft**: Target use case for mod-specific HUD elements
- **Minecraft Forge**: Framework that makes this mod possible

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](../../issues)
- **Modpack Integration**: This mod is designed to be modpack-friendly
- **Documentation**: See `/docs` folder for detailed technical documentation

---

**Made with ❤️ by GreatTomFox & Sora**  
*Making HUD management universal, one element at a time.*