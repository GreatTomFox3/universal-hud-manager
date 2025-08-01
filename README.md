# Universal HUD Manager for Minecraft Forge 1.20.1

**Created by GreatTomFox & Sora collaboration project**

A powerful mod that allows universal positioning of HUD elements from vanilla Minecraft and other mods, inspired by Xaero's Minimap drag-and-drop system.

## 🌟 Features

### ✨ Universal HUD Control
- **Detect and manage HUD elements** from vanilla Minecraft and loaded mods
- **Drag & Drop positioning** similar to Xaero's Minimap (future feature)
- **Real-time preview** of HUD positions during editing
- **Automatic mod detection** for TerraFirmaCraft, JEI, Jade, and more

### 🎮 Easy to Use
- **Press H key** to toggle HUD edit mode
- **Visual outlines** show editable HUD elements
- **Intuitive interface** with clear instructions
- **Modpack friendly** - no source modifications needed

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

1. Download the latest release from [Releases](../../releases)
2. Place the `.jar` file in your `mods/` folder
3. Launch Minecraft with Forge 1.20.1
4. Press **H** key in-game to start editing HUD positions!

## 🎯 Usage

### Basic Usage
1. **Enter Edit Mode**: Press `H` key
2. **See HUD Elements**: Green outlines show draggable elements
3. **Position Elements**: Click and drag to new positions (coming soon)
4. **Exit Edit Mode**: Press `H` key again

### Advanced Features
- **Mod Detection**: Automatically discovers HUD elements from loaded mods
- **Configuration**: Positions are saved and restored between sessions
- **Compatibility**: Works alongside existing HUD mods

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