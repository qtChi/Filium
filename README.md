<div align="center">

# Filium

**A desktop network simulator built with Java 21 and JavaFX**

*Design topologies. Configure devices. Watch packets travel.*

---

[![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat&logo=apache-maven)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-671_passing-27AE60?style=flat)]()
[![Coverage](https://img.shields.io/badge/Coverage-100%25_non--UI-27AE60?style=flat)]()
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat)](LICENSE)

</div>

---

Filium is a local-first desktop application for building and simulating computer networks. Place devices on a canvas, connect them with cables, configure IP addresses and protocols, then start the simulation and watch packets flow through your network in real time.

It is a spiritual successor to [Filius](https://www.lernsoftware-filius.de/), rebuilt from scratch with a modern dark UI, deeper protocol support, and a test-first codebase.

> *Filium — Latin for "wire" or "thread"*

---

## Screenshots

```
┌─────────────────────────────────────────────────────────────────────┐
│  New   Open   Save  │  ▶ Start   ⏸ Stop   ⟳ Reset  │  ✦ Cable     │
├──────────┬──────────────────────────────────────┬───────────────────┤
│          │                                      │  Properties       │
│ DEVICES  │                                      │                   │
│          │   ┌──────────┐      ┌──────────┐     │  Name   PC-1      │
│  [ PC ]  │   │    PC    │──────│  Router  │     │  IP     10.0.0.1  │
│          │   └──────────┘      └──────────┘     │  Mask   255.0.0.0 │
│ [Router] │                          │           │  GW     10.0.0.254│
│          │                     ┌────┴─────┐     │  MAC    AA:BB:... │
│ [Switch] │                     │  Switch  │     │                   │
│          │                     └──────────┘     │                   │
│  [DNS]   │                                      │                   │
│          │                                      │                   │
│  [DHCP]  │                                      │                   │
│          │                                      │                   │
│   [FW]   │                                      │                   │
├──────────┴──────────────────────────────────────┴───────────────────┤
│ [RECV] 10:42:01.334  PC-1 -> Router  |  ICMP echo request received  │
│ [SENT] 10:42:01.389  Router -> PC-1  |  ICMP echo reply             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Features

**Canvas**
- Drag devices from the sidebar palette onto the canvas
- Drag placed devices freely to rearrange your topology
- Click **✦ Cable** then click two devices to connect them
- Single-click to select — double-click to open the config dialog
- Select a device to edit its name, IP, mask, and gateway inline in the Properties panel

**Simulation**
- Start / Stop / Reset controls with a configurable speed multiplier (0.25× – 8×)
- Full ICMP, ARP, DNS, DHCP, and HTTP protocol simulation
- Every packet event logged in the real-time simulation log panel

**IO**
- Save and load topologies as human-readable JSON
- Canvas positions, device configuration, and cable connections are all preserved

---

## Devices

| Device | Description |
|---|---|
| **PC** | End-user machine. Sends and receives packets. |
| **Router** | Forwards packets between networks. Supports up to 8 interfaces and a static routing table. |
| **Switch** | Layer 2 forwarding. Learns MAC addresses dynamically. Supports up to 16 ports. |
| **DNS Server** | Resolves hostnames to IP addresses from a configurable A-record table. |
| **DHCP Server** | Assigns IPs dynamically from a configurable address pool. |
| **Firewall** | Stateless packet filtering using ordered allow/deny rules with wildcard support. |

---

## Protocols

| Protocol | Packets | Behaviour |
|---|---|---|
| **ICMP** | `ECHO_REQUEST` / `ECHO_REPLY` / `UNREACHABLE` | Ping between any two devices |
| **ARP** | `REQUEST` / `REPLY` | Resolves IP → MAC, populates shared ARP table |
| **DNS** | `QUERY` / `RESPONSE` | Hostname lookup against the DNS server's A records |
| **DHCP** | `DISCOVER` → `OFFER` → `REQUEST` → `ACK` | Full four-message IP assignment flow |
| **HTTP** | `REQUEST` / `RESPONSE` | Simulated request/response for end-to-end path testing |

---

## Requirements

| | |
|---|---|
| **Java** | 21 LTS |
| **Maven** | 3.9+ |
| **OS** | macOS · Linux · Windows |

No additional install needed. JavaFX is pulled in automatically by Maven.

---

## Getting Started

```bash
# Clone
git clone https://github.com/qtChi/filium.git
cd filium

# Run
mvn javafx:run
```

### Build a fat JAR

```bash
mvn package
java -jar target/filium-1.0.0-SNAPSHOT.jar
```

---

## Development

### Project structure

```
filium/
├── pom.xml
└── src/
    ├── main/java/com/filium/
    │   ├── Main.java
    │   ├── app/                    # FiliumApp — JavaFX entry point and wiring
    │   ├── model/
    │   │   ├── devices/            # Device, PC, Router, Switch, DNSServer, DHCPServer, Firewall
    │   │   └── network/            # IPAddress, NetworkInterface, Cable, NetworkTopology
    │   ├── packet/                 # Packet, PacketHeader, PacketType
    │   ├── simulation/
    │   │   ├── protocols/          # Protocol interface + ICMP/ARP/DNS/DHCP/HTTP handlers
    │   │   └── ...                 # SimulationEngine, Clock, Queue, RoutingTable, ARPTable
    │   ├── ui/
    │   │   ├── canvas/             # NetworkCanvas, CanvasController, DeviceNode, CableEdge
    │   │   ├── panels/             # Toolbar, Sidebar, Properties, SimulationLog
    │   │   └── dialogs/            # DeviceConfigDialog, AboutDialog
    │   └── io/                     # TopologySerializer, TopologyDeserializer
    └── test/java/com/filium/       # Mirrors main/ — TestXxx.java per class
```

### Dependency direction

```
ui  →  simulation  →  model  →  packet
                  ↘
                  io
```

The UI only reads simulation events — it never writes to the model directly. Protocol handlers are stateless; the engine owns all mutable state.

### Run the tests

```bash
# Run tests + generate JaCoCo HTML report
mvn verify

# Run tests + enforce 100% line/branch coverage on all non-UI classes
mvn verify -Pcoverage-check

# Open the coverage report
open target/site/jacoco/index.html
```

> Coverage enforcement excludes `com/filium/ui/**`, `FiliumApp`, and `Main`.

### Headless CI

If running in a headless environment (no display), add these flags to the Surefire config in `pom.xml`:

```xml
<argLine>
  -Djava.awt.headless=true
  -Dtestfx.headless=true
  -Dglass.platform=Monocle
  -Dmonocle.platform=Headless
</argLine>
```

Or run with `DISPLAY=:99 mvn verify` if Xvfb is available.

---

## Architecture notes

**`SimulationEngine`** drives the main loop. On each `tick()`, it drains the `PacketQueue`, dispatches every packet to the matching `Protocol` handler via `ProtocolRegistry`, and fires the resulting `SimulationEvent`s to all registered listeners.

**`SimulationClock`** is decoupled from wall time. The JavaFX `AnimationTimer` in `FiliumApp` calls `engine.tick()` at a rate scaled by `clock.getSpeed()`, so simulation speed can be adjusted without touching the engine or handlers.

**`ProtocolRegistry`** is an ordered list of `Protocol` handlers. Adding new protocols in future versions requires no changes to the engine — register a new handler and it participates immediately.

**`TopologySerializer/Deserializer`** use Jackson to write and read a simple JSON schema. Devices are keyed by UUID so cables can safely reference their endpoints across save/load cycles.

---

## Roadmap

**v1.0 — current**
- [x] Full device palette (PC, Router, Switch, DNS, DHCP, Firewall)
- [x] ICMP, ARP, DNS, DHCP, HTTP simulation
- [x] Firewall rule evaluation
- [x] Real-time simulation event log
- [x] Save / load as JSON
- [x] Dark theme

**v2.0 — planned**
- [ ] Packet path animation on the canvas
- [ ] Link state simulation (UP / DOWN per cable)
- [ ] OSPF / RIP dynamic routing
- [ ] Wireless access point device
- [ ] IPv6 addressing
- [ ] Guided lab / scenario challenge mode

---

## Contributing

Filium is test-first. Every non-UI class has a corresponding `TestXxx.java` with 100% line and branch coverage enforced by JaCoCo.

Before opening a pull request:

1. Write or update `TestXxx.java` for your changes
2. Run `mvn verify -Pcoverage-check` — the build must pass with full coverage
3. Follow the test naming convention: `methodName_partitionDescription_expectedBehaviour`
4. Keep the dependency arrows clean — `ui` must not import from `io`, model must not import from simulation

---

## License

MIT — see [LICENSE](LICENSE) for details.

---

<div align="center">

Built with Java 21 · JavaFX 21 · Maven · JUnit 5 · JaCoCo

*Inspired by Filius · Filium — Latin for "wire"*

</div>
