# HOTELIER: An Hotel Advisor Service

A simplified distributed platform inspired by TripAdvisor, developed as a final project for the **Laboratorio di reti** course (A.Y. 2023/24). 

The system leverages a **Client-Server architecture** allowing users to search for hotels across predefined Italian cities, submit structured quality reviews, track local ranking variations, and earn user-expertise badges based on active contributions.

---

## 🏗️ Architectural Overview & Design Choices

The platform splits tasks across two main components communicating over mixed networking protocols (`TCP/IP` and `UDP Multicast`):

### Server Architecture (`ServerMain`)
* **Non-Blocking I/O Multiplexing (Java NIO):** Built upon a `Selector` pattern to handle multiple concurrent client connections efficiently without spawning a dedicated thread per connection.
* **Master-Slave ThreadPool Model:** 
  * The **Master Thread** executes a continuous loop utilizing `selectNow()` to fetch incoming events and places them into a synchronized **SelectionKey FIFO Queue**.
  * A fixed-size **ThreadPool (Slave Threads)** continuously polls the synchronized queue, executing business logic and answering client operations independently.
* **Concurrency Control:** Thread-safe resource isolation is fully guaranteed by injecting dedicated synchronization `Monitors` (e.g., `MonitorAccounts`, `MonitorHotels`) into the constructor of slave workers.
* **Periodic Tasks & Persistence:** When `selectNow()` yields 0 events, the server checks execution timestamps to handle automated backup cycles, saving registered accounts and updated hotels to persistent **JSON storage files** via the `Gson` library.

### Client Architecture (`ClientMain`)
* **Command Line Interface (CLI):** Provides an interactive menu for operations such as registration, logging in, logging out, hotel lookups, and review submissions.
* **Publish-Subscribe Notifications:** Upon a successful TCP login, the client spawns a background thread dedicated to listening on a **UDP Multicast Group**. Whenever a local hotel ranking changes its 1st-place position, the server publishes a real-time broadcast alert to the group.

---

## 🧮 Local Ranking Algorithm

The sorting hierarchy of local hotels dynamically incorporates **Review Quality** (synthetic score out of 5 stars) and **Review Quantity** (total contribution thresholds). 

The score calculation employs custom step increments (**Valore Scaglione**) combined with a constant scaling multiplier:

\[Punteggio = (PunteggioSintetico \times 2) + ValoreScaglione(N_{recensioni}) + \frac{N_{recensioni}}{N_{recensioni} + 1}\]

### Quantity Step Distribution:
* **1 to 99 reviews:** +1 Point
* **100 to 499 reviews:** +2 Points
* **500 to 1499 reviews:** +3 Points
* **≥ 1500 reviews:** +4 Points

---

## ⚙️ Compilation and Execution Guide

### External Dependencies
* **Google Gson** (`gson-2.8.2.jar`) for JSON parsing and object serialization.

### Compilation from Terminal
Ensure your external `.jar` dependencies are placed inside a `lib/` directory or the current root, then execute:

```bash
# Compile Server
javac -cp .:gson-2.8.2.jar ServerMain.java

# Compile Client
javac -cp .:gson-2.8.2.jar ClientMain.java
```

### Running the Applications
Both applications feature **optional configuration arguments**. If arguments are omitted, the software gracefully loads predefined fallback parameters.

```bash
# Run Server
# Arguments: [PORT, PORT_UDP, IP_GROUP, PORT_GROUP, INTERVALLO_RANK_STORE, INTERVALLO_SLEEP_SELECT]
java -cp .:gson-2.8.2.jar ServerMain [argS1 argS2 argS3 argS4 argS5 argS6]

# Run Client
# Arguments: [IP, PORT, PORT_UDP, IP_GROUP]
java -cp .:gson-2.8.2.jar ClientMain [argC1 argC2 argC3 argC4]
```

### Executable JAR Building & Execution
To export individual self-contained deployable packages:

```bash
# Build JARs
jar cvf ServerMain.jar *.class lib/gson-2.8.2.jar
jar cvf ClientMain.jar *.class lib/gson-2.8.2.jar

# Run Server JAR
java -cp ServerMain.jar:lib/gson-2.8.2.jar ServerMain [args...]

# Run Client JAR
java -cp ClientMain.jar:lib/gson-2.8.2.jar ClientMain [args...]
```



