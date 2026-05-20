import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = ViewModelObserver()

    @State private var totalScore: String = ""
    @State private var recallScore: String = ""
    @State private var educationYears: String = "16"
    @State private var modelLoaded: Bool = false

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // Input Card
                    VStack(spacing: 12) {
                        Text("MMSE Clinical Assessment")
                            .font(.title2)
                            .fontWeight(.bold)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        TextField("MMSE Total Score (0–30)", text: $totalScore)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)

                        TextField("Delayed Recall Score (0–3)", text: $recallScore)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)

                        TextField("Education Years", text: $educationYears)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                    }
                    .padding(20)
                    .background(Color(.systemBackground))
                    .cornerRadius(16)
                    .shadow(color: .black.opacity(0.05), radius: 4)

                    // Buttons
                    HStack(spacing: 12) {
                        Button(action: loadTensor) {
                            Label("Load Tensor", systemImage: "square.and.arrow.down")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                        .tint(.blue)

                        Button(action: analyze) {
                            Label("Analyze", systemImage: "brain.head.profile")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.blue)
                    }

                    if !modelLoaded {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.orange)
                            Text("Load the ONNX model before running analysis")
                                .font(.caption)
                                .foregroundColor(.orange)
                        }
                        .padding(12)
                        .frame(maxWidth: .infinity)
                        .background(Color.orange.opacity(0.1))
                        .cornerRadius(12)
                    }

                    // Results
                    if let state = viewModel.uiState {
                        resultView(for: state)
                    }
                }
                .padding(16)
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("NeuroDetect Lite")
        }
    }

    private func loadTensor() {
        if let path = Bundle.main.path(forResource: "lightalznet_int8", ofType: "onnx"),
           let data = FileManager.default.contents(atPath: path) {
            let dataKotlin = KotlinByteArray(size: Int32(data.count))
            for i in 0..<data.count {
                dataKotlin.set(index: Int32(i), value: Int8(bitPattern: data[i]))
            }
            viewModel.loadModel(modelBytes: dataKotlin)
            modelLoaded = true
        }
    }

    private func analyze() {
        guard let total = Int(totalScore),
              let recall = Int(recallScore),
              let edu = Int(educationYears) else { return }
        viewModel.analyze(
            totalScore: Int32(total),
            recallScore: Int32(recall),
            educationYears: Int32(edu)
        )
    }

    @ViewBuilder
    private func resultView(for state: UiState) -> some View {
        switch state {
        case is UiState.Loading:
            ProgressView("Analyzing...")
                .padding()

        case let standbyState as UiState.Standby:
            GateCard(gateResult: standbyState.gateResult)

        case let completeState as UiState.Complete:
            GateCard(gateResult: completeState.gateResult)
            PredictionCard(prediction: completeState.predictionResult)

        case let errorState as UiState.Error:
            HStack {
                Image(systemName: "xmark.octagon.fill")
                    .foregroundColor(.red)
                Text(errorState.message)
                    .foregroundColor(.red)
                    .font(.caption)
            }
            .padding(12)
            .frame(maxWidth: .infinity)
            .background(Color.red.opacity(0.1))
            .cornerRadius(12)

        default:
            EmptyView()
        }
    }
}

struct GateCard: View {
    let gateResult: GateResult

    var alertColor: Color {
        switch gateResult.alertLevel {
        case .none: return .green
        case .info: return .blue
        case .review: return .orange
        case .escalate: return .red
        default: return .gray
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(gateResult.alertLevel.name)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(alertColor)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(alertColor.opacity(0.15))
                    .cornerRadius(8)

                Text(gateResult.triggerMriPipeline ? "ML Pipeline: TRIGGERED" : "ML Pipeline: STANDBY")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(gateResult.triggerMriPipeline ? .orange : .green)
            }

            Text(gateResult.message)
                .font(.body)
                .foregroundColor(.primary)

            Text(gateResult.clinicalNote)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(alertColor.opacity(0.08))
        .cornerRadius(16)
    }
}

struct PredictionCard: View {
    let prediction: PredictionResult

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Model Inference Results")
                .font(.headline)

            HStack {
                VStack(alignment: .leading) {
                    Text("Diagnosis")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text(PredictionResult.companion.CLASS_NAMES[Int(prediction.predictedClass)])
                        .font(.title3)
                        .fontWeight(.bold)
                }
                Spacer()
                VStack(alignment: .trailing) {
                    Text("Confidence")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text("\(Int(prediction.confidence * 100))%")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(prediction.confidence > 0.7 ? .green : .orange)
                }
            }

            Divider()

            Text("Class Probabilities")
                .font(.caption)
                .foregroundColor(.secondary)

            ForEach(Array(PredictionResult.companion.CLASS_NAMES.enumerated()), id: \.offset) { i, name in
                let prob = prediction.probabilities[i]
                HStack {
                    Text(name)
                        .frame(width: 140, alignment: .leading)
                        .font(.subheadline)

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 4)
                                .fill(Color(.systemGray6))
                                .frame(height: 20)

                            RoundedRectangle(cornerRadius: 4)
                                .fill(barColor(for: i))
                                .frame(width: geo.size.width * CGFloat(prob), height: 20)
                        }
                    }
                    .frame(height: 20)

                    Text("\(Int(prob * 100))%")
                        .frame(width: 40, alignment: .trailing)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 4)
    }

    private func barColor(for index: Int) -> Color {
        switch index {
        case 0: return .green
        case 1: return .orange
        default: return .red
        }
    }
}
