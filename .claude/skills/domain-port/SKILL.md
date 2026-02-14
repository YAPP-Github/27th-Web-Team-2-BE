---
name: domain-port
description: Creates domain objects and port interfaces together following domain-centric and implementation-agnostic design.
argument-hint: "[domain name] [description]"
---

# domain-port Skill

Creates domain objects and port interfaces together.

**Request**: $ARGUMENTS

---

## Core Principles

### 1. Domain-Centric
- Use domain terminology that expresses business use cases
- Hide technical implementation details (DB, external APIs, etc.) from interfaces
- Use domain objects as parameter and return types

### 2. Implementation-Agnostic
- No concrete external system/platform types exposed in interfaces
- Prohibit implementation-specific types such as PlayStore, AppStore, MySQL, Redis, etc.
- Abstract so that implementations can be swapped

---

## Execution Flow

### Step 1: Requirement Analysis

#### 1.1 Identify the Domain
Analyze $ARGUMENTS for the following:
- What domain (feature) is this?
- What business concept is being modeled?
- What attributes and behaviors are needed?
- What external systems does it communicate with? (must not be exposed in the interface)

#### 1.2 Check Existing Code
Check for existing objects and interfaces in the related domain:
- Whether domain objects already exist
- Whether port interfaces already exist

Reuse existing objects as much as possible; only create what is missing.

---

### Step 2: Design and Create Domain Objects

#### 2.1 Identify Required Domain Objects
- **ID Value Class**: Create for each entity that needs an identifier
- **Domain Data Class**: Model representing the core business concept
- **Enum / Sealed Interface**: When state or type distinction is needed

#### 2.2 Create Domain Objects
Create in the corresponding feature package within the project's domain module.

Design rules:
- All properties must be `val` (immutable)
- IDs use `@JvmInline value class` for type safety
- Business logic is written as methods inside domain objects
- No technical concerns (JPA, JSON, etc.) in domain objects

#### 2.3 Follow Existing Patterns
Explore existing domain object files in the project and follow the same style.

---

### Step 3: Design and Create Port Interface

#### 3.1 Design the Interface
Create in the corresponding feature package within the project's port module.

Design rules:
- Suffix follows the port naming convention defined in CLAUDE.md
- Method names express domain behaviors (find, save, delete, etc.)
- Parameters and return types must be domain objects
- No concrete external systems or technologies should appear in method names or types

#### 3.2 Abstraction Verification
Verify the interface with the following questions:
- "What if we replace RDB with NoSQL?" -> No interface change should be needed
- "What if we switch the external API provider?" -> No interface change should be needed
- "What if we add a new implementation?" -> Interface changes should be minimal

If any of these questions require an interface change, revisit the abstraction level.

#### 3.3 Follow Existing Patterns
Explore existing port interfaces in the project and follow the same style.

---

### Step 4: Build Verification

Build the domain and port modules to verify there are no compilation errors.
Fix immediately if the build fails.

---

## Anti-Patterns

### Implementation Details Leaked into Interface
- Method names containing technology names like MySQL, Redis, Kafka
- Using external API response types directly as return types
- Splitting methods by platform (verifyIOS, verifyAndroid, etc.)

### Correct Approach
- Write method names using domain terminology
- Convert to domain objects before returning
- Handle platform/implementation distinction via parameter enums or in the Adapter layer

---

## Output Format

After completion, output the following:

```
## Domain + Port Creation Complete

### Created Domain Objects
- `{file path}`: {description}

### Created Port Interface
- `{file path}`: {interface name}

### Method List
| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|

### Implementation Independence Verification
- No concrete platform/technology exposed in interface
- Method names written in domain terminology
- Domain objects used as parameter/return types
- Naming convention followed

### Next Steps
1. Create implementation in Adapter module
2. Use Port in Service
3. Write tests
```
