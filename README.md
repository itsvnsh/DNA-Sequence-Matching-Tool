# 🧬 GenomeScan

<div align="center">

![GenomeScan Banner](https://img.shields.io/badge/GenomeScan-DNA%20Pattern%20Matching-008B8B?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iI2ZmZiIgZD0iTTEyIDJhMTAgMTAgMCAxIDAgMCAyMEExMCAxMCAwIDAgMCAxMiAyeiIvPjwvc3ZnPg==)

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

</div>

---

## 📌 Overview

**GenomeScan** is a full-stack DNA sequence pattern matching application. It implements **5 classic string-matching algorithms** with a real-time Spring Boot REST API backend and an interactive dark-themed frontend.

Users can input any DNA sequence and search for patterns using different algorithms, compare performance benchmarks, and explore bioinformatics utilities — all from a clean browser UI.

---

## ✨ Features

| Tab | Description |
|-----|-------------|
| 🔬 **Matcher** | Run any of 5 algorithms on custom genome + pattern input |
| 📊 **Benchmark** | Compare all 5 algorithms side-by-side with timing & comparison counts |
| 🚀 **DNA Utils** | Bioinformatics tools: complement, reverse complement, GC content, translation, Hamming/Edit distance, k-mer frequency, mutation detection |
| ⚡ **KMP / LPS** | Visualize the KMP Failure Function (LPS array) and Rabin-Karp rolling hash step-by-step |
| 🗺️ **Flow** | Request flow diagram and MVC architecture overview |
| 📚 **Algorithms** | Reference cards for all 5 algorithms with complexity, use cases, and explanations |

---

## 🧠 Algorithms Implemented

| Algorithm | Time (worst) | Time (best) | Space | Best For |
|-----------|-------------|-------------|-------|----------|
| **Naive** | O(n·m) | O(n) | O(1) | Small text, simple use |
| **KMP** (Knuth-Morris-Pratt) | O(n+m) | O(n+m) | O(m) | Repetitive patterns, streaming |
| **Rabin-Karp** | O(n·m) | O(n+m) | O(1) | Multi-pattern search |
| **Boyer-Moore** | O(n·m) | O(n/m) | O(σ+m) | Long patterns, large text |
| **Z-Algorithm** | O(n+m) | O(n+m) | O(n+m) | Clean code, cache-friendly |

---

## 🏗️ Project Structure

```
GenomeScan/
├── frontend/
│   ├── index.html          # Main SPA entry point
│   ├── script.js           # UI logic, fetch calls, tab routing
│   └── style.css           # Dark theme styling
│
├── src/main/java/com/example/dna/
│   ├── algorithms/
│   │   └── Algorithms.java         # All 5 string-matching algorithms (pure static)
│   ├── controller/
│   │   ├── ApiController.java      # REST endpoints (@PostMapping)
│   │   └── MatcherService.java     # Business logic, switch(algo), DNA utils
│   ├── model/
│   │   └── MatchResult.java        # Response model: positions, comparisons, timeNs
│   └── DnaApplication.java         # Spring Boot entry point
│
├── gradle/wrapper/
├── build.gradle
└── README.md
```

---

## 🔌 API Endpoints

### `POST /api/match`
Run a single algorithm on a genome + pattern.

**Request:**
```json
{
  "text": "ATGCATGCTTACG...",
  "pattern": "ATGCAT",
  "algo": "KMP"
}
```
`algo` values: `"NAIVE"`, `"KMP"`, `"RABIN-KARP"`, `"BOYER-MOORE"`, `"Z-ALGORITHM"`, `"ALL"`

**Response:**
```json
{
  "matches": 3,
  "positions": [0, 24, 48],
  "results": []
}
```

---

### `POST /api/benchmark`
Run all 5 algorithms and return comparison + timing data.

**Request:** `{ "text": "...", "pattern": "..." }`  
**Response:** `{ "results": [ ...5 algorithm results... ] }`

---

### `POST /api/dna/utils`
Bioinformatics utilities.

**Request:**
```json
{
  "op": "gc",
  "seq": "ATGCATGC",
  "seq2": "ATGCTTGC",
  "k": 3
}
```
`op` values: `gc`, `complement`, `revcomplement`, `translate`, `hamming`, `edit`, `kmer`, `mutation`

---

### `GET /api/health`
Returns `{ "status": "UP" }` — used by the frontend for the green "API Connected" indicator.

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Gradle (or use the included `gradlew` wrapper)

### Run the Backend

```bash
# Clone the repo
git clone https://github.com/your-username/GenomeScan.git
cd GenomeScan

# Build and run Spring Boot
./gradlew bootRun
# Server starts at http://localhost:8080
```

### Open the Frontend

Just open `frontend/index.html` in your browser.  
The UI auto-checks `GET /api/health` and shows the green **API Connected** dot when the backend is up.

---

## 🏛️ Architecture

```
Browser (frontend/)
    │
    │  POST /api/match  →  JSON body
    ▼
ApiController.java          ← HTTP layer only, zero algo logic
    │
    ▼
MatcherService.java         ← switch(algo), DNA utility methods, input sanitization
    │
    ▼
Algorithms.java             ← Pure static functions, no Spring dependency
    │
    ▼
MatchResult.java            ← { positions, comparisons, timeNs }
    │
    │  JSON Response
    ▼
Frontend renders            ← Cards, bars, strand viewer
```

**MVC Layers:**

| Layer | File | Responsibility |
|-------|------|---------------|
| Controller | `ApiController.java` | HTTP handling, JSON in/out |
| Service | `MatcherService.java` | Business logic, algorithm dispatch |
| Algorithm | `Algorithms.java` | Core computation, independently testable |

---

## 📖 Algorithm Notes

**KMP Failure Function (LPS Array)**  
The LPS (Longest Proper Prefix = Suffix) array lets KMP skip redundant comparisons. On mismatch at index `i`, jump to `lps[i-1]` instead of restarting from zero.

**Rabin-Karp Rolling Hash**  
Polynomial hash with DNA alphabet (BASE=4):  
`H = Σ (4^i × val(c)) mod 10⁹+7`  
where A=1, T=2, C=3, G=4

---

## 📸 Screenshots

### 🔬 Matcher
![Matcher](screenshots/matcher.png)

### 📊 Benchmark
![Benchmark](screenshots/benchmark.png)

### 🚀 DNA Utils
![DNA Utils](screenshots/dna-utils.png)

### ⚡ KMP / LPS
![KMP LPS](screenshots/kmp-lps.png)

### 🗺️ Flow
![Flow](screenshots/flow.png)

### 📚 Algorithms Reference
![Algorithms](screenshots/algorithms.png)

---

## 👨‍💻 Author

[**Dhruv Sharma** ](https://github.com/Dhruv-Sharma29)

[**Vansh Chowdhury** ](https://github.com/itsvnsh)

---

## 📄 License

[MIT](LICENSE)
