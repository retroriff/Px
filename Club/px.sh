#!/bin/bash
#
# Px OSC Sender (Bash version using oscsend)
# Requires: liblo-tools (install with: brew install liblo)
#
# Usage:
#   ./px.sh '707 i: \bd dur: 1'
#   ./px.sh 'Px.stop([1, 2, 3])'
#

SC_HOST="127.0.0.1"
SC_PORT="57120"
OSC_ADDRESS="/px"

if [ -z "$1" ]; then
    echo "Usage: $0 '<SuperCollider code>'"
    echo ""
    echo "Examples:"
    echo "  $0 '707 i: \\bd dur: 1'"
    echo "  $0 'Px.stop([1, 2, 3])'"
    exit 1
fi

CODE="$1"

# Check if oscsend is available
if ! command -v oscsend &> /dev/null; then
    echo "Error: oscsend not found. Install with: brew install liblo"
    exit 1
fi

# Send OSC message
oscsend "$SC_HOST" "$SC_PORT" "$OSC_ADDRESS" s "$CODE"

if [ $? -eq 0 ]; then
    echo "✓ Sent to SuperCollider: $CODE"
else
    echo "✗ Error sending OSC message"
    exit 1
fi
