# Secret Rooms (Fabric, Minecraft 26.3)

A Fabric port of [Wyn-Price/SecretRooms](https://github.com/Wyn-Price/SecretRooms). Ghost blocks, secret doors, levers,
buttons, pressure plates, chests, observers and more, all of which copy the look of the block they are placed against.

* Minecraft 26.3, Fabric Loader 0.19.5 or newer, Fabric API 0.163.0+26.3 or newer, Java 25
* Mod id is still `secretroomsmod`, and so are all of the block and item ids, so worlds from the older Forge versions
  keep their secret blocks. The saved data (`secret_data` with `blockstate` and `tile_data`) is in the same format.

## Building

```
./gradlew build
```

The jar ends up in `build/libs`. `./gradlew runClient` starts a development client.

## Data generation

The recipes, loot tables and tags in `src/main/generated` are generated:

```
./gradlew runDatagen
```

## Testing

`./gradlew runClientGameTest` starts a client, builds a scene with every kind of secret block, checks placing, copying
with the switch probe, saving and mining, and saves screenshots to `build/run/clientGameTest/screenshots`. To check that
the blocks still render with other mods, pass their jars: `-PextraTestMods=path/to/sodium.jar,path/to/other.jar`.

## How it differs from the Forge version

* Models are Fabric Rendering API block state models. A secret block's blockstate file says
  `"fabric:type": "secretroomsmod:secret_block"` (or `secret_mapped_model` for doors and trapdoors, `one_way_glass` for
  the glass) and the model asks the mirrored block's model for its quads. The mirrored block's tint is baked into those
  quads, so grass, leaves and stained glass look right.
* How much a secret block hides other blocks and blocks light is decided by its `solid` property when it is placed, as
  Minecraft no longer lets a block look at the world to answer those questions.
* Mining speed is worked out from the mirrored block in `SecretBaseBlock#getDestroyProgress`, rather than an event.
* The secret clamber is climbable through the `minecraft:climbable` tag, and a mixin lets open secret trapdoors above a
  ladder be climbed like normal trapdoors.
* The goggles of true vision are an equippable item using the normal helmet model with the goggles texture.
* The sounds and particles of mining and breaking a secret block are the mirrored block's. Walking on it, placing it
  and running or landing particles still use the secret block's own.
