#!/bin/bash
#
# Run fetch_sacado_enrichment repeatedly until no more records to enrich
# Usage: ./run_enrichment_loop.sh [batch_size] [workers]
#

BATCH_SIZE=${1:-2000}
WORKERS=${2:-5}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BINARY="$SCRIPT_DIR/fetch_sacado_enrichment"

# Check if binary exists
if [[ ! -x "$BINARY" ]]; then
    echo "Error: Binary not found or not executable: $BINARY"
    exit 1
fi

echo "========================================"
echo "Sacado Enrichment Loop"
echo "========================================"
echo "Batch size: $BATCH_SIZE"
echo "Workers: $WORKERS"
echo "Binary: $BINARY"
echo "========================================"
echo ""

ITERATION=0
TOTAL_ENRICHED=0
TOTAL_COMPLETED=0
TOTAL_ERRORS=0

while true; do
    ITERATION=$((ITERATION + 1))
    echo ""
    echo "========================================"
    echo "ITERATION $ITERATION - $(date '+%Y-%m-%d %H:%M:%S')"
    echo "========================================"
    
    # Run the binary and capture output
    OUTPUT=$("$BINARY" "$BATCH_SIZE" "$WORKERS" 2>&1)
    EXIT_CODE=$?
    
    # Print output
    echo "$OUTPUT"
    
    # Check for errors
    if [[ $EXIT_CODE -ne 0 ]]; then
        echo ""
        echo "Error: Binary exited with code $EXIT_CODE"
        break
    fi
    
    # Extract "Found X incomplete records" from output
    FOUND=$(echo "$OUTPUT" | grep -oP 'Found \K[0-9]+(?= incomplete records)')
    
    if [[ -z "$FOUND" ]]; then
        echo ""
        echo "Could not parse output. Stopping."
        break
    fi
    
    # If no records found, we're done
    if [[ "$FOUND" -eq 0 ]]; then
        echo ""
        echo "========================================"
        echo "NO MORE RECORDS TO ENRICH"
        echo "========================================"
        break
    fi
    
    # Extract stats from output
    ENRICHED=$(echo "$OUTPUT" | grep -oP 'Enriched: \K[0-9]+' | tail -1)
    COMPLETED=$(echo "$OUTPUT" | grep -oP 'Completed: \K[0-9]+' | tail -1)
    ERRORS=$(echo "$OUTPUT" | grep -oP 'Errors: \K[0-9]+' | tail -1)
    
    TOTAL_ENRICHED=$((TOTAL_ENRICHED + ${ENRICHED:-0}))
    TOTAL_COMPLETED=$((TOTAL_COMPLETED + ${COMPLETED:-0}))
    TOTAL_ERRORS=$((TOTAL_ERRORS + ${ERRORS:-0}))
    
    echo ""
    echo "Running totals: Enriched=$TOTAL_ENRICHED, Completed=$TOTAL_COMPLETED, Errors=$TOTAL_ERRORS"
    
    # Small delay between iterations
    sleep 1
done

echo ""
echo "========================================"
echo "FINAL SUMMARY"
echo "========================================"
echo "Total iterations: $ITERATION"
echo "Total enriched: $TOTAL_ENRICHED"
echo "Total completed: $TOTAL_COMPLETED"
echo "Total errors: $TOTAL_ERRORS"
echo "Finished at: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

