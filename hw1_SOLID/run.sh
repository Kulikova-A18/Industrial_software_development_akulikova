#!/bin/bash

# set -x
# set -e

# ==================================================================
# Main script that orchestrates the build and execution process
# ==================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

echo "==================================================="
echo "    Zoo Management System Launcher"
echo "==================================================="
echo "Script directory: $SCRIPT_DIR"
echo "Working directory: $(pwd)"
echo ""

# ==================================================================
# Load and execute all subscripts in order
# ==================================================================

echo "[Step 1] Clean previous build "
source "$SCRIPT_DIR/cleanup.sh"

echo "[Step 2] Check dependencies "
source "$SCRIPT_DIR/check_dependencies.sh"

echo "[Step 3] Compile program "
source "$SCRIPT_DIR/compile.sh"

echo "[Step 4] Run program "
source "$SCRIPT_DIR/run_program.sh"

echo "[Step 5] Summary "
source "$SCRIPT_DIR/summary.sh"

exit 0