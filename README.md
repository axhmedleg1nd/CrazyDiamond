# Crazy Diamond Stand — Fabric 1.20.1

A summonable stand with 7 abilities. Requires Fabric Loader 0.15+ and Fabric API (0.92.2+1.20.1).

## Controls (rebindable in Options → Controls → "Crazy Diamond")
| Key | Action |
|-----|--------|
| V | Summon / dismiss the stand |
| R | Switch to the next ability (its name shows above the hotbar) |
| X | Use the selected ability |

## Abilities
| Ability | What it does |
|---------|--------------|
| Punch | Single heavy hit on whatever you look at (or smashes a block) |
| Barrage | 3 seconds of rapid hits, big knockback finisher |
| Return Block | Rebuilds blocks the stand smashed within 24 blocks, nearest first |
| Heal Mode | Heals the mob/player you look at for 5 s (yourself if nobody is targeted) |
| Stone Shot | Fires a long-range stone with a golden trail |
| Disassemble | Breaks the held item back into its crafting ingredients |
| Repair Item | Gradually restores durability of the held item |

Notes
- Blocks smashed by the stand drop nothing (so Return Block can't duplicate items). Only full,
  hard-ish blocks without block entities are affected. Set `StandActions.BREAK_BLOCKS = false`
  to disable smashing entirely.
- The texture is a simple placeholder: replace
  `src/main/resources/assets/crazydiamond/textures/entity/crazy_diamond.png` (64x32, classic
  biped layout) with your own art. Model shape lives in `client/StandModel.java`.

## Build
1. Install JDK 17.
2. Easiest: open this folder in IntelliJ IDEA (Gradle project), wait for the import, then run the
   `runClient` task to test, or `build` to produce the jar.
3. Or, with Gradle 8.5+ installed: `gradle build`. The jar appears in `build/libs/`
   (use the one without `-sources`). Drop it into `mods/` together with Fabric API.

## Code map
- `CrazyDiamondMod` – entrypoint
- `ModEntities` – registers the stand and the stone projectile
- `StandEntity` – follows the owner, runs the current ability
- `StandActions` – the logic of every ability
- `BrokenBlockLog` – memory for Return Block
- `StandNetworking` – key presses → server
- `client/*` – model, renderer, key bindings
