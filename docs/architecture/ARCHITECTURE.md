# ARTHROSCAN-NER Architecture

## AI-Assisted Multimodal Musculoskeletal Osteoarthritis Risk Screening and Clinical Research Platform
**Smart India Hackathon 2026 | Problem Statement: SIH26004 | Team: GOD'S PLAN**

### 1. Architectural Philosophy
The system provides a unified hardware-independent multimodal pipeline. The identical processing pipeline handles:
- **Synthetic Demonstration Data**
- **CSV Replay**
- **Hardware-in-the-loop (HIL)**
- **Real Physical Sensor Streams**

### 2. Core Ingestion Pipeline
```
[Sensor Source (Simulator/BLE/USB)]
       ↓
[Universal Data Contract (Schema v1.0)]
       ↓
[Quality Gate (NaN, Clipping, Dropped Packets)]
       ↓
[Signal Preprocessing (Butterworth, Rectification, S11 extraction)]
       ↓
[Feature Store (Windowed provable features)]
       ↓
[Modality Models (IMU, VAG, sEMG, RF, Context)]
       ↓
[Multimodal Fusion Engine (Reliability-weighted)]
       ↓
[Uncertainty & Explainability Engine]
       ↓
[Clinical Intelligence & Referral Recommendation]
```

### 3. Modalities
1. **IMU**: Tibial acceleration and angular rate during controlled flexion/extension or step.
2. **VAG (Vibroarthrography)**: Knee joint acoustic emission and acoustic crepitus patterns.
3. **sEMG**: Quadriceps / Hamstring activation and co-contraction index.
4. **RF**: Exploratory research modality investigating electromagnetic loading perturbation at 2.45 GHz.
5. **Context**: WOMAC symptom questionnaire and occupational exposure.

### 4. Clinical Safety
**SCREENING & REFERRAL-SUPPORT ONLY**. This system does not diagnose osteoarthritis and does not replace X-ray, MRI, or clinician examination.
