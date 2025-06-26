# MOCA GUI - Professional MOCA Development Environment

A modern, enterprise-grade Java-based GUI application for MOCA development with advanced features and Language Server Protocol support.

## Downloads

Get the latest release from the [Releases](https://github.com/your-repo/moca-language-server/releases) page:

### Windows Executable
- **File**: `MOCA-GUI.exe`
- **Size**: ~40-60 MB  
- **Requirements**: None - includes embedded JVM
- **Run**: Double-click the .exe file

### Cross-Platform JAR
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
   - Enter your MOCA server details
   - Click "Connect"
4. **Start developing**:
   - Switch to "Command & Script" tab
   - Enter MOCA commands (default: `list warehouses`)
   - Press F5 or click "Execute All"

## Features

### Connection Management
- Save multiple server profiles with secure credential storage
- Quick connect to recent connections
- Connection testing and validation
- Import/export connection profiles
- Favorite connections for quick access

### Advanced Script Editor
- MOCA syntax highlighting with professional color schemes
- Code completion and IntelliSense for MOCA commands
- Real-time syntax checking and error detection
- Find/replace with regex support
- Bookmarks and goto line functionality
- Code folding for better organization

### Command Execution
- Execute single or multiple commands
- Selective execution (run only selected text)
- Multi-command support with separate result tabs
- Command history and suggestions
- Performance monitoring and execution time tracking

### Results & Data Management
- Tabbed results for multiple commands
- Advanced data grid with sorting and filtering
- Export results to Excel, CSV, JSON, XML
- Copy data to clipboard in multiple formats
- Result comparison tools
- Search within results

### Data Analysis & Visualization
- Basic statistics for result data
- Data validation and quality checks
- Result comparison between executions
- Performance metrics and monitoring

### Professional Features
- Layout persistence (save/restore window positions)
- Comprehensive keyboard shortcuts
- Context-sensitive help system
- Audit logging and security features
- Modern, clean user interface

## Keyboard Shortcuts

- **F5**: Execute all commands
- **Ctrl+F5**: Execute selected text
- **Ctrl+N**: New script
- **Ctrl+O**: Open script
- **Ctrl+S**: Save script
- **Ctrl+F**: Find
- **Ctrl+H**: Find and replace
- **Ctrl+G**: Goto line
- **F1**: Help
- **F6**: Connect to server

## Building from Source

### Requirements
- Java 17+ 
- Maven 3.6+

### Build Commands
```bash
git clone https://github.com/your-repo/moca-language-server
cd moca-language-server
mvn clean package
```

### Run Options
```bash
# GUI Application
java -jar target/moca-gui.jar

# Language Server Mode (for VS Code integration)
java -jar target/moca-gui.jar --server
```

## Language Server Protocol Support

### VS Code Integration
- IntelliSense for MOCA, SQL, and Groovy
- Go to definition and reference finding
- Hover documentation for commands and functions
- Signature help for function parameters

### Language Features

|                           | MOCA                              | SQL                                                                  | Groovy                                            |
|---------------------------|-----------------------------------|----------------------------------------------------------------------|---------------------------------------------------|
| **Completion**            | commands, arguments, functions    | tables/views, indexes, columns, aliases, subqueries, CTEs, functions | imports, classes, methods/functions               |
| **Hover**                 | commands, functions               | tables/views, aliases, subqueries, CTEs, functions                   | imports, classes, methods/functions, variables    |
| **Definition Lookup**     | commands, triggers                |                                                                      | classes, methods/functions, variables             |
| **Diagnostics**           | errors, warnings                  | errors, warnings                                                     | errors, warnings                                  |
| **Formatting**            | on save, on paste, on type        | on save, on paste, on type                                           |                                                   |
| **Semantic Highlighting** | commands, streams(```;```)        | range(```[select..]```), tables/views                                | range(```[[..]]```)                               |
| **Signature Help**        | functions                         |                                                                      | methods/functions                                 |
| **References**            | commands                          |                                                                      |                                                   |


### Command Execution

- [x] MOCA Connection
    - [ ] Direct (legacy)
    - [x] http/https
        - [Fix https SSLHandshakeException demo]
            - [https SSLHandshakeException StackOverflow thread]
- [x] MOCA Script Execution
    - [x] Approve Unsafe Scripts
- [x] MOCA Tracing
- [x] MOCA Command/Trigger Lookup
- [x] Open MOCA Trace Outline


### Trace Outliner

#### Semantic Highlighting
- Execution Status
- Execution Time
- MOCA Commands
- MOCA Triggers
- Returned Rows
- Command Statement Status
- Component Type
- Thread-Session
- Initiated From Compiled Code

#### Definition Lookup
- .log Definition

#### Hover
- Stack Level
- Execution Status
- Execution Time
- Returned Rows
- Component Type
- Component Level
- Stack Arguments
- Instruction


## Configuration Options

- Enable/Disable MOCA Diagnostics
- Enable/Disable MOCA Warning Diagnostics
- Enable/Disable SQL Diagnostics
- Enable/Disable SQL Warning Diagnostics
- Enable/Disable Groovy Diagnostics
- Enable/Disable Groovy Warning Diagnostics
- Enable/Disable SQL Formatting
- Enable/Disable Groovy Formatting
    - Not yet supported
- Enable/Disable Groovy Static Type Checking


## Clients

- [vscode-moca-client]


## Contribute

Thinking about contributing to the MOCA Language Server?! If you think something is missing or could be improved, please open issues and pull requests. If you'd like to help this project grow, we'd love to have you! 

Please refer to the [contribution guide] for specifics.


## Contact

- Danny Glass - mrglassdanny@gmail.com



[Language Server Protocol]: https://langserver.org
[vscode-moca-client]: https://github.com/mrglassdanny/vscode-moca-client
[Fix https SSLHandshakeException demo]: https://vimeo.com/500196466
[https SSLHandshakeException StackOverflow thread]: https://stackoverflow.com/questions/9619030/resolving-javax-net-ssl-sslhandshakeexception-sun-security-validator-validatore
[contribution guide]: https://github.com/mrglassdanny/moca-language-server/blob/master/CONTRIBUTE.md

