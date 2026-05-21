import SwiftUI
import shared

class ViewModelObserver: ObservableObject {
    let onnxClassifier = OnnxClassifier(weightsBytes: nil)
    lazy var viewModel: SharedViewModel = {
        SharedViewModel(classifier: onnxClassifier)
    }()

    @Published var uiState: UiState? = nil

    init() {
        observeState()
    }

    func loadModel(modelBytes: KotlinByteArray) {
        viewModel.loadModel(modelBytes: modelBytes, modelName: "")
    }

    func analyze(totalScore: Int32, recallScore: Int32, educationYears: Int32) {
        viewModel.analyze(
            clinicalState: ClinicalState(
                totalScore: totalScore,
                recallScore: recallScore,
                educationYears: educationYears
            )
        )
    }

    private func observeState() {
        viewModel.observeState { [weak self] state in
            DispatchQueue.main.async {
                self?.uiState = state
            }
        }
    }

    deinit {
        viewModel.stopObserving()
    }
}
