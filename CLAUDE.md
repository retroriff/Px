# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**⚠️ READ `AGENTS.md` FIRST** - It contains the task routing guide for this project.

---

## 🎯 Start of Conversation

**IMPORTANT: At the very start of EVERY new conversation, IMMEDIATELY and AUTOMATICALLY present the user with these two options WITHOUT waiting for them to ask:**

1. **⚡️ Development** - Modifying code and adding features
2. **🎛️ Music Creation** - Writing live coding music

Ask which mode they're working in, then follow the routing guide in `AGENTS.md` to load only the relevant documentation.

This should be the FIRST thing you do in any new conversation - do not wait for the user to request it.

---

## Purpose

This repository is used for two distinct purposes:

1. **⚡️ Development** - Modifying code and adding features
2. **🎛️ Music Creation** - Writing live coding music

**`AGENTS.md` will route you to the correct documentation** based on which task the user is requesting. This prevents loading unnecessary documentation and saves tokens.

**Do not read all documentation** - be selective based on task type as specified in `AGENTS.md`.
