# Universal HUD Manager for Minecraft Forge 1.20.1

**Created by GreatTomFox & Sora collaboration project**

> [!WARNING]  
> **🚧 ALPHA VERSION - PARTIAL IMPLEMENTATION 🚧**
> 
> This mod is currently in **early alpha stage** with basic functionality implemented.
> 
> **✅ CURRENTLY WORKING:**
> - Experience Bar positioning with enhanced rendering (prettier than vanilla's messy numbers!)
> - Health, Food, and Air Bar rendering system (position control via config file only)
> - Edit mode framework (H key toggle)
> - Basic HUD overlay system
> 
> **⚠️ LIMITATIONS & KNOWN ISSUES:**
> - **No real-time HUD editing** - H key shows edit UI but can't drag vanilla HUDs yet, positions must be set via config file manually
> - **Performance impact** - debug logging causes some game slowdown  
> - **4 out of 5 vanilla HUD elements** - Health/Food/Experience/Air implemented, Chat positioning not implemented (can chat even be moved?)
> 
> **🔮 FUTURE GOALS:**
> - **H key real-time editing** - Press H to open edit GUI with drag & drop interface for instant HUD positioning
> - **Multi-mod HUD detection** - Automatic discovery of other mods' HUD elements
> - **Performance optimization** - Remove debug logging overhead

A powerful mod that allows universal positioning of HUD elements from vanilla Minecraft and other mods, inspired by Giacomo's HUD Overlays Configurator and Xaero's Minimap drag-and-drop functionality.

## 🌟 Features

### ✅ Currently Implemented (Alpha State)
- **Experience Bar enhancement** - Cleaner number rendering compared to vanilla's irregular patterns ✅
- **Basic HUD overlay system** - Health, Food, Air Bar rendering framework ✅
- **H key framework** - Edit mode toggle (UI only, no actual HUD control yet) ✅
- **Config-based positioning** - Manual position setting via config file ✅
- **Resource Pack partial support** - Works with some texture elements ✅

### 🔮 Planned Features (Development Goals)
- **H key real-time editing** - Press H to open edit GUI where you can drag & drop vanilla HUD elements in real-time ⏳
- **Intelligent position saving** - Dragged positions automatically saved to config file as X,Y coordinates (no manual editing) ⏳
- **Multi-mod HUD detection** - Automatic discovery and control of other mods' HUD elements ⏳
- **Performance optimization** - Remove debug logging overhead, improve efficiency ⏳
- **Chat positioning** - Add the 5th major HUD element ⏳
- **Advanced conflict resolution** - Smart handling of overlapping systems ⏳

### 🌍 Supported HUD Elements

#### Vanilla Minecraft (⚠️ Partial Implementation)
- **Health Bar** - Rendering system implemented, config-based positioning only ⚠️
- **Food Bar** - Rendering system implemented, config-based positioning only ⚠️  
- **Experience Bar** - Enhanced rendering with cleaner numbers (vanilla numbers are messy!) ✅
- **Air/Oxygen Bar** - Rendering system implemented, config-based positioning only ⚠️
- **Chat** - Not yet implemented (can chat even be moved?) ❌

#### Mod Support (🔮 Future Plans)
- **Universal Architecture** - Planned automatic detection system ⏳
- **Conflict Resolution** - Planned intelligent integration ⏳
- **Extensibility** - Framework designed for easy mod support addition ⏳

## 🚀 Installation

> [!CAUTION]
> **Alpha testing only!** This mod has performance issues and limited functionality.

**Installation Steps (Testers Only):**
1. No releases available - build from source only
2. Download and extract the source code, then run `./gradlew build`
3. Place the generated `.jar` file in your `mods/` folder
4. Launch Minecraft with Forge 1.20.1
5. **Known issues:** Debug logging may cause performance slowdown
6. **Current limitation:** H key shows edit UI but doesn't control vanilla HUDs yet
7. **Position control:** Edit `config/universalhudmanager-client.toml` manually, or use Configured mod for in-game config editing

## 🎯 How to Use

### Current Usage (Alpha Limitations)
1. **Enter Edit Mode**: Press `H` key while in-game
2. **Visual Feedback**: Edit screen appears (framework only)
3. **⚠️ No Real Dragging**: HUD elements cannot be dragged yet
4. **Manual Config**: Edit `config/universalhudmanager-client.toml` to change positions
5. **Restart/Reload**: Changes require game restart or reload (Vanilla HUD changes apply without restart, use Configured mod for in-game config editing)

### What Actually Works
- **Experience Bar Enhancement**: Cleaner number rendering compared to vanilla's messy font
- **Config-Based Positioning**: Manual position control via config file editing
- **Basic Framework**: Edit mode UI foundation is implemented

### What Doesn't Work Yet
- **❌ Real-time HUD editing**: H key opens edit UI but can't actually drag vanilla HUDs yet
- **❌ Intelligent saving**: No automatic position saving to config file (manual editing required)
- **❌ Live preview**: Changes don't show until restart
- **❌ Performance**: Debug logging causes slowdown

### Configuration (Manual Only)
- **Config File**: `config/universalhudmanager-client.toml`
- **Manual Editing Required**: Use text editor to change X,Y coordinates
- **Restart/Reload**: Changes apply after restart or reload (Configured mod allows in-game config editing)

## 🔧 Development


### Project Structure
```
src/main/java/com/greattomfoxsora/universalhudmanager/
├── UniversalHudManager.java                    # Main mod class
├── core/
│   ├── HUDElement.java                         # HUD element representation
│   └── HUDRegistry.java                        # Central HUD management
├── client/
│   ├── HudEditScreen.java                      # Edit mode UI interface
│   ├── DraggableHudElement.java                # Drag & drop functionality
│   ├── ResourcePackCompatibleOverlays.java    # Rendering system
│   ├── VanillaHudController.java              # Vanilla HUD management
│   └── KeyBindings.java                        # Key input handling
└── config/
    └── HUDConfig.java                          # Configuration management
```

## 🎨 Architecture

### Core Components
- **HUDRegistry**: Central registry for all discovered HUD elements with automatic detection
- **HUDElement**: Represents individual HUD elements with position data and metadata
- **ResourcePackCompatibleOverlays**: Advanced rendering system with resource pack support
- **VanillaHudController**: Intelligent vanilla HUD management and conflict resolution
- **HudEditScreen**: Comprehensive edit mode interface with drag & drop
- **HUDConfig**: Persistent configuration management with Vector2i positioning

### Design Philosophy
- **Honest Development**: Transparent about current limitations and future goals
- **Quality Over Speed**: Focus on solid foundation rather than rushing features
- **User Experience Goals**: Aiming for intuitive drag & drop interface (not yet implemented)
- **Performance Awareness**: Currently has debug logging overhead, optimization planned
- **Universal Compatibility**: Designed to work with any mod (framework in place)
- **Incremental Progress**: Building robust foundation before adding advanced features

### Current Development Status
- **Alpha Stage**: Basic framework implemented, major features still in development
- **Experience Bar**: Successfully enhanced rendering quality (vanilla numbers are quite messy!)
- **UI Framework**: Edit mode foundation completed
- **Next Priorities**: Real drag & drop, vanilla HUD integration, performance optimization

## 🤝 Contributing

We welcome contributions! Please feel free to:
- Report bugs via GitHub Issues
- Submit feature requests
- Create pull requests for improvements
- Add support for additional mods

## 🙏 Acknowledgments

This project draws inspiration from existing mods:

- **Giacomo's HUD Overlays Configurator**: Primary inspiration for HUD management concept
- **Xaero's Minimap**: Inspiration for drag-and-drop positioning interface

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](../../issues)
- **Modpack Integration**: This mod is designed to be modpack-friendly
- **Documentation**: See `/docs` folder for detailed technical documentation

---

**Made with ❤️ by GreatTomFox & Sora**  
*Making HUD management universal, one element at a time.*