import SwiftUI
import shared

class ViewModelObserver: ObservableObject {
    let onnxClassifier = OnnxClassifier()
    lazy var viewModel: SharedViewModel = {
        SharedViewModel(classifier: onnxClassifier)
    }()

    @Published var uiState: UiState? = nil

    private var job: Ktor_ioCloseable? = nil

    init() {
        observeState()
    }

    func loadModel(modelBytes: KotlinByteArray) {
        viewModel.loadModel(modelBytes: modelBytes)
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
        job = viewModel.uiState.watch { [weak self] state in
            DispatchQueue.main.async {
                self?.uiState = state
            }
        }
    }

    deinit {
        job?.close()
    }
}
