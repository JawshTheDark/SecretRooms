package com.wynprice.secretrooms.test;

import com.wynprice.secretrooms.server.blocks.SecretBaseBlock;
import com.wynprice.secretrooms.server.blocks.SecretBlocks;
import com.wynprice.secretrooms.server.items.SecretItems;
import com.wynprice.secretrooms.server.items.SwitchProbe;
import com.wynprice.secretrooms.server.tileentity.SecretTileEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.List;
import java.util.function.UnaryOperator;

public class SecretRoomsClientGameTest implements FabricClientGameTest {

    // The floor is at y = FLOOR, blocks are placed at FLOOR + 1
    private static final int FLOOR = -61;
    private static final int Y = FLOOR + 1;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            singleplayer.getConnection().waitForChunksRender();

            server.runCommand("gamemode creative @p");
            server.runCommand("time set noon");
            server.runCommand("fill -12 " + FLOOR + " -12 12 " + FLOOR + " 12 minecraft:smooth_stone");
            server.runCommand("fill -12 " + Y + " -12 12 " + (Y + 8) + " 12 minecraft:air");

            server.runOnServer(s -> buildScene(s.overworld()));
            server.runOnServer(s -> checkPersistence(s.overworld()));
            server.runOnServer(SecretRoomsClientGameTest::checkItems);

            // Looking at the first row from the south side
            server.runCommand("tp @p 0 " + (Y + 1) + " -7 0 15");
            context.waitTicks(20);
            singleplayer.getConnection().waitForChunksRender();
            context.takeScreenshot("scene_front");

            // And from above, to see the roofs of everything
            server.runCommand("tp @p 0 " + (Y + 7) + " -5 0 55");
            context.waitTicks(10);
            context.takeScreenshot("scene_above");

            // Now with the goggles on
            server.runCommand("item replace entity @p armor.head with secretroomsmod:true_vision_goggles");
            server.runCommand("tp @p 0 " + (Y + 1) + " -7 0 15");
            context.waitTicks(40);
            singleplayer.getConnection().waitForChunksRender();
            context.takeScreenshot("scene_true_vision");

            // Third person, to see the goggles on the head
            context.runOnClient(client -> client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(10);
            context.takeScreenshot("goggles_third_person");
            context.runOnClient(client -> client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));

            server.runCommand("item replace entity @p armor.head with minecraft:air");
            context.waitTicks(40);

            // Interact: flip the one way glass, press the button, open the door
            server.runOnServer(s -> interact(s.overworld()));
            context.waitTicks(20);
            singleplayer.getConnection().waitForChunksRender();
            server.runCommand("tp @p 0 " + (Y + 1) + " -1 0 10");
            context.waitTicks(20);
            context.takeScreenshot("scene_second_row");
        }
    }

    private static void place(ServerLevel level, BlockPos pos, Block secret, BlockState mirror, UnaryOperator<BlockState> edit) {
        BlockState base = secret.defaultBlockState();
        if (secret instanceof SecretBaseBlock secretBlock) {
            base = secretBlock.getPlaceState(level, pos, mirror, base);
        }
        level.setBlock(pos, edit.apply(base), 3);
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof SecretTileEntity secretEntity)) {
            throw new AssertionError("No secret block entity at " + pos);
        }
        secretEntity.getData().setBlockState(mirror);
    }

    private static BlockState vanilla(String name) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(name)).defaultBlockState();
    }

    private static void place(ServerLevel level, BlockPos pos, Block secret, BlockState mirror) {
        place(level, pos, secret, mirror, s -> s);
    }

    private static void buildScene(ServerLevel level) {
        // Row 1, in view of the first screenshot
        place(level, new BlockPos(-5, Y, 0), SecretBlocks.GHOST_BLOCK, Blocks.GOLD_BLOCK.defaultBlockState());
        place(level, new BlockPos(-4, Y, 0), SecretBlocks.GHOST_BLOCK, Blocks.GRASS_BLOCK.defaultBlockState());
        place(level, new BlockPos(-3, Y, 0), SecretBlocks.GHOST_BLOCK, Blocks.OAK_LEAVES.defaultBlockState());
        place(level, new BlockPos(-2, Y, 0), SecretBlocks.GHOST_BLOCK, Blocks.GLASS.defaultBlockState());
        place(level, new BlockPos(-1, Y, 0), SecretBlocks.GHOST_BLOCK, vanilla("red_stained_glass"));
        place(level, new BlockPos(0, Y, 0), SecretBlocks.SECRET_STAIRS, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), s -> s
            .setValue(com.wynprice.secretrooms.server.blocks.SecretStairs.FACING, Direction.NORTH)
            .setValue(com.wynprice.secretrooms.server.blocks.SecretStairs.HALF, Half.BOTTOM)
            .setValue(com.wynprice.secretrooms.server.blocks.SecretStairs.SHAPE, StairsShape.STRAIGHT));
        place(level, new BlockPos(1, Y, 0), SecretBlocks.ONE_WAY_GLASS, Blocks.DIAMOND_BLOCK.defaultBlockState(),
            s -> s.setValue(PipeBlock.NORTH, false));
        place(level, new BlockPos(2, Y, 0), SecretBlocks.ONE_WAY_GLASS, Blocks.DIAMOND_BLOCK.defaultBlockState());
        place(level, new BlockPos(3, Y, 0), SecretBlocks.SECRET_CHEST, Blocks.BOOKSHELF.defaultBlockState());
        place(level, new BlockPos(4, Y, 0), SecretBlocks.SECRET_LEVER, Blocks.ACACIA_PLANKS.defaultBlockState());
        place(level, new BlockPos(5, Y, 0), SecretBlocks.SECRET_STONE_BUTTON, Blocks.COBBLESTONE.defaultBlockState());

        // Row 2 further back: doors and trapdoors
        place(level, new BlockPos(-3, Y, 4), SecretBlocks.SECRET_DOOR, Blocks.EMERALD_BLOCK.defaultBlockState(),
            s -> s.setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.FACING, Direction.NORTH));
        place(level, new BlockPos(-3, Y + 1, 4), SecretBlocks.SECRET_DOOR, Blocks.EMERALD_BLOCK.defaultBlockState(),
            s -> s.setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.FACING, Direction.NORTH)
                .setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.HALF, DoubleBlockHalf.UPPER));
        place(level, new BlockPos(-1, Y, 4), SecretBlocks.SECRET_IRON_DOOR, Blocks.PURPUR_BLOCK.defaultBlockState(),
            s -> s.setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.FACING, Direction.NORTH));
        place(level, new BlockPos(-1, Y + 1, 4), SecretBlocks.SECRET_IRON_DOOR, Blocks.PURPUR_BLOCK.defaultBlockState(),
            s -> s.setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.FACING, Direction.NORTH)
                .setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.HALF, DoubleBlockHalf.UPPER));
        place(level, new BlockPos(1, Y, 4), SecretBlocks.SECRET_TRAPDOOR, Blocks.LAPIS_BLOCK.defaultBlockState());
        place(level, new BlockPos(3, Y, 4), SecretBlocks.SECRET_PRESSURE_PLATE, vanilla("red_wool"));
        place(level, new BlockPos(5, Y, 4), SecretBlocks.SECRET_REDSTONE, Blocks.BLACKSTONE.defaultBlockState());

        // A plain stone block next to the secret ones, to compare them with
        level.setBlock(new BlockPos(-6, Y, 0), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        level.setBlock(new BlockPos(6, Y, 0), Blocks.OAK_PLANKS.defaultBlockState(), 3);

        // Plain torch lever
        level.setBlock(new BlockPos(7, Y, 0), SecretBlocks.TORCH_LEVER.defaultBlockState(), 3);
    }

    private static void checkPersistence(ServerLevel level) {
        BlockPos pos = new BlockPos(-5, Y, 0);
        SecretTileEntity entity = (SecretTileEntity) level.getBlockEntity(pos);
        var tag = entity.saveWithFullMetadata(level.registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(pos, level.getBlockState(pos), tag, level.registryAccess());
        if (!(loaded instanceof SecretTileEntity secretLoaded) || secretLoaded.getData().getBlockState().getBlock() != Blocks.GOLD_BLOCK) {
            throw new AssertionError("Secret block did not survive saving and loading: " + tag);
        }
        System.out.println("[secretrooms-test] persistence ok: " + tag);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        System.out.println("[secretrooms-test] ok: " + message);
    }

    private static InteractionResult use(ServerPlayer player, ServerLevel level, ItemStack stack, BlockPos on, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Vec3 click = Vec3.atCenterOf(on).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
        return player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND, new BlockHitResult(click, face, on, false));
    }

    private static void checkItems(MinecraftServer server) {
        ServerLevel level = server.overworld();
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

        // Placing a ghost block against a gold block copies the gold block
        BlockPos gold = new BlockPos(-6, Y, 0);
        BlockPos placed = gold.above();
        InteractionResult result = use(player, level, new ItemStack(SecretItems.GHOST_BLOCK), gold, Direction.UP);
        check(result.consumesAction(), "placing a ghost block works (" + result + ")");
        check(level.getBlockState(placed).getBlock() == SecretBlocks.GHOST_BLOCK, "ghost block is where it was placed");
        SecretTileEntity placedEntity = (SecretTileEntity) level.getBlockEntity(placed);
        check(placedEntity.getData().getBlockState().getBlock() == Blocks.GOLD_BLOCK, "ghost block copied the gold block");
        check(level.getBlockState(placed).getValue(SecretBaseBlock.SOLID), "ghost block copied the solidity of gold");

        // A secret door is two blocks tall, and both halves copy the block they were placed on
        BlockPos floor = new BlockPos(8, FLOOR, 3);
        result = use(player, level, new ItemStack(SecretItems.SECRET_DOOR), floor, Direction.UP);
        check(result.consumesAction(), "placing a secret door works (" + result + ")");
        BlockPos lower = floor.above();
        check(level.getBlockState(lower).getBlock() == SecretBlocks.SECRET_DOOR, "door lower half placed");
        check(level.getBlockState(lower.above()).getBlock() == SecretBlocks.SECRET_DOOR, "door upper half placed");
        check(((SecretTileEntity) level.getBlockEntity(lower)).getData().getBlockState().getBlock() == Blocks.SMOOTH_STONE, "door lower half copied the floor");
        check(((SecretTileEntity) level.getBlockEntity(lower.above())).getData().getBlockState().getBlock() == Blocks.SMOOTH_STONE, "door upper half copied the floor");

        // The switch probe can copy a block and paste it on to a secret block
        ItemStack probe = new ItemStack(SecretItems.SWITCH_PROBE);
        player.setItemInHand(InteractionHand.MAIN_HAND, probe);
        BlockPos diamond = new BlockPos(2, Y, 0);
        UseOnContext copy = new UseOnContext(level, player, InteractionHand.MAIN_HAND, probe, new BlockHitResult(Vec3.atCenterOf(new BlockPos(6, Y, 0)), Direction.UP, new BlockPos(6, Y, 0), false));
        check(probe.getItem().useOn(copy).consumesAction(), "probe copies oak planks");
        check(SwitchProbe.getStored(probe).map(s -> s.blockState().getBlock() == Blocks.OAK_PLANKS).orElse(false), "probe remembers oak planks");
        UseOnContext paste = new UseOnContext(level, player, InteractionHand.MAIN_HAND, probe, new BlockHitResult(Vec3.atCenterOf(diamond), Direction.UP, diamond, false));
        check(probe.getItem().useOn(paste).consumesAction(), "probe pastes on to the one way glass");
        check(((SecretTileEntity) level.getBlockEntity(diamond)).getData().getBlockState().getBlock() == Blocks.OAK_PLANKS, "one way glass now looks like oak planks");
        // And put it back for the screenshots
        ((SecretTileEntity) level.getBlockEntity(diamond)).getData().setBlockState(Blocks.DIAMOND_BLOCK.defaultBlockState());

        // Breaking is as slow as breaking the block that is copied, and the secret block drops itself
        player.setGameMode(GameType.SURVIVAL);
        float progress = level.getBlockState(placed).getDestroyProgress(player, level, placed);
        check(progress > 0F && progress < 1F, "breaking a ghost copying gold takes a while: " + progress);
        List<ItemStack> drops = Block.getDrops(level.getBlockState(placed), level, placed, level.getBlockEntity(placed));
        check(drops.stream().anyMatch(stack -> stack.getItem() == SecretItems.GHOST_BLOCK), "ghost block drops itself: " + drops);
        player.setGameMode(GameType.CREATIVE);
        level.removeBlock(placed, false);
    }

    private static void interact(ServerLevel level) {
        BlockPos glass = new BlockPos(1, Y, 0);
        level.setBlock(glass, level.getBlockState(glass).setValue(PipeBlock.NORTH, true).setValue(PipeBlock.SOUTH, false), 3);
        ((SecretTileEntity) level.getBlockEntity(glass)).requestModelDataUpdateIfPossible();

        BlockPos door = new BlockPos(-3, Y, 4);
        BlockState doorState = level.getBlockState(door);
        level.setBlock(door, doorState.setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.OPEN, true), 10);
        BlockPos upper = door.above();
        level.setBlock(upper, level.getBlockState(upper).setValue(com.wynprice.secretrooms.server.blocks.SecretDoor.OPEN, true), 10);

        BlockPos trapdoor = new BlockPos(1, Y, 4);
        level.setBlock(trapdoor, level.getBlockState(trapdoor).setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN, true), 2);
    }
}
