#!/bin/bash

# PDF Signature Analyzer - Command-line script
#
# Analyzes PDF signature structure and validates it.
#
# Usage:
#   ./scripts/analyze-pdf-signature.sh <pdf-file>
#
# Examples:
#   ./scripts/analyze-pdf-signature.sh certificate.pdf
#   ./scripts/analyze-pdf-signature.sh certificate.pdf

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

if [ $# -eq 0 ]; then
    echo "Error: PDF file path missing"
    echo ""
    echo "Usage:"
    echo "  $0 <pdf-file>"
    echo ""
    echo "Examples:"
    echo "  $0 certificate.pdf"
    exit 1
fi

PDF_FILE="$1"
ARGS="$*"

if [ ! -f "$PDF_FILE" ]; then
    echo "Error: File not found: $PDF_FILE"
    exit 1
fi

# Ensure code is compiled
if [ ! -d "target/classes" ]; then
    echo "Compiling code..."
    mvn -q compile
fi

# Run Maven exec:java
mvn -q exec:java \
    -Dexec.mainClass="fi.oph.koski.todistus.PdfSignatureAnalyzerCLI" \
    -Dexec.args="$ARGS" \
    -Dexec.classpathScope=compile
