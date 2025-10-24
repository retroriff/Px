# Px OSC Sender

Send SuperCollider code to Px via OSC from the terminal.

## Quick Start

```bash
# Make sure SuperCollider is running with Px.listen;
# Then simply:
./px.py '707 i: \bd dur: 1'
```

That's it! The script sends your code to SuperCollider via OSC.

**Basic command pattern:**

```bash
./px.py 'SUPERCOLLIDER_CODE_HERE'
```

Examples:

- `./px.py '707 i: \bd dur: 1'` - Play 707 kick
- `./px.py 'Px.stop([1, 2])'` - Stop patterns
- `./px.py 'Dx.preset(\electro, 2)'` - Load preset

## Setup

Make sure SuperCollider is running and the OSC listener is active:

```supercollider
// In SuperCollider, run this first:
Px.listen;
```

## Installation

### Recommended: Bash Script (Simplest)

Install liblo-tools:

```bash
brew install liblo
```

Use the `px.sh` script (already executable).

### Alternative: Python Script

**Note**: macOS Homebrew uses externally-managed Python, so you'll need a virtual environment:

```bash
# Create a virtual environment
python3 -m venv venv
source venv/bin/activate
pip install python-osc

# Then use:
python px.py '707 i: \bd dur: 1'
```

Or use the bash script instead - it's simpler!

## Usage

### Bash Script (Recommended)

```bash
cd Club
./px.sh '707 i: \bd dur: 1'
./px.sh 'Px.stop([1, 2, 3])'
./px.sh '\707 i: \all'
```

### Python Script (if using venv)

```bash
cd Club
source venv/bin/activate  # If using virtual environment
./px.py '707 i: \bd dur: 1'

# Pipe input
echo '707 i: \bd dur: 1' | ./px.py
```

## Examples

```bash
# Four to the floor with 707
./px.sh '707 i: \bd dur: 1'

# Add snare on 2 and 4
./px.sh '707 i: \sd dur: 2 off: 1'

# Add hi-hats
./px.sh '707 i: \oh dur: 0.25 beat: 0.7 amp: 0.4'

# Stop everything
./px.sh 'Px.release(16)'

# Stop specific drum machine
./px.sh '\707 i: \all'

# Load a preset
./px.sh 'Dx.preset(\electro, 2)'

# Play a sample
./px.sh '1 play: "pop-2:0" dur: 4'
```

## Troubleshooting

**"Connection refused" error:**

- Make sure SuperCollider is running
- Make sure the OSC listener is active: `Px.listen;`

**"oscsend not found" (bash script):**

- Install liblo-tools: `brew install liblo`

**"No module named pythonosc" (python script):**

- Install python-osc: `pip3 install python-osc`

## Configuration

Default settings (edit scripts to change):

- **Host**: 127.0.0.1 (localhost)
- **Port**: 57120 (SuperCollider default)
- **OSC Address**: /px

## Notes

- Each script call sends one OSC message
- For multiple patterns, call the script multiple times
- Backslashes need escaping in bash: `\\bd` becomes `\bd` in SC
- Quotes in SC code need escaping: `'Px.stop([1, 2])'`
