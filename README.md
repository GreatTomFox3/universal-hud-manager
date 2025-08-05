# Universal HUD Manager for Minecraft Forge 1.20.1

**Created by GreatTomFox & Sora collaboration project**

> [!NOTE]  
> **🚧 DEVELOPMENT VERSION - BASIC FUNCTIONALITY IMPLEMENTED 🚧**
> 
> Universal HUD Manager provides basic positioning control for Vanilla HUD elements.
> 
> **✅ CURRENTLY WORKING:**
> - **Main Vanilla HUD elements** - Health, Food, Experience, Air, Hotbar positioning via config file
> - **Horse HUD elements** - Horse health and jump gauge rendering implemented
> - **Armor bar** - Basic positioning with absorption hearts support
> - **Config-based positioning** - 1-pixel precision adjustment via config file editing
> - **Some Resource Pack support** - Tested with several texture packs, may not work with all
> - **Edit mode UI** - H key shows edit interface (visual only, no actual editing yet)
> 
> **⚠️ LIMITATIONS:**
> - **Config file editing required** - No real-time drag & drop editing
> - **Limited Resource Pack testing** - May not work with all texture modifications
> - **No multi-mod HUD support** - Only Vanilla HUD elements currently supported
> - **Alpha quality** - Expect bugs and issues during gameplay

A mod that provides basic positioning control for Vanilla Minecraft HUD elements via config file editing, inspired by existing HUD management mods.

## 🌟 Features

### ✅ Currently Implemented
- **Basic HUD positioning** - Main Vanilla HUD elements can be repositioned via config ✅
- **Config-based control** - 1-pixel precision adjustment through file editing ✅
- **Horse HUD rendering** - Horse health and jump gauge basic implementation ✅
- **Armor bar positioning** - Basic positioning with absorption hearts consideration ✅
- **Some Resource Pack support** - Works with tested texture packs (not all) ⚠️
- **Edit mode UI framework** - H key shows interface (no actual editing functionality) ⚠️
- **Position calculation system** - Basic coordinate management implemented ✅

### 🎯 Precision Control System
- **1-pixel adjustment** - Fine-tune HUD positions with perfect precision
- **Independent element control** - Each HUD element can be positioned separately
- **Bar/text independent positioning** - Experience bar and numbers can be adjusted separately
- **Dynamic positioning** - Smart positioning that adapts to game state changes
- **Vanilla-perfect integration** - Seamless integration with Minecraft's rendering system

### 🌍 Supported HUD Elements

#### Vanilla Minecraft (⚠️ Basic Implementation)
- **Health Bar** - Basic position control implemented ✅
- **Food Bar** - Basic positioning functionality ✅
- **Experience Bar** - Position control with separate bar/text positioning ✅
- **Air/Oxygen Bar** - Basic underwater gauge positioning ✅
- **Hotbar** - Basic hotbar positioning ✅
- **Horse Health** - Horse health display with multi-row rendering ✅
- **Horse Jump Gauge** - Jump charging display with space key detection ✅
- **Armor Bar** - Basic positioning with absorption hearts support ✅

#### Additional Features
- **Resource Pack Integration** - Works with some icons.png/widgets.png customizations (limited testing)
- **Game state awareness** - Different positioning for horse riding and underwater states
- **Vanilla-compatible timing** - Attempts to match Vanilla rendering behavior
- **Basic performance** - Minimal impact during normal gameplay (not extensively tested)

## 🚀 Installation

**Development Build:**
1. No stable releases yet - build from source only
2. Place the generated `.jar` file in your `mods/` folder
3. Launch Minecraft with Forge 1.20.1-47.3.0 or later
4. **Performance**: Basic optimization, may have some impact during gameplay
5. **Compatibility**: Limited testing with other mods, use with caution

**Build from Source:**
```bash
git clone https://github.com/GreatTomFox3/universal-hud-manager.git
cd universal-hud-manager
./gradlew build
```

## 🎯 How to Use

### Config-based Position Control
1. **Edit Mode**: Press `H` key to view edit interface (visual only, no editing functionality)
2. **Configuration**: Edit `config/universalhudmanager-client.toml` manually for positioning
3. **File-based Updates**: Config changes apply after game restart or reload
4. **Pixel Precision**: Manual adjustment with 1-pixel accuracy through config editing

### Advanced Configuration
- **Config File**: `config/universalhudmanager-client.toml`
- **Vector2i Positioning**: Precise X,Y coordinate control for each element
- **Enable/Disable Control**: Individual HUD elements can be toggled on/off
- **Vanilla Position Manager**: Automatic position calculation for perfect Vanilla integration

### What Currently Works
- **Basic HUD Elements**: Position control for main Vanilla HUD components via config
- **Some Resource Pack Support**: Works with tested texture packs (compatibility not guaranteed)
- **Basic Performance**: Minimal impact during normal gameplay (limited testing)
- **Multi-state Support**: Basic positioning for different game scenarios (horse riding, underwater, etc.)

### Configuration Examples
```toml
[hud_positions]
health_position = [0, 0]        # Default Vanilla position
food_position = [-8, 0]         # 8 pixels left from Vanilla
experience_position = [1, 0]    # 1 pixel right for perfect alignment
air_position = [-8, 0]          # Matches food bar positioning
hotbar_position = [0, 0]        # Centered Vanilla position

[hud_enabled]
health_enabled = true
food_enabled = true
experience_enabled = true
air_enabled = true
hotbar_enabled = true
```

## 🔧 Development

### Technical Achievements

#### IDE-Based Development
Universal HUD Manager was developed using **IntelliJ IDEA environment** with Claude Code assistance:

- **Minecraft source access**: Analysis of Minecraft's HUD rendering through IDE environment
- **Mapping file reference**: Field name resolution using MCP ↔ SRG name mappings
- **Vanilla implementation study**: Attempted reproduction of Minecraft's HUD behavior
- **Development debugging**: Analysis of HUD positioning and rendering through IDE tools

#### Current Architecture
- **Vanilla Position Manager**: Coordinate calculation system that attempts to replicate Minecraft's positioning
- **Resource Pack Compatibility**: Basic rendering system that works with some texture modifications
- **Multi-state HUD Management**: Basic handling of different game states (horse riding, underwater, etc.)
- **Performance Considerations**: Attempts to minimize rendering overhead (limited testing)

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
│   ├── ResourcePackCompatibleOverlays.java    # Advanced rendering system
│   ├── VanillaHudController.java              # Vanilla HUD management
│   └── KeyBindings.java                        # Key input handling
└── config/
    └── HUDConfig.java                          # Configuration management with VanillaPositionManager
```

## 🎨 Architecture Highlights

### Core Components
- **VanillaPositionManager**: Coordinate system that attempts to replicate Minecraft's HUD positioning
- **ResourcePackCompatibleOverlays**: Rendering layer that integrates with some texture packs
- **IDE-Assisted Development**: Development methodology using IDE environment for Minecraft analysis
- **Precision Control**: 1-pixel positioning system through config file editing

### Design Philosophy
- **Vanilla Compatibility**: Attempt to match Minecraft's HUD behavior where possible
- **Performance Awareness**: Minimize rendering impact during gameplay
- **Basic Compatibility**: Work with common modpacks and texture packs where tested
- **Development Transparency**: Open development process with available technical documentation
- **Iterative Development**: Gradual improvement and testing of functionality

### Development Milestones
- **Phase 1**: HUD detection and basic framework ✅
- **Phase 2**: Drag & drop interface foundation ✅
- **Phase 3**: Complete Vanilla HUD mastery ✅
- **Future**: Multi-mod HUD integration and advanced features

## 🚀 Future Development

### Potential Future Enhancements
- **Multi-mod HUD support**: Investigate detection and control of other mods' HUD elements
- **Real-time edit mode**: Attempt to implement drag & drop positioning functionality
- **Preset system**: Consider save and load different HUD layout configurations
- **GUI configuration**: Explore in-game configuration interface options

### Technical Considerations
- **API exploration**: Investigate potential API for other mod developers
- **Performance testing**: Further testing and optimization for various environments
- **Enhanced positioning**: Explore dynamic positioning based on game events
- **Platform support**: Consider Fabric version if development continues

## 🤝 Contributing

We welcome contributions from the community:

- **Bug Reports**: [Create an issue](../../issues) for any problems encountered
- **Feature Requests**: Suggest new features or improvements
- **Pull Requests**: Contribute code improvements or new features
- **Documentation**: Help improve documentation and guides
- **Testing**: Test with different modpacks and texture packs

## 🚧 Development Status

This project is an ongoing development effort:

- **Current State**: Basic HUD positioning functionality implemented
- **Development Approach**: IDE-integrated development for accurate implementation
- **Community Value**: Provides basic HUD customization for users who need it
- **Quality Goal**: Aiming to provide reliable HUD management functionality

## 🙏 Acknowledgments

- **Giacomo's HUD Overlays Configurator**: Original inspiration for HUD management concepts
- **FirstAid mod**: Technical reference for advanced HUD integration techniques
- **Minecraft Forge community**: Excellent documentation and development tools
- **IDE Environment Revolution**: Breakthrough development methodology achievement

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](../../issues)
- **Technical Documentation**: Comprehensive guides for developers and advanced users
- **Community Support**: Active development with regular updates and improvements
- **Modpack Integration**: Designed for seamless modpack integration with detailed compatibility notes

---

**Made with ❤️ by GreatTomFox & Sora**  
*Developing HUD management functionality through AI-human collaboration.*

**🚧 Development ongoing - Basic HUD positioning functionality available 🚧**