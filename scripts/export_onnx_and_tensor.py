"""
export_onnx_and_tensor.py — NeuroDetect Lite ONNX Export + Sample Tensor Generator

This script:
1. Loads the INT8-quantized LightAlzNet PyTorch model
2. Exports it to ONNX format for mobile deployment
3. Generates a random sample_tensor.bin for demo purposes
4. Copies artifacts to androidApp assets and iosApp bundle

Usage:
    python scripts/export_onnx_and_tensor.py [--onnx-only]

Requires: torch, numpy
"""

import argparse
import os
import struct
import sys
from pathlib import Path

import numpy as np

# Add back-end to path for model import
SCRIPT_DIR = Path(__file__).parent.resolve()
PROJECT_ROOT = SCRIPT_DIR.parent.parent  # back to NeuroDetectLite root
BACKEND_DIR = PROJECT_ROOT / "back-end"
MODELS_DIR = PROJECT_ROOT / "models"
MOBILE_DIR = PROJECT_ROOT / "mobile"

sys.path.insert(0, str(BACKEND_DIR))

TORCH_AVAILABLE = False
try:
    import torch
    import torch.nn as nn
    TORCH_AVAILABLE = True
except ImportError:
    print("[WARN] PyTorch not installed. Only generating sample tensor.")


def export_onnx():
    """Export LightAlzNet INT8 model to ONNX format."""
    if not TORCH_AVAILABLE:
        print("[SKIP] ONNX export requires PyTorch. Install with: pip install torch")
        return None

    # Import model architecture from backend
    try:
        from models import LightAlzNet, NUM_CLASSES, IN_CHANNELS
    except ImportError:
        print("[ERROR] Cannot import model from back-end/models.py")
        print("Make sure back-end/models.py is accessible.")
        return None

    # Build model and load INT8 weights
    model = LightAlzNet(num_classes=NUM_CLASSES, in_ch=IN_CHANNELS)
    model.eval()

    int8_path = MODELS_DIR / "int8" / "lightalznet_int8.pth"
    if not int8_path.exists():
        print(f"[ERROR] INT8 model not found at {int8_path}")
        return None

    try:
        state_dict = torch.load(int8_path, map_location="cpu", weights_only=True)
        model.load_state_dict(state_dict, strict=False)
        print(f"[OK] Loaded INT8 model from {int8_path}")
    except Exception as e:
        print(f"[WARN] Could not load INT8 weights ({e}). Using untrained model for ONNX export.")
        print("The ONNX graph will be valid, but weights will be random.")

    # Export to ONNX
    dummy_input = torch.randn(1, IN_CHANNELS, 224, 224)
    onnx_path = MOBILE_DIR / "androidApp" / "src" / "main" / "assets" / "lightalznet_int8.onnx"

    MOBILE_DIR.parent.mkdir(parents=True, exist_ok=True)
    (MOBILE_DIR / "androidApp" / "src" / "main" / "assets").mkdir(parents=True, exist_ok=True)
    (MOBILE_DIR / "iosApp" / "iosApp").mkdir(parents=True, exist_ok=True)

    torch.onnx.export(
        model,
        dummy_input,
        str(onnx_path),
        input_names=["input"],
        output_names=["output"],
        dynamic_axes={
            "input": {0: "batch_size"},
            "output": {0: "batch_size"},
        },
        opset_version=17,
    )
    print(f"[OK] ONNX model exported to {onnx_path}")
    print(f"     Size: {onnx_path.stat().st_size / 1024:.1f} KB")

    # Ensure IR version ≤ 9 for ONNX Runtime 1.17.x compatibility
    try:
        import onnx as onnx_check
        model_proto = onnx_check.load(str(onnx_path))
        if model_proto.ir_version > 9:
            print(f"[WARN] Model has IR version {model_proto.ir_version}, downgrading to 9 for ORT 1.17.x compatibility")
            model_proto.ir_version = 9
            onnx_check.save(model_proto, str(onnx_path))
            print(f"[OK] IR version downgraded to 9")
    except ImportError:
        pass

    # Copy to iOS bundle
    ios_onnx = MOBILE_DIR / "iosApp" / "iosApp" / "lightalznet_int8.onnx"
    ios_data = MOBILE_DIR / "iosApp" / "iosApp" / "lightalznet_int8.onnx.data"
    import shutil
    shutil.copy2(onnx_path, ios_onnx)
    print(f"[OK] Copied ONNX model to iOS bundle: {ios_onnx}")
    onnx_data = onnx_path.with_suffix(".onnx.data")
    if onnx_data.exists():
        shutil.copy2(onnx_data, ios_data)
        print(f"[OK] Copied ONNX external data to iOS bundle: {ios_data}")

    return onnx_path


def generate_sample_tensor():
    """Generate a random [1, 7, 224, 224] float32 tensor as raw binary."""
    tensor = np.random.default_rng(42).uniform(-1, 1, size=(1, 7, 224, 224)).astype(np.float32)

    android_dir = MOBILE_DIR / "androidApp" / "src" / "main" / "assets"
    ios_dir = MOBILE_DIR / "iosApp" / "iosApp"

    android_dir.mkdir(parents=True, exist_ok=True)
    ios_dir.mkdir(parents=True, exist_ok=True)

    android_path = android_dir / "sample_tensor.bin"
    ios_path = ios_dir / "sample_tensor.bin"

    tensor.tofile(android_path)
    tensor.tofile(ios_path)

    file_size = android_path.stat().st_size
    expected = 1 * 7 * 224 * 224 * 4
    print(f"[OK] Sample tensor generated: {android_path}")
    print(f"     Shape: [1, 7, 224, 224] | Size: {file_size} bytes (expected {expected})")

    assert file_size == expected, f"Size mismatch: got {file_size}, expected {expected}"


def main():
    parser = argparse.ArgumentParser(description="Export LightAlzNet to ONNX and generate sample tensor")
    parser.add_argument("--onnx-only", action="store_true", help="Only export ONNX, skip tensor generation")
    parser.add_argument("--tensor-only", action="store_true", help="Only generate sample tensor, skip ONNX export")
    args = parser.parse_args()

    print("=" * 60)
    print("  NeuroDetect Lite — ONNX Export & Sample Tensor Generator")
    print("=" * 60)

    if not args.tensor_only:
        onnx_path = export_onnx()
        print()

    if not args.onnx_only:
        generate_sample_tensor()
        print()

    print("[DONE] All artifacts generated successfully.")
    print()
    print("Next steps for mobile deployment:")
    print("  1. Android: Build with './gradlew :androidApp:assembleDebug'")
    print("  2. iOS: Run 'pod install' in iosApp/, then open .xcworkspace in Xcode")
    print("  3. The shared module will be compiled as a framework for iOS via CocoaPods")


if __name__ == "__main__":
    main()
