# ScanSafe — Cross-Platform QR Phishing Detector

> **"Look before you tap."**

ScanSafe detects and risk-scores URLs hidden inside QR codes in real time using a classical OpenCV 4.13 computer vision pipeline and a 22-rule heuristic URL scoring engine. No cloud dependency. No pretrained ML models. Zero URL transmission.

**Platform:** iOS (Swift 5 + SwiftUI) · Android (Kotlin) · Python prototype (CLI + Flask)
**Research:** AIoT Lab, Grambling State University · Dr. Vasanth Iyer · April 2026

---

## Research Question

Can a fully on-device classical CV pipeline provide meaningful QR phishing protection in low-connectivity and privacy-sensitive environments — and what are the measurable tradeoffs in detection rates?

---

## Detection Performance

| Metric | Result |
|---|---|
| Commodity phishing TPR | 85% |
| Sophisticated phishing TPR | 30% (Phase 1 baseline) |
| False positive rate | ~8% |
| Rule 13 SafeLinks coverage | 100% |
| Rule 14 blob: scheme coverage | 100% (forced HIGH RISK) |
| Avg. URL analysis time | <5 ms (all 22 rules) |
| URLs transmitted | Zero |
| Frames stored | Zero |

---

## The 22-Rule Engine

Rules are additive and independent. Each fired rule adds its weight to a cumulative score.

| Score | Verdict | Badge |
|---|---|---|
| 0–2 | SAFE | Green |
| 3–5 | SUSPICIOUS | Yellow |
| 6+ | HIGH RISK | Red |

**Phase 1 — Structural URL Analysis (Rules 1–18)**

HTTP scheme · IP in hostname · suspicious TLD · high-risk ccTLD · excessive subdomains · long path · query overload · brand in non-apex domain · URL shortener · @ symbol · punycode/IDN homoglyph · double file extension · SafeLinks/redirect wrapper (Rule 13) · dangerous URI scheme blob:/data:/javascript: (Rule 14, +6 forced HIGH RISK) · high consonant ratio · numeric-heavy domain · phishing keywords in path · percent-encoded obfuscation

**Phase 2 — Fuzzy Matching (Rules 19–22)**

- Rule 19: LCS typosquatting — paypa1.com (83% LCS), grarnbling.edu (89% LCS)
- Rule 20: SimHash near-duplicate URL detection (Hamming distance ≤ 4 bits)
- Rule 21: Urgency language detection (urgent, verify-now, act-now, suspended)
- Rule 22: Free hosting platform abuse — wixsite, netlify, vercel, firebase, github.io

---

## OpenCV Pipeline

Every camera frame passes through these stages in fixed order:

1. ITU-R BT.601 grayscale (Ig = 0.299R + 0.587G + 0.114B)
2. 5x5 Gaussian blur (sigma=0)
3. Canny edge detection (low=50, high=150)
4. Suzuki-Abe FindContours — isolates rectangular QR candidates
5. cv2.QRCodeDetector decode — pure OpenCV, no ML weights
6. 22-rule heuristic scoring

---

## Real-World GSU Case Studies

**Rule 13 — SafeLinks Phishing Incident**
A phishing email targeting GSU students used SafeLinks wrapping to hide the malicious destination. Rule 13 now detects 100% of SafeLinks-wrapped attacks by recursively scoring the inner URL.

**Rule 14 — blob: URI in Quarantined GSU Email**
A quarantined GSU email contained `blob:https://outlook.office.com/...`. The original engine returned SAFE because outlook.office.com is legitimate. Rule 14 now treats blob:, data:, and javascript: as forced HIGH RISK (+6 pts).

**Rule 22 — OneDrive Impersonation on Wix**
`ivoryrobinson94.wixsite.com/0ne-dr1ve` impersonated OneDrive on a free Wix site, passing all 18 Phase 1 rules. Rule 22 (+3) combined with Rule 19 LCS path similarity (88%) now scores it HIGH RISK (6+).

---

## Repository Layout

```
scan-safe/
└── scansafe/
    ├── app.py                  # Flask web UI + REST API (POST /scan)
    ├── scansafe_prototype.py   # Full 22-rule engine + OpenCV QR decode (CLI)
    ├── sensor_log.py           # Device motion logger (accel + gyro → CSV, EMA)
    ├── assets/
    │   ├── screenshots/        # Live app screenshots (iOS)
    │   └── images/             # Test QR code images
    ├── data/
    │   └── phishing_corpus.txt # 28-URL evaluation corpus
    └── docs/
        └── ScanSafe_Report.pdf # Research report (April 2026)
```

---

## Quick Start

```bash
cd scansafe
pip install opencv-python flask pyopenssl cryptography
```

**Flask Web UI**

```bash
python app.py
```

Open `http://<IP>:8000` on any phone on the same WiFi. Accept the self-signed cert warning.

**CLI modes**

```bash
python scansafe_prototype.py --url "https://paypa1.com/login"
python scansafe_prototype.py --image qr.png
python scansafe_prototype.py --camera
python scansafe_prototype.py --clipboard
python scansafe_prototype.py --url "..." --radar
```

---

## Privacy Guarantees

- No camera frames stored or transmitted
- No URLs sent to any external service
- No analytics or device identifiers collected
- Processing is fully local and ephemeral
- Cloudflare Radar integration is off by default (opt-in via --radar)

---

## Known Limitations

- Sophisticated phishing using HTTPS + clean registered domains: 30% TPR ceiling
- Compromised legitimate websites hosting phishing pages score clean
- Unicode homographs not encoded as punycode bypass Rule 11
- Brand list for Rules 8/19 is finite
- SimHash session cache (Rule 20) does not persist across restarts

---

## Built By

Patrick Ennin Selby · Research Assistant, AIoT Lab · Grambling State University
Supervised by Dr. Vasanth Iyer · April 2026
