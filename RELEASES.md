# MOCA GUI Releases

## Download Latest Release

Visit the [Releases page](../../releases/latest) to download the latest version of MOCA GUI.

## Download Options

### 🪟 Windows Executable (Recommended)
- **File**: `MOCA-GUI.exe`
- **Size**: ~50-80 MB
- **Requirements**: Windows 10/11
- **Java**: Automatically downloads Java 17+ if needed
- **Installation**: None required - just download and run

### ☕ Cross-Platform JAR
- **File**: `moca-gui.jar`
- **Size**: ~40-60 MB  
- **Requirements**: Java 17+ installed
- **Platforms**: Windows, macOS, Linux
- **Run**: `java -jar moca-gui.jar`

## Quick Start Guide

1. **Download** the appropriate file for your system
2. **Run** the application:
   - Windows: Double-click `MOCA-GUI.exe`
   - Other platforms: `java -jar moca-gui.jar`
3. **Connect** to your MOCA server:
   - Go to the "Connections" tab
   - Click "New" to create a connection profile
   - Enter your MOCA server details
   - Click "Connect"
4. **Start developing**:
   - Switch to "Command & Script" tab
   - Enter MOCA commands (default: `list warehouses`)
   - Press F5 or click "Execute All"

## Features Overview

### 🔌 Connection Management
- Save multiple server profiles
- Quick connect to recent connections
- Secure credential storage
- Connection testing and validation

### 📝 Advanced Script Editor
- MOCA syntax highlighting
- Code completion and IntelliSense
- Real-time syntax checking
- Find/replace with regex support
- Bookmarks and goto line
- Code folding

### ⚡ Command Execution
- Execute single or multiple commands
- Selective execution (run only selected text)
- Parallel execution for performance
- Command history and suggestions

### 📊 Results & Data Management
- Tabbed results for multiple commands
- Advanced data grid with sorting/filtering
- Export to Excel, CSV, JSON, XML
- Copy data in multiple formats
- Result comparison tools
- Data visualization and statistics

### 🎨 Professional Interface
- Clean, modern UI design
- Resizable panels and layouts
- Layout persistence (saves your preferences)
- Comprehensive keyboard shortcuts
- Context-sensitive help

### 📈 Performance & Monitoring
- Execution time tracking
- Memory usage monitoring
- Performance metrics dashboard
- Query optimization insights

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Execute All Commands | `F5` |
| Execute Selected | `Ctrl+F5` |
| New Script | `Ctrl+N` |
| Open Script | `Ctrl+O` |
| Save Script | `Ctrl+S` |
| Save As | `Ctrl+Shift+S` |
| Find | `Ctrl+F` |
| Replace | `Ctrl+H` |
| Goto Line | `Ctrl+G` |
| Toggle Bookmark | `Ctrl+F2` |
| Format Code | `Ctrl+Alt+L` |
| Check Syntax | `F7` |
| Connect to Server | `F6` |

## System Requirements

### For Windows Executable (.exe)
- **OS**: Windows 10 or Windows 11
- **Memory**: 2GB RAM minimum, 4GB recommended
- **Disk**: 100MB free space
- **Java**: Automatically handled (downloads JRE if needed)

### For JAR File
- **Java**: OpenJDK or Oracle JDK 17 or higher
- **Memory**: 2GB RAM minimum, 4GB recommended  
- **Disk**: 100MB free space
- **OS**: Any platform supporting Java (Windows, macOS, Linux)

## Installation Instructions

### Windows Executable
1. Download `MOCA-GUI.exe` from the latest release
2. Save to any location (e.g., Desktop, Program Files)
3. Double-click to run
4. If prompted, allow Windows to download Java runtime

### JAR File (All Platforms)
1. Ensure Java 17+ is installed: `java -version`
2. Download `moca-gui.jar` from the latest release
3. Run from command line: `java -jar moca-gui.jar`
4. Or double-click if Java is properly associated

## Troubleshooting

### Application won't start
- **Windows**: Ensure you have internet connection for Java download
- **All platforms**: Verify Java 17+ is installed
- **Firewall**: Allow the application through your firewall

### Connection issues
- Verify MOCA server URL is correct
- Check network connectivity to the server
- Ensure credentials are valid
- Try the "Test Connection" feature

### Performance issues
- Close unused result tabs
- Increase Java heap size: `java -Xmx4g -jar moca-gui.jar`
- Check system resources in Performance tab

## Security Notes

- Connection credentials are stored encrypted locally
- All network communication uses HTTPS when available
- No data is sent to external servers
- Audit logging available for enterprise use

## Support & Feedback

- 🐛 **Bug Reports**: Create an issue on GitHub
- 💡 **Feature Requests**: Open a discussion on GitHub
- 📖 **Documentation**: Check the built-in help system (F1)
- 🔧 **Enterprise Support**: Contact your IT department

## Version History

Check the [Releases page](../../releases) for detailed changelog and version history.

---

**Built with ❤️ for MOCA developers**
