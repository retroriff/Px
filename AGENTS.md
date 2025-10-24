# LLM Agent Task Routing

**Px** is a SuperCollider extension for live coding. LLM agents will be used for **two distinct purposes**:

1. **Development** - Coding features, fixing bugs, refactoring
2. **Music Creation** - Writing live coding patterns and performances

**IMPORTANT**: LLM agents must distinguish between these tasks and **only read the necessary documentation** for the goal at hand. Do not load all documentation - be selective.

---

## Task Detection Guide

| User Request Type | Examples                                                                              | Route To                                                 |
| ----------------- | ------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| **Playing Music** | "play a beat", "add reverb", "how do I use samples?", "create a drum pattern"         | [Music Creation Docs](#documentation-for-music-creation) |
| **Development**   | "add a feature", "fix this bug", "refactor", "how does the code work?", "implement X" | [Development Docs](#documentation-for-development)       |

---

## Documentation for Music Creation

**When the user wants to play music or understand how to use Px for live coding:**

### Primary References (read in order of relevance)

1. **`Docs/how-to-play-music/agent-instructions.md`** - Agents responses

2. **`README.md`** - Complete API reference

   - Full list of pattern methods
   - Full list of effects
   - Parameter descriptions

3. **`Docs/how-to-play-music/audio-samples.md`** - How audio sample files are organized and referenced

   - Sample location: `~/icloud/Music/Samples/Live Coding`
   - Folder structure and indexing format

4. **`Examples/*.scd`** - Working code examples for music creation
   - `1. Basic.scd` - Core pattern syntax
   - `2. Loops.scd` - Playing audio sample files
   - `3. Drum Machines.scd` - Using drum machine presets
   - `4. FX.scd` - Adding effects
   - `5. MIDI.scd` - MIDI integration
   - `6. Notes.scd` - Playing notes and melodies

### What NOT to Read for Music Creation

- ❌ `Docs/how-to-develop-features/` - Internal architecture (not needed for playing)
- ❌ `Classes/*.sc` - Source code (not needed unless debugging)

### Quick Music Creation Commands

```supercollider
// Setup
Px.loadSamples("~/icloud/Music/Samples/Live Coding");
Px.loadSynthDefs;

// Play patterns
1 i: \bd dur: 1 reverb: 0.2      // Kick drum
2 i: \sd dur: 2 off: 1           // Snare on beat 2
3 play: "pop-2:0" dur: 4         // Play sample file

// Stop
\1 i: \bd                        // Stop pattern 1
Px.stop([1, 2, 3])              // Stop multiple
Px.release(16)                   // Fade out all
```

---

## Documentation for Development

**When the user wants to modify code, add features, or understand the codebase:**

### Primary References (read in order)

1. **`Docs/how-to-develop-features/init-context.md`** - Complete architecture and development workflow

   - Core class hierarchy
   - How the DSL pattern works
   - Pattern generation flow
   - Development workflow and testing
   - How to add features (effects, parameters, presets)

2. **`Classes/*.sc`** - Source code to modify
   - `Classes/Number.sc` - Integer method extensions (DSL entry point)
   - `Classes/Px.sc` - Base pattern generator
   - `Classes/Fx.sc` - Effects handler
   - `Classes/Dx.sc` - Drum machines
   - See `Docs/how-to-develop-features/init-context.md` for full file organization

### What NOT to Read for Development

- ❌ `Examples/*.scd` - Usage examples (helpful for testing, not for architecture)
- ❌ `Docs/how-to-play-music/audio-samples.md` - User-facing sample guide (not relevant for coding)

### Development Quick Reference

**After editing `.sc` files:**

- **Must recompile**: Cmd+K (macOS) or Language → Recompile Class Library
- **Test manually**: Run code from `Examples/` directory
- **No unit tests**: Manual testing only

**Common development tasks:**

- Add effect → Edit `Effects/*.scd` + `Classes/Fx.sc`
- Add pattern parameter → Edit `Classes/Number.sc` + `Classes/Px.sc`
- Add drum preset → Create `Presets/yaml/*.yaml`

---

## File Type Reference

- `.sc` - SuperCollider class definitions (source code)
- `.scd` - SuperCollider data/scripts (executable code, effect definitions)
- `.yaml` - Drum machine preset data

---

## Agent Workflow Examples

### Example 1: Music Creation Request

**User**: "How do I play a sample with reverb?"

**Agent should:**

1. ✅ Read `Docs/how-to-play-music/audio-samples.md` (understand sample referencing)
2. ✅ Read `Examples/2. Loops.scd` (see sample playback examples)
3. ✅ Read `Examples/4. FX.scd` (see reverb examples)
4. ❌ Do NOT read `Docs/how-to-develop-features` or `Classes/*.sc`

### Example 2: Development Request

**User**: "Add a new effect called 'chorus'"

**Agent should:**

1. ✅ Read `Docs/how-to-develop-features/init-context.md` (understand effect architecture)
2. ✅ Read `Classes/Fx.sc` (see existing effect methods)
3. ✅ Read `Effects/Reverb.scd` (example effect structure)
4. ❌ Do NOT read `README.md` or `Examples/*` initially

### Example 3: Mixed Request

**User**: "The reverb isn't working, can you fix it?"

**Agent should:**

1. ✅ Start with `Examples/4. FX.scd` (verify correct usage)
2. ✅ If usage is correct, then read `Docs/how-to-develop-features/init-context.md` (understand architecture)
3. ✅ Then read `Classes/Fx.sc` and `Effects/Reverb.scd` (debug code)

---

## Critical Reminders

- **Be selective**: Only read docs relevant to the task
- **Music ≠ Development**: Different documentation paths
- **Examples show usage**: Great for music creation and testing
- **init-context shows architecture**: Essential for development only
