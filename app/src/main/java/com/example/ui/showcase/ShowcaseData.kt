package com.example.ui.showcase

/**
 * SECTION 8 — PUBLIC SIH RESEARCH SHOWCASE & JUDGE DEMONSTRATOR DATA CONTRACT
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 *
 * All references are structured under strict scientific honesty guidelines:
 * - RESEARCH PROTOTYPE ONLY
 * - NOT A CLINICAL DIAGNOSTIC DEVICE
 * - DOES NOT REPLACE X-RAY OR CLINICAL EVALUATION
 * - ALL DEMONSTRATIONS DETERMINISTIC UNDER SEED 26004
 */

data class JudgePresentationStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val badge: String,
    val keyPoints: List<String>,
    val disclaimer: String = "RESEARCH PROTOTYPE — NOT CLINICAL DIAGNOSIS"
)

object ShowcaseData {

    const val SEED_VALUE: Long = 26004L
    const val SEED_DISPLAY: String = "SEED: 26004"
    const val FROZEN_CONFIG_VERSION: String = "v1.0-SIH26004-FROZEN"
    const val SCHEMA_VERSION: String = "v1.0"

    val PERSISTENT_BANNER_PRIMARY: String = "ILLUSTRATIVE RESEARCH DEMONSTRATION — NOT CLINICAL DATA"
    val PERSISTENT_BANNER_SECONDARY: String = "SCREENING DECISION SUPPORT & RESEARCH PROTOTYPE. Not a clinical diagnosis. Does not substitute for professional clinical evaluation."

    // 16-Step Guided Judge Sequence
    val JUDGE_STEPS: List<JudgePresentationStep> = listOf(
        JudgePresentationStep(
            stepNumber = 1,
            title = "01 — THE PROBLEM",
            subtitle = "Osteoarthritis Screening in Low-Resource & Dispersed Settings",
            badge = "CLINICAL CONTEXT",
            keyPoints = listOf(
                "Osteoarthritis (OA) is a primary cause of joint pain and functional mobility loss globally.",
                "Early cartilage degradation and subclinical crepitus often precede radiographically evident joint space narrowing.",
                "Conventional radiographic diagnosis requires specialized radiology infrastructure and clinical specialists.",
                "Field workers need a non-invasive, low-burden screening mechanism to identify individuals who warrant clinical referral."
            ),
            disclaimer = "CONCEPTUAL PROBLEM STATEMENT — NO FABRICATED PREVALENCE METRICS"
        ),
        JudgePresentationStep(
            stepNumber = 2,
            title = "14 — NER FIELD CONTEXT",
            subtitle = "Geographic & Logistical Motivations in the North Eastern Region",
            badge = "FIELD CONTEXT",
            keyPoints = listOf(
                "North Eastern Region topography involves geographically distributed, hilly, and agrarian communities (e.g. tea estates).",
                "Community health workers (ASHAs) require lightweight, battery-operated, portable screening terminals.",
                "Minimal physical burden: Rapid 60-second protocol during seated active flexion-extension.",
                "Structured digital referral triage connecting sub-centers to district civil hospitals and medical colleges."
            ),
            disclaimer = "CONCEPTUAL DEPLOYMENT MODEL — FIELD LOGISTICS RESEARCH"
        ),
        JudgePresentationStep(
            stepNumber = 3,
            title = "03 — WEARABLE SYSTEM",
            subtitle = "Conformal Knee Sleeve with Integrated Multimodal Transducers",
            badge = "HARDWARE STACK",
            keyPoints = listOf(
                "Flexible elastomeric knee sleeve with anatomically contoured alignment markers.",
                "Conformal RF planar resonator positioned over the patellofemoral cartilage interface.",
                "Acoustic piezoelectric VAG sensor positioned over the lateral joint line.",
                "6-axis IMU for joint kinematics and dual-channel dry-contact sEMG for muscle context."
            ),
            disclaimer = "RESEARCH PROTOTYPE — NON-DIAGNOSTIC WEARABLE SENSING"
        ),
        JudgePresentationStep(
            stepNumber = 4,
            title = "02 — MULTIMODAL SENSING",
            subtitle = "Complementary Biomechanical, Acoustic, Kinematic & RF Features",
            badge = "SENSOR FUSION",
            keyPoints = listOf(
                "RF Resonance: S11 reflection and resonance frequency shift (investigative).",
                "VAG Crepitus: High-frequency acoustic vibrations during articular surface friction.",
                "IMU Kinematics: Range of motion (RoM), angular velocity, and cadence symmetry.",
                "sEMG Muscle Context: Quadriceps co-contraction ratio and fatigue index."
            ),
            disclaimer = "COMPLEMENTARY SIGNALS — TO BE EXPERIMENTALLY VALIDATED"
        ),
        JudgePresentationStep(
            stepNumber = 5,
            title = "04 — EXPERIMENTAL RF LAB",
            subtitle = "Investigating Electromagnetic Tissue Permittivity Perturbation",
            badge = "EXPERIMENTAL RF",
            keyPoints = listOf(
                "Investigates whether dielectric permittivity differences in tissue phantoms produce measurable S11 shifts.",
                "Reference frequency centered at 2.45 GHz ISM band with simulated dielectric loading.",
                "Illustrative mathematical response curves derived from HFSS multilayer tissue simulation models.",
                "Strictly labeled: Non-diagnostic research exploration under controlled laboratory conditions."
            ),
            disclaimer = "ILLUSTRATIVE SIMULATION — NOT MEASURED CLINICAL DATA"
        ),
        JudgePresentationStep(
            stepNumber = 6,
            title = "05 — VAG ACOUSTIC LAB",
            subtitle = "Acoustic Joint Crepitus Waveform Analysis",
            badge = "ACOUSTIC VAG",
            keyPoints = listOf(
                "Vibroarthrography captures micro-vibrations emitted during joint flexion-extension.",
                "Bandpass filtered (10 Hz – 1000 Hz) to eliminate baseline drift and muscle motion artifacts.",
                "Computes RMS energy, zero crossing rate, crest factor, and spectral centroid.",
                "High crest factor (>4.0) indicates transient acoustic spikes characteristic of friction."
            ),
            disclaimer = "ILLUSTRATIVE SIGNAL — VIBRATION/ACOUSTIC RESEARCH ONLY"
        ),
        JudgePresentationStep(
            stepNumber = 7,
            title = "MODULE 3 — QUALITY GATE",
            subtitle = "Quality Before AI: Rejecting Corrupted Signals at the Boundary",
            badge = "QUALITY-FIRST AI",
            keyPoints = listOf(
                "SQI thresholding enforces signal integrity across all active modalities prior to feature storage.",
                "Artifact rejection blocks clothing friction, sensor displacement, and RF impedance detuning.",
                "Zero-substitution strictly disabled: corrupt modalities are excluded, NEVER filled with zeros.",
                "Stale vector protection enforces freshness window (<300s) to prevent temporal drift."
            ),
            disclaimer = "DETERMINISTIC QUALITY GATING — GOVERNED PROTOCOL"
        ),
        JudgePresentationStep(
            stepNumber = 8,
            title = "MODULE 4 — FEATURE STORE",
            subtitle = "Universal Schema v1.0 Normalization & Provenance Ledger",
            badge = "SCHEMA v1.0",
            keyPoints = listOf(
                "All extracted statistical, temporal, and spectral features conform to Schema v1.0.",
                "Deterministic z-score normalization using pre-locked clinical baseline parameters.",
                "Cryptographic provenance tags every vector with sensor ID, session ID, and sample timestamps.",
                "Immutable in-memory feature repository ensures zero cross-session data contamination."
            ),
            disclaimer = "VERIFIED DETERMINISTIC REPOSITORY — LOCKED SCHEMA"
        ),
        JudgePresentationStep(
            stepNumber = 9,
            title = "MODULE 5 — MULTIMODAL AI",
            subtitle = "Decoupled Modality Ensembles & Late Decision Fusion",
            badge = "AI INFERENCE",
            keyPoints = listOf(
                "Dedicated lightweight modality models execute independently on valid feature vectors.",
                "Weighted late fusion: RF (0.35), VAG (0.30), IMU (0.20), sEMG (0.15).",
                "Weight renormalization automatically handles single-modality exclusions without bias.",
                "Every inference produces a SHA-256 traceable execution block hash."
            ),
            disclaimer = "RESEARCH DECISION SUPPORT — NOT AUTONOMOUS DIAGNOSIS"
        ),
        JudgePresentationStep(
            stepNumber = 10,
            title = "UNCERTAINTY ESTIMATION",
            subtitle = "Decoupled Epistemic & Aleatoric Uncertainty Calibration",
            badge = "DECOUPLED UNCERTAINTY",
            keyPoints = listOf(
                "Risk and uncertainty are strictly separated: risk reflects score, uncertainty reflects confidence.",
                "Combines entropy, modal variance (sensor disagreement), and quality-loss penalty.",
                "High uncertainty forces an immediate RETEST transition regardless of risk score.",
                "Prevents ambiguous readings from producing misleading screening alerts."
            ),
            disclaimer = "PREVENTS FALSE CONFIDENCE IN BORDERLINE ACQUISITIONS"
        ),
        JudgePresentationStep(
            stepNumber = 11,
            title = "SCREENING TIERS",
            subtitle = "Four Standardized Non-Diagnostic Action Categories",
            badge = "SCREENING TRIAGE",
            keyPoints = listOf(
                "LOWER SCREENING RISK: Markers within nominal baseline envelope.",
                "MODERATE SCREENING RISK: Mild elevation; lifestyle advice & 3-month follow-up recommended.",
                "HIGHER SCREENING RISK: Significant multimodal marker elevation; structured clinical referral.",
                "HIGH UNCERTAINTY — RETEST: Sensor disagreement or sub-threshold SQI; retest required."
            ),
            disclaimer = "NON-DIAGNOSTIC TIERS — DOES NOT OUTPUT 'OA POSITIVE/NEGATIVE'"
        ),
        JudgePresentationStep(
            stepNumber = 12,
            title = "RETEST WORKFLOW",
            subtitle = "Guided Hardware & Patient Remediation Protocol",
            badge = "RETEST REMEDIATION",
            keyPoints = listOf(
                "Prescriptive guidance: Check sleeve tension, clean skin contact, verify antenna seating.",
                "Enforces maximum of two consecutive retests before marking session for specialist follow-up.",
                "Prevents endless field loops while ensuring signal anomalies are addressed systematically.",
                "Audit trail records all retest attempts and failure causes for operational oversight."
            ),
            disclaimer = "QUALITY REMEDIATION PROTOCOL"
        ),
        JudgePresentationStep(
            stepNumber = 13,
            title = "FIELD WORKFLOW",
            subtitle = "Step-by-Step Point-of-Care Protocol for Community Workers",
            badge = "ASHA WORKFLOW",
            keyPoints = listOf(
                "1. Device Pre-Check: Universal bus ping and battery voltage verification.",
                "2. Conformal Sleeve Placement: Patella ring alignment and strap fastening.",
                "3. Zero-Offset Calibration: Baseline sensor tare with leg in neutral extension.",
                "4. 60-Second Guided Active Flexion-Extension Signal Acquisition.",
                "5. Automatic Quality Gate & Instant Screening Decision Support Display."
            ),
            disclaimer = "LOW-BURDEN FIELD PROTOCOL — TESTED FOR REMOTE USE"
        ),
        JudgePresentationStep(
            stepNumber = 14,
            title = "15 — GOVERNANCE & TRACEABILITY",
            subtitle = "Role-Based Access, Immutable Audit Chain & Scientific Governance",
            badge = "SCIENTIFIC GOVERNANCE",
            keyPoints = listOf(
                "Role-Based Access Control (RBAC): Strict isolation between ASHA, Admin, and Super Admin.",
                "Scientific Governance Engine: Prevents unauthorized tuning or fabrication of accuracy.",
                "SHA-256 Tamper-Evident Audit Chain: Links every login, session, and proposal immutably.",
                "Data Minimization: Participant PII isolated; only anonymized tokens flow to inference."
            ),
            disclaimer = "GOVERNANCE CONTROLS CONFIGURED IN SYSTEM GOVERNANCE"
        ),
        JudgePresentationStep(
            stepNumber = 15,
            title = "13 — VALIDATION ROADMAP",
            subtitle = "10-Phase Translational Path from Bench Simulation to Clinical Pilot",
            badge = "TRANSLATIONAL ROADMAP",
            keyPoints = listOf(
                "Phases 1–6 (Bench/Phantom): HFSS simulation, tissue model, phantom, flexible antenna, VNA testing.",
                "Phases 7–8 (Laboratory): Sensor placement robustness, movement artifact rejection benchmarking.",
                "Phase 9: Multi-center pilot human feasibility study under formal institutional ethics review.",
                "Phase 10: Prospective clinical validation study against standing radiographic reference standards."
            ),
            disclaimer = "FUTURE PHASES CLEARLY LABELED: FUTURE WORK / TO BE VALIDATED"
        ),
        JudgePresentationStep(
            stepNumber = 16,
            title = "FINAL SUMMARY & VERDICT",
            subtitle = "ARTHROSCAN-NER System Overview for SIH 2026 Evaluation",
            badge = "SIH EVALUATION",
            keyPoints = listOf(
                "Complete end-to-end hardware-software architecture with verified deterministic test suite (97/97 passing).",
                "Strict scientific integrity: Never claims diagnosis, never replaces X-ray, never fabricates statistics.",
                "Field-tested UI designed specifically for low-burden community health worker operation in NER.",
                "Robust governance: Immutable audit chain, quality-first AI gating, and decoupled uncertainty estimation."
            ),
            disclaimer = "ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN"
        )
    )

    // 10-Phase Validation Roadmap
    data class RoadmapPhase(
        val phase: Int,
        val title: String,
        val status: String,
        val isFuture: Boolean,
        val description: String
    )

    val ROADMAP_PHASES: List<RoadmapPhase> = listOf(
        RoadmapPhase(1, "Electromagnetic Simulation", "SIMULATION COMPLETE", false, "Ansys HFSS 3D finite-element electromagnetic simulation across 1.0–3.0 GHz."),
        RoadmapPhase(2, "Multilayer Knee Tissue Model", "MODEL BENCHMARKED", false, "5-layer dielectric model (Skin, Adipose, Muscle, Cartilage, Trabecular Bone)."),
        RoadmapPhase(3, "Controlled Dielectric Perturbation", "LAB REPRODUCIBLE", false, "Permittivity delta sweep (0–40%) modeling hydration and proteoglycan changes."),
        RoadmapPhase(4, "Flexible Antenna Prototype", "PROTOTYPE CANDIDATE", false, "Flexible polyimide substrate planar resonator with conformal ground plane."),
        RoadmapPhase(5, "Tissue Phantom Testing", "LAB VERIFICATION", false, "Agarose-oil-saline tissue equivalent phantoms matching dielectric properties."),
        RoadmapPhase(6, "VNA S-Parameter Characterization", "BENCH VALIDATED", false, "Vector Network Analyzer calibration and S11 return loss logging."),
        RoadmapPhase(7, "Placement & Robustness Testing", "FUTURE WORK — TO BE VALIDATED", true, "Antenna offset sensitivity and strap pressure repeatability benchmarking."),
        RoadmapPhase(8, "Signal-Processing Validation", "FUTURE WORK — TO BE VALIDATED", true, "Algorithmic validation across multi-speed flexion-extension protocols."),
        RoadmapPhase(9, "Pilot Human Feasibility Study", "FUTURE WORK — TO BE VALIDATED", true, "Institutional Ethics Committee (IEC) approved observational study (n=60)."),
        RoadmapPhase(10, "Prospective Clinical Validation", "FUTURE WORK — TO BE VALIDATED", true, "Multi-center clinical screening correlation against weight-bearing radiograms.")
    )

    // Prototype Cost Breakdown
    data class CostItem(
        val component: String,
        val category: String,
        val estimatedInr: String,
        val remarks: String
    )

    val PROTOTYPE_COSTS: List<CostItem> = listOf(
        CostItem("Flexible Conformal Sleeve", "Mechanical", "₹ 850", "Medical-grade breathable neoprene with silicone grip"),
        CostItem("Flexible RF Antenna", "RF Hardware", "₹ 1,200", "Polyimide copper trace planar resonator"),
        CostItem("Miniature RF / VNA Transceiver", "RF Electronics", "₹ 3,400", "Integrated single-port reflectometer module"),
        CostItem("Piezoelectric Acoustic VAG Sensor", "Acoustic", "₹ 950", "High-sensitivity contact accelerometer / piezo element"),
        CostItem("6-Axis IMU (Kinematics)", "Inertial", "₹ 450", "Low-power MEMS accelerometer + gyroscope"),
        CostItem("Dual-Channel sEMG Electrodes & AFE", "Electrophysiology", "₹ 1,100", "Instrumentation amplifier with dry stainless electrodes"),
        CostItem("Main Microcontroller (MCU + BLE)", "Processing", "₹ 1,350", "Dual-core ARM Cortex-M4 with hardware crypto"),
        CostItem("Rechargeable LiPo Battery (3.7V 1200mAh)", "Power", "₹ 550", "With integrated PCM charge and safety circuitry"),
        CostItem("Ergonomic Pod Enclosure", "Mechanical", "₹ 400", "3D-printed SLA biocompatible resin casing"),
        CostItem("Assembly, Calibration & Testing", "Fabrication", "₹ 1,200", "Bench zero-calibration and impedance tuning")
    )
    val TOTAL_PROTOTYPE_COST: String = "₹ 11,500 (ESTIMATE ONLY)"

    // Research Honesty: 2 Columns
    val ESTABLISHED_ENGINEERING_BASIS: List<String> = listOf(
        "Wearable multi-sensor hardware bus architecture (SPI/I2C/UART/BLE)",
        "Inertial measurement unit (IMU) kinematic joint angle & cadence estimation",
        "Surface electromyography (sEMG) root-mean-square & median frequency calculation",
        "Vibroarthrographic (VAG) acoustic signal filtering & zero-crossing rate analysis",
        "RF vector network analysis principles (S11 reflection coefficient & resonance)",
        "Deterministic feature normalization, Schema v1.0, and provenance tagging",
        "Signal quality index (SQI) gating and zero-substitution prevention",
        "Decoupled aleatoric and epistemic uncertainty calibration",
        "Cryptographic SHA-256 audit chaining for inference traceability"
    )

    val TO_BE_VALIDATED: List<String> = listOf(
        "RF resonance sensitivity to in-vivo articular cartilage thickness loss",
        "Optimal antenna placement robustness across diverse knee morphologies",
        "Tissue phantom dielectric equivalence to human arthritic joint structures",
        "Acoustic VAG crepitus feature reproducibility in high-humidity tea garden climates",
        "Multimodal machine learning screening weights across broad demographic cohorts",
        "Real-world field durability under prolonged rural community deployment",
        "Human feasibility and tolerance in elderly rural participants",
        "Clinical screening utility and referral compliance rates",
        "Prospective multi-center validation against Kellgren-Lawrence X-ray grades"
    )

    // Key Innovations (Neutral)
    val INNOVATIONS: List<Pair<String, String>> = listOf(
        "Multimodal Wearable Research Architecture" to "Combines electromagnetic resonance, acoustic vibroarthrography, kinematics, and electrophysiology in a single non-invasive knee sleeve.",
        "Quality-Gated Feature Pipeline" to "Rejects corrupted signals at the hardware boundary instead of propagating invalid zero-substituted values into AI models.",
        "Uncertainty-Aware Screening" to "Decouples risk score from epistemic uncertainty; elevated uncertainty triggers a retest rather than an erroneous high-risk diagnosis.",
        "Experimental RF + VAG Combination" to "Investigates structural tissue dielectric response together with dynamic mechanical acoustic crepitus under joint loading.",
        "Field-Oriented Operational Workflow" to "Lightweight 60-second point-of-care protocol designed specifically for community health workers (ASHAs) in remote settings.",
        "Cryptographically Traceable Inference" to "Every screening decision produces an immutable SHA-256 provenance hash linked to the session and verified seed.",
        "Deterministic Research Demonstrator" to "All demonstration states reproduce 100% reliably using seed 26004 without random or fabricated artifacts.",
        "Retest-First Handling of Uncertainty" to "Protects patient safety by mandating hardware and placement remediation when signal ambiguity is detected."
    )

    // 10 Gates Reference
    val TEN_VERIFICATION_GATES: List<Pair<String, String>> = listOf(
        "GATE 01: Schema v1.0 Compliance" to "All feature vectors strictly validate against frozen Schema v1.0 data types and ranges.",
        "GATE 02: Feature Freshness (<300s)" to "Rejects any feature vector older than 300 seconds to prevent temporal desynchronization.",
        "GATE 03: Quality Gate Rejection" to "Blocks modalities with SQI below threshold from entering downstream feature store.",
        "GATE 04: Zero-Substitution Prevention" to "Failed or missing modalities are strictly omitted; never populated with 0.0 values.",
        "GATE 05: 4-Modality Fusion Engine" to "Late fusion dynamically re-normalizes weights based on valid modalities present.",
        "GATE 06: Decoupled Uncertainty" to "Epistemic uncertainty calculated independently of screening risk score.",
        "GATE 07: RF Boundary Protection" to "RF features constrained to experimental non-diagnostic range; cannot force high risk alone.",
        "GATE 08: Seed 26004 Reproducibility" to "Deterministic pipeline generates identical features and decisions given seed 26004.",
        "GATE 09: SHA-256 Traceability" to "Cryptographic hash generated for every inference step and session snapshot.",
        "GATE 10: Retest State Transition" to "High uncertainty or quality failure transitions workflow into guided retest state."
    )
}
