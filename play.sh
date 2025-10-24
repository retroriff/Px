#!/bin/bash
#
# Px Play - Wrapper for Club/px.sh
# Usage: ./play.sh '707 i: \bd dur: 1'
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"$SCRIPT_DIR/Club/px.sh" "$@"
