# ScanSafe — Research Notes

## Research Question

Can a fully on-device, classical computer vision pipeline provide meaningful QR phishing 
protection in low-connectivity and privacy-sensitive environments — and what are the 
tradeoffs in false positive and false negative rates?

## Why Classical CV (No ML)?

1. **Explainability** — every decision can be traced to a specific rule. No black box.
2. **No cloud dependency** — works fully offline. Critical for low-connectivity environments.
3. **No pretrained model** — nothing to retrain, nothing to poison, nothing to audit externally.
4. **Performance** — classical CV runs efficiently on mid-range Android hardware in real time.
5. **Privacy** — no data ever leaves the device.

## OpenCV Pipeline — Section References (Dr. Iyer's Guide)

| Step | Guide Section | Purpose |
|------|--------------|---------|
| Grayscale | 5.1 Preprocessing | Reduce computation, standardize input |
| Gaussian Blur | 5.1 Preprocessing | Remove noise before edge detection |
| Canny Edge Detection | 5.2 Feature Extraction | Find boundaries of QR finder squares |
| FindContours | 5.2 Feature Extraction | Isolate geometric shapes in frame |
| Frame processing | 5.3 Motion Analysis | Real-time per-frame processing loop |

## Evaluation Test Cases

### Safe QR Codes (10)
- google.com
- grambling.edu
- github.com/pat-selby
- linkedin.com
- youtube.com
- amazon.com
- wikipedia.org
- cdc.gov
- nytimes.com
- apple.com

### Phishing-Pattern QR Codes (10)
- http://192.168.1.1/login
- http://paypa1.com/secure
- http://amaz0n-deals.net/claim
- http://grambling-edu.info/financial-aid
- http://bit.ly/freescholarship2026 (IP redirect)
- http://secure-login.grambling.account-verify.com
- http://192.168.0.105:8080/update
- http://microsofft.com/signin
- http://trusted-ci-apply.net/form
- http://scholarship-apply.grambling.freehosting.net

## Expected Outcomes

- High true positive rate on obvious phishing patterns (IP, brand misspelling)
- Some false positives on legitimate URL shorteners (known limitation)
- Latency target: < 500ms from scan to verdict
- Battery impact: minimal (classical CV is lightweight)

## Limitations to Discuss in Presentation

1. Heuristic rules can be evaded by sophisticated attackers using clean-looking domains
2. URL shorteners (bit.ly, tinyurl) hide the destination — flagged as suspicious by default
3. No certificate validation (would require network call — against on-device constraint)
4. Rules need ongoing maintenance as phishing patterns evolve
