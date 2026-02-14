---
name: analyze-modules
description: Analyzes which modules need to be modified for a given task. Determines affected modules and whether new modules are needed.
argument-hint: "[task description]"
---

# analyze-modules Skill

Analyzes **which modules need to be modified** to fulfill the requested task.

**Task**: $ARGUMENTS

---

## Step 1: Understand Current Module Structure

### 1.1 Collect Module List
Read `settings.gradle.kts` (or `settings.gradle`) to identify all registered modules.

### 1.2 Identify Module Dependencies
Read the root `CLAUDE.md` to understand module dependency directions.
If dependency info is not in the root CLAUDE.md, check each module's `build.gradle.kts` `dependencies` block.

### 1.3 Collect Module Specs
Read `CLAUDE.md` (or `Claude.md`) in each module directory to understand its role and rules.
- Root `CLAUDE.md` (overall structure)
- Each module's `CLAUDE.md`

### 1.4 Explore Existing Code
Explore existing code related to the task.
- Check if relevant classes/interfaces already exist
- Identify existing patterns and conventions

---

## Step 2: Analyze the Task

### 2.1 Determine Affected Modules
Based on module roles and dependency directions from Step 1, determine which modules are affected by the requested task.

**Criteria:**
- Does the task fall within a module's responsibility as defined in its `CLAUDE.md`?
- Does the dependency direction cause changes to propagate to other modules?
- Which modules contain existing files that need modification?

### 2.2 Determine If New Modules Are Needed

Propose a new module when:
- A new responsibility cannot be covered by any existing module's role
- A new external system integration is required (e.g., new adapter)
- A new application entry point is needed (e.g., batch, consumer)
- A new cross-cutting utility area is needed

---

## Step 3: Output Analysis Results

Output the analysis in the following format:

```markdown
## Module Analysis Results

### Task Summary
{summary of the task}

### Affected Modules

| Module | Role | Changes Needed | New/Modified |
|--------|------|----------------|--------------|
| `module-name` | module role | specific changes | Modified |

### Modification Order (dependency-based)
List from downstream (depended-upon) to upstream (depends-on).
1. `module-a` - changes (no dependencies on other modules)
2. `module-b` - changes (depends on module-a)
3. ...

### New Modules (if applicable)
- Module name, role, and dependency relationships
- Needs to be added to settings.gradle.kts
- Needs a CLAUDE.md to be created

### Related Existing Files
- List of existing files that need modification (with full paths)
```

---

## Step 4: Create New Modules (if applicable)

If new modules are needed, perform the following:

### 4.1 Create Module Directory
Follow the same directory structure as existing modules.
```
{module}/
├── build.gradle.kts
├── CLAUDE.md
└── src/
    ├── main/kotlin/...
    └── test/kotlin/...
```

### 4.2 Write build.gradle.kts
Reference existing modules' build.gradle.kts patterns.

### 4.3 Update settings.gradle.kts
Add the new module to the `include()` block.

### 4.4 Write Module CLAUDE.md
Create a CLAUDE.md with the module's role, rules, and example code.

### 4.5 Update Root CLAUDE.md
Add new module information to the root `CLAUDE.md` module structure.

---

## References

- Root `CLAUDE.md`: overall module structure and dependency directions
- Each module's `CLAUDE.md`: module-specific rules
- `settings.gradle.kts`: registered module list
