#!/usr/bin/env zsh
# ─────────────────────────────────────────────────────────────────────────────
# Filium — Project Setup Script
# Run from the PARENT of your filium folder:
#   chmod +x setup_filium.sh && ./setup_filium.sh
# Safe to re-run — existing files are NEVER overwritten.
# ─────────────────────────────────────────────────────────────────────────────

set -e

echo "🔧 Setting up Filium project..."

# ─────────────────────────────────────────────────────────────────────────────
# 1. DEPENDENCY CHECK & INSTALL
# ─────────────────────────────────────────────────────────────────────────────

echo "\n📦 Checking dependencies...\n"

if ! command -v brew &>/dev/null; then
  echo "⚠️  Homebrew not found. Installing..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

if ! java -version 2>&1 | grep -q "21"; then
  echo "☕ Installing Java 21 (Temurin)..."
  brew install --cask temurin@21
else
  echo "✅ Java 21 found"
fi

if ! command -v mvn &>/dev/null; then
  echo "📦 Installing Maven..."
  brew install maven
else
  echo "✅ Maven found: $(mvn -version 2>&1 | head -1)"
fi

# ─────────────────────────────────────────────────────────────────────────────
# 2. CREATE DIRECTORY STRUCTURE
# ─────────────────────────────────────────────────────────────────────────────

echo "\n📁 Creating project structure...\n"

ROOT="filium"

dirs=(
  "$ROOT/src/main/java/com/filium/app"
  "$ROOT/src/main/java/com/filium/ui/canvas"
  "$ROOT/src/main/java/com/filium/ui/panels"
  "$ROOT/src/main/java/com/filium/ui/dialogs"
  "$ROOT/src/main/java/com/filium/model/devices"
  "$ROOT/src/main/java/com/filium/model/network"
  "$ROOT/src/main/java/com/filium/simulation/protocols"
  "$ROOT/src/main/java/com/filium/packet"
  "$ROOT/src/main/java/com/filium/io"
  "$ROOT/src/main/resources/css"
  "$ROOT/src/test/java/com/filium/app"
  "$ROOT/src/test/java/com/filium/ui/canvas"
  "$ROOT/src/test/java/com/filium/ui/panels"
  "$ROOT/src/test/java/com/filium/ui/dialogs"
  "$ROOT/src/test/java/com/filium/model/devices"
  "$ROOT/src/test/java/com/filium/model/network"
  "$ROOT/src/test/java/com/filium/simulation/protocols"
  "$ROOT/src/test/java/com/filium/packet"
  "$ROOT/src/test/java/com/filium/io"
)

for d in $dirs; do
  mkdir -p "$d"
done

echo "✅ Directories created"

# ─────────────────────────────────────────────────────────────────────────────
# 3. pom.xml — only written if it doesn't already exist
# ─────────────────────────────────────────────────────────────────────────────

if [ ! -f "$ROOT/pom.xml" ]; then
cat > "$ROOT/pom.xml" << 'POMEOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.filium</groupId>
  <artifactId>filium</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>jar</packaging>
  <name>Filium</name>
  <description>Modern network simulation tool</description>

  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <javafx.version>21.0.2</javafx.version>
    <junit.version>5.10.2</junit.version>
    <mockito.version>5.10.0</mockito.version>
    <jackson.version>2.16.1</jackson.version>
    <testfx.version>4.0.18</testfx.version>
    <jacoco.version>0.8.11</jacoco.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.openjfx</groupId>
      <artifactId>javafx-controls</artifactId>
      <version>${javafx.version}</version>
    </dependency>
    <dependency>
      <groupId>org.openjfx</groupId>
      <artifactId>javafx-fxml</artifactId>
      <version>${javafx.version}</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>${jackson.version}</version>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <version>${junit.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
      <version>${junit.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-params</artifactId>
      <version>${junit.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>${mockito.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-junit-jupiter</artifactId>
      <version>${mockito.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testfx</groupId>
      <artifactId>testfx-core</artifactId>
      <version>${testfx.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testfx</groupId>
      <artifactId>testfx-junit5</artifactId>
      <version>${testfx.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.12.1</version>
        <configuration>
          <source>21</source>
          <target>21</target>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
        <configuration>
          <includes>
            <include>**/Test*.java</include>
          </includes>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>${jacoco.version}</version>
        <executions>
          <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
          </execution>
          <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-maven-plugin</artifactId>
        <version>0.0.8</version>
        <configuration>
          <mainClass>com.filium.Main</mainClass>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.1</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <mainClass>com.filium.Main</mainClass>
                </transformer>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

  <profiles>
    <profile>
      <id>coverage-check</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>${jacoco.version}</version>
            <executions>
              <execution>
                <id>check</id>
                <phase>verify</phase>
                <goals><goal>check</goal></goals>
                <configuration>
                  <rules>
                    <rule>
                      <element>BUNDLE</element>
                      <excludes>
                        <exclude>com/filium/ui/**</exclude>
                        <exclude>com/filium/app/FiliumApp</exclude>
                        <exclude>com/filium/Main</exclude>
                      </excludes>
                      <limits>
                        <limit>
                          <counter>LINE</counter>
                          <value>COVEREDRATIO</value>
                          <minimum>1.00</minimum>
                        </limit>
                        <limit>
                          <counter>BRANCH</counter>
                          <value>COVEREDRATIO</value>
                          <minimum>1.00</minimum>
                        </limit>
                        <limit>
                          <counter>METHOD</counter>
                          <value>COVEREDRATIO</value>
                          <minimum>1.00</minimum>
                        </limit>
                      </limits>
                    </rule>
                  </rules>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>

</project>
POMEOF
  echo "✅ pom.xml created"
else
  echo "⏭️  Skipping pom.xml (already exists)"
fi

# ─────────────────────────────────────────────────────────────────────────────
# 4. HELPER — skips file if it already exists
# ─────────────────────────────────────────────────────────────────────────────

write_java() {
  local filepath=$1
  local pkg=$2
  local kind=$3
  local name=$4
  local clause=${5:-""}

  if [ -f "$filepath" ]; then
    echo "  ⏭️  Skipping (exists): $filepath"
    return
  fi

  cat > "$filepath" << JAVAEOF
package ${pkg};

// TODO: Implement ${name}
public ${kind} ${name}${clause} {

}
JAVAEOF
}

# ─────────────────────────────────────────────────────────────────────────────
# 5. MAIN SOURCE FILES
# ─────────────────────────────────────────────────────────────────────────────

BASE="$ROOT/src/main/java/com/filium"

write_java "$BASE/Main.java"                                         "com.filium"                      "class"          "Main"
write_java "$BASE/app/FiliumApp.java"                                "com.filium.app"                  "class"          "FiliumApp"
write_java "$BASE/ui/canvas/NetworkCanvas.java"                      "com.filium.ui.canvas"            "class"          "NetworkCanvas"
write_java "$BASE/ui/canvas/CanvasController.java"                   "com.filium.ui.canvas"            "class"          "CanvasController"
write_java "$BASE/ui/canvas/DeviceNode.java"                         "com.filium.ui.canvas"            "class"          "DeviceNode"
write_java "$BASE/ui/canvas/CableEdge.java"                          "com.filium.ui.canvas"            "class"          "CableEdge"
write_java "$BASE/ui/panels/SidebarPanel.java"                       "com.filium.ui.panels"            "class"          "SidebarPanel"
write_java "$BASE/ui/panels/PropertiesPanel.java"                    "com.filium.ui.panels"            "class"          "PropertiesPanel"
write_java "$BASE/ui/panels/ToolbarPanel.java"                       "com.filium.ui.panels"            "class"          "ToolbarPanel"
write_java "$BASE/ui/panels/SimulationLogPanel.java"                 "com.filium.ui.panels"            "class"          "SimulationLogPanel"
write_java "$BASE/ui/dialogs/DeviceConfigDialog.java"                "com.filium.ui.dialogs"           "class"          "DeviceConfigDialog"
write_java "$BASE/ui/dialogs/AboutDialog.java"                       "com.filium.ui.dialogs"           "class"          "AboutDialog"
write_java "$BASE/model/devices/DeviceType.java"                     "com.filium.model.devices"        "enum"           "DeviceType"
write_java "$BASE/model/devices/Device.java"                         "com.filium.model.devices"        "abstract class" "Device"
write_java "$BASE/model/devices/PC.java"                             "com.filium.model.devices"        "class"          "PC"           " extends Device"
write_java "$BASE/model/devices/Router.java"                         "com.filium.model.devices"        "class"          "Router"       " extends Device"
write_java "$BASE/model/devices/Switch.java"                         "com.filium.model.devices"        "class"          "Switch"       " extends Device"
write_java "$BASE/model/devices/DNSServer.java"                      "com.filium.model.devices"        "class"          "DNSServer"    " extends Device"
write_java "$BASE/model/devices/DHCPServer.java"                     "com.filium.model.devices"        "class"          "DHCPServer"   " extends Device"
write_java "$BASE/model/devices/Firewall.java"                       "com.filium.model.devices"        "class"          "Firewall"     " extends Device"
write_java "$BASE/model/network/IPAddress.java"                      "com.filium.model.network"        "class"          "IPAddress"
write_java "$BASE/model/network/NetworkInterface.java"               "com.filium.model.network"        "class"          "NetworkInterface"
write_java "$BASE/model/network/Cable.java"                          "com.filium.model.network"        "class"          "Cable"
write_java "$BASE/model/network/NetworkTopology.java"                "com.filium.model.network"        "class"          "NetworkTopology"
write_java "$BASE/packet/PacketType.java"                            "com.filium.packet"               "enum"           "PacketType"
write_java "$BASE/packet/PacketHeader.java"                          "com.filium.packet"               "class"          "PacketHeader"
write_java "$BASE/packet/Packet.java"                                "com.filium.packet"               "class"          "Packet"
write_java "$BASE/simulation/SimulationEventType.java"               "com.filium.simulation"           "enum"           "SimulationEventType"
write_java "$BASE/simulation/SimulationEvent.java"                   "com.filium.simulation"           "class"          "SimulationEvent"
write_java "$BASE/simulation/SimulationClock.java"                   "com.filium.simulation"           "class"          "SimulationClock"
write_java "$BASE/simulation/PacketQueue.java"                       "com.filium.simulation"           "class"          "PacketQueue"
write_java "$BASE/simulation/RoutingTable.java"                      "com.filium.simulation"           "class"          "RoutingTable"
write_java "$BASE/simulation/ARPTable.java"                          "com.filium.simulation"           "class"          "ARPTable"
write_java "$BASE/simulation/SimulationEngine.java"                  "com.filium.simulation"           "class"          "SimulationEngine"
write_java "$BASE/simulation/protocols/Protocol.java"                "com.filium.simulation.protocols" "interface"      "Protocol"
write_java "$BASE/simulation/protocols/ProtocolRegistry.java"        "com.filium.simulation.protocols" "class"          "ProtocolRegistry"
write_java "$BASE/simulation/protocols/ICMPHandler.java"             "com.filium.simulation.protocols" "class"          "ICMPHandler"  " implements Protocol"
write_java "$BASE/simulation/protocols/ARPHandler.java"              "com.filium.simulation.protocols" "class"          "ARPHandler"   " implements Protocol"
write_java "$BASE/simulation/protocols/DNSHandler.java"              "com.filium.simulation.protocols" "class"          "DNSHandler"   " implements Protocol"
write_java "$BASE/simulation/protocols/DHCPHandler.java"             "com.filium.simulation.protocols" "class"          "DHCPHandler"  " implements Protocol"
write_java "$BASE/simulation/protocols/HTTPHandler.java"             "com.filium.simulation.protocols" "class"          "HTTPHandler"  " implements Protocol"
write_java "$BASE/io/TopologySerializer.java"                        "com.filium.io"                   "class"          "TopologySerializer"
write_java "$BASE/io/TopologyDeserializer.java"                      "com.filium.io"                   "class"          "TopologyDeserializer"

echo "✅ Main source files created"

# ─────────────────────────────────────────────────────────────────────────────
# 6. TEST FILES
# ─────────────────────────────────────────────────────────────────────────────

TBASE="$ROOT/src/test/java/com/filium"

write_java "$TBASE/app/TestFiliumApp.java"                           "com.filium.app"                  "class"          "TestFiliumApp"
write_java "$TBASE/ui/canvas/TestNetworkCanvas.java"                 "com.filium.ui.canvas"            "class"          "TestNetworkCanvas"
write_java "$TBASE/ui/canvas/TestCanvasController.java"              "com.filium.ui.canvas"            "class"          "TestCanvasController"
write_java "$TBASE/ui/canvas/TestDeviceNode.java"                    "com.filium.ui.canvas"            "class"          "TestDeviceNode"
write_java "$TBASE/ui/canvas/TestCableEdge.java"                     "com.filium.ui.canvas"            "class"          "TestCableEdge"
write_java "$TBASE/ui/panels/TestSidebarPanel.java"                  "com.filium.ui.panels"            "class"          "TestSidebarPanel"
write_java "$TBASE/ui/panels/TestPropertiesPanel.java"               "com.filium.ui.panels"            "class"          "TestPropertiesPanel"
write_java "$TBASE/ui/panels/TestToolbarPanel.java"                  "com.filium.ui.panels"            "class"          "TestToolbarPanel"
write_java "$TBASE/ui/panels/TestSimulationLogPanel.java"            "com.filium.ui.panels"            "class"          "TestSimulationLogPanel"
write_java "$TBASE/ui/dialogs/TestDeviceConfigDialog.java"           "com.filium.ui.dialogs"           "class"          "TestDeviceConfigDialog"
write_java "$TBASE/ui/dialogs/TestAboutDialog.java"                  "com.filium.ui.dialogs"           "class"          "TestAboutDialog"
write_java "$TBASE/model/devices/TestDeviceType.java"                "com.filium.model.devices"        "class"          "TestDeviceType"
write_java "$TBASE/model/devices/TestDevice.java"                    "com.filium.model.devices"        "class"          "TestDevice"
write_java "$TBASE/model/devices/TestPC.java"                        "com.filium.model.devices"        "class"          "TestPC"
write_java "$TBASE/model/devices/TestRouter.java"                    "com.filium.model.devices"        "class"          "TestRouter"
write_java "$TBASE/model/devices/TestSwitch.java"                    "com.filium.model.devices"        "class"          "TestSwitch"
write_java "$TBASE/model/devices/TestDNSServer.java"                 "com.filium.model.devices"        "class"          "TestDNSServer"
write_java "$TBASE/model/devices/TestDHCPServer.java"                "com.filium.model.devices"        "class"          "TestDHCPServer"
write_java "$TBASE/model/devices/TestFirewall.java"                  "com.filium.model.devices"        "class"          "TestFirewall"
write_java "$TBASE/model/network/TestIPAddress.java"                 "com.filium.model.network"        "class"          "TestIPAddress"
write_java "$TBASE/model/network/TestNetworkInterface.java"          "com.filium.model.network"        "class"          "TestNetworkInterface"
write_java "$TBASE/model/network/TestCable.java"                     "com.filium.model.network"        "class"          "TestCable"
write_java "$TBASE/model/network/TestNetworkTopology.java"           "com.filium.model.network"        "class"          "TestNetworkTopology"
write_java "$TBASE/packet/TestPacketType.java"                       "com.filium.packet"               "class"          "TestPacketType"
write_java "$TBASE/packet/TestPacketHeader.java"                     "com.filium.packet"               "class"          "TestPacketHeader"
write_java "$TBASE/packet/TestPacket.java"                           "com.filium.packet"               "class"          "TestPacket"
write_java "$TBASE/simulation/TestSimulationEventType.java"          "com.filium.simulation"           "class"          "TestSimulationEventType"
write_java "$TBASE/simulation/TestSimulationEvent.java"              "com.filium.simulation"           "class"          "TestSimulationEvent"
write_java "$TBASE/simulation/TestSimulationClock.java"              "com.filium.simulation"           "class"          "TestSimulationClock"
write_java "$TBASE/simulation/TestPacketQueue.java"                  "com.filium.simulation"           "class"          "TestPacketQueue"
write_java "$TBASE/simulation/TestRoutingTable.java"                 "com.filium.simulation"           "class"          "TestRoutingTable"
write_java "$TBASE/simulation/TestARPTable.java"                     "com.filium.simulation"           "class"          "TestARPTable"
write_java "$TBASE/simulation/TestSimulationEngine.java"             "com.filium.simulation"           "class"          "TestSimulationEngine"
write_java "$TBASE/simulation/protocols/TestProtocolRegistry.java"   "com.filium.simulation.protocols" "class"          "TestProtocolRegistry"
write_java "$TBASE/simulation/protocols/TestICMPHandler.java"        "com.filium.simulation.protocols" "class"          "TestICMPHandler"
write_java "$TBASE/simulation/protocols/TestARPHandler.java"         "com.filium.simulation.protocols" "class"          "TestARPHandler"
write_java "$TBASE/simulation/protocols/TestDNSHandler.java"         "com.filium.simulation.protocols" "class"          "TestDNSHandler"
write_java "$TBASE/simulation/protocols/TestDHCPHandler.java"        "com.filium.simulation.protocols" "class"          "TestDHCPHandler"
write_java "$TBASE/simulation/protocols/TestHTTPHandler.java"        "com.filium.simulation.protocols" "class"          "TestHTTPHandler"
write_java "$TBASE/io/TestTopologySerializer.java"                   "com.filium.io"                   "class"          "TestTopologySerializer"
write_java "$TBASE/io/TestTopologyDeserializer.java"                 "com.filium.io"                   "class"          "TestTopologyDeserializer"

echo "✅ Test files created"

# ─────────────────────────────────────────────────────────────────────────────
# 7. RESOURCES
# ─────────────────────────────────────────────────────────────────────────────

if [ ! -f "$ROOT/src/main/resources/css/dark-theme.css" ]; then
cat > "$ROOT/src/main/resources/css/dark-theme.css" << 'CSSEOF'
/* Filium Dark Theme */
.root {
  -fx-background-color: #1A1A2E;
  -fx-font-family: "SF Pro Display", "Segoe UI", Arial, sans-serif;
}
CSSEOF
  echo "✅ CSS created"
else
  echo "⏭️  Skipping CSS (exists)"
fi

if [ ! -f "$ROOT/README.md" ]; then
cat > "$ROOT/README.md" << 'READMEEOF'
# Filium
Modern network simulation tool — a spiritual successor to Filius.

## Run
```zsh
mvn javafx:run
```

## Test
```zsh
mvn verify
mvn verify -Pcoverage-check
```
READMEEOF
  echo "✅ README created"
else
  echo "⏭️  Skipping README (exists)"
fi

# ─────────────────────────────────────────────────────────────────────────────
# 8. OPEN IN VSCODE
# ─────────────────────────────────────────────────────────────────────────────

echo "\n🚀 Opening Filium in VS Code...\n"

if command -v code &>/dev/null; then
  code "$ROOT"
else
  echo "⚠️  VS Code 'code' CLI not found — open the folder manually: $(pwd)/$ROOT"
fi

echo "\n✅ Done! cd $ROOT && mvn verify to confirm everything compiles."