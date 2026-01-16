#!/bin/bash
#
# Run sync_enriched_to_mssql in batches of 500 records
# with 20 second pauses between batches until no more records are found.
#

BATCH_SIZE=500
PAUSE_SECONDS=20
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BINARY="$SCRIPT_DIR/sync_enriched_to_mssql"

# Check if binary exists
if [ ! -x "$BINARY" ]; then
    echo "Error: Binary not found or not executable: $BINARY"
    exit 1
fi

batch_num=0

while true; do
    batch_num=$((batch_num + 1))
    echo ""
    echo "========================================"
    echo "BATCH #$batch_num - $(date '+%Y-%m-%d %H:%M:%S')"
    echo "========================================"
    
    # Run the sync and capture output
    output=$("$BINARY" -l $BATCH_SIZE 2>&1)
    echo "$output"
    
    # Check if no records were found
    if echo "$output" | grep -q "No records to sync"; then
        echo ""
        echo "========================================"
        echo "ALL DONE - No more records to sync"
        echo "Total batches processed: $batch_num"
        echo "========================================"
        break
    fi
    
    # Check for errors that should stop the loop
    if echo "$output" | grep -q "Error:"; then
        echo ""
        echo "Warning: Errors occurred in this batch"
    fi
    
    echo ""
    echo "Waiting $PAUSE_SECONDS seconds before next batch..."
    sleep $PAUSE_SECONDS
done

