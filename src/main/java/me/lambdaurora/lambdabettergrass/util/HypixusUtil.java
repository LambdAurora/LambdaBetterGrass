package me.lambdaurora.lambdabettergrass.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class HypixusUtil {


    final public static boolean DEBUG = false;


    // always remove 1 from z shown in mc
    final public static int VERBOSE_X = 3493;
    final public static int VERBOSE_Y = 250;
    final public static int VERBOSE_Z = -1113;

    final public static boolean[] falseQuad = {false, false, false, false};

    /**
     * Logs certain event under mod with prefix "[hypixus] ".
     *
     * @param message Message to be displayed in console.
     */
    public static void log(String message) {
        System.out.println("[hypixus] " + message);
    }

    /**
     * Checks whether given block is an instance of another block.
     *
     * @param blockState  Block to be checked.
     * @param inGameBlock In-game block to be compared against.
     * @return effect of the comparison.
     */
    public static boolean compareBlocks(@NotNull BlockState blockState, @NotNull Block inGameBlock) {
        return blockState == inGameBlock.getDefaultState();
    }

    public static boolean isVisibleOnOriginalSides(boolean[] original, boolean[] toCompare) {
        for (int i = 0; i < 4; i++) {
            if (original[i] && !toCompare[i]) return false;
        }
        return true;
    }

    /**
     * Checks which sides of the block are unobstructed.
     *
     * @param world    RenderView used to get block states.
     * @param position Position of the block.
     * @return Array of four booleans, corresponding to north, south, west and east respectively.
     */
    public static boolean[] visibleSides(@NotNull BlockRenderView world, @NotNull BlockPos position, boolean verbose) {
        boolean[] toReturn = new boolean[4];
        toReturn[0] = world.getBlockState(position.north()).isAir();
        toReturn[1] = world.getBlockState(position.south()).isAir();
        toReturn[2] = world.getBlockState(position.west()).isAir();
        toReturn[3] = world.getBlockState(position.east()).isAir();
        /*toReturn[0] = world.isSkyVisible(position.north());
        toReturn[1] = world.isSkyVisible(position.south());
        toReturn[2] = world.isSkyVisible(position.west());
        toReturn[3] = world.isSkyVisible(position.east());*/
        if (verbose) log("north:" + (toReturn[0] ? "yes" : "no"));
        if (verbose) log("south:" + (toReturn[1] ? "yes" : "no"));
        if (verbose) log("west:" + (toReturn[2] ? "yes" : "no"));
        if (verbose) log("east:" + (toReturn[3] ? "yes" : "no"));
        return toReturn;
    }

    //@TODO fix dirt detection
    //@TODO look for efficient way of sun access checking
    //@TODO optimizations

    //@TODO fix the plants considered as a block thing (sunlight level)

    /**
     * Analyzes whether given block of dirt should have grassy sides.
     *
     * @param world     RenderView used to get block positions.
     * @param basePos   Target block position. Used to scan area for ability of grass growth.
     * @param baseBlock Target block.
     */
    public static boolean grassyDirt(@NotNull BlockRenderView world, @NotNull BlockPos basePos, @NotNull BlockState baseBlock) {
        return DEBUG ? grassDirtSidesDEBUG(world, basePos, baseBlock) : grassDirtSides(world, basePos, baseBlock);
    }

    // This exists to not bork a working code. Any untested changes go here.
    public static boolean grassDirtSidesDEBUG(@NotNull BlockRenderView world, @NotNull BlockPos basePos, @NotNull BlockState baseBlock) {
        boolean verbose = (basePos.getX() == VERBOSE_X && basePos.getY() == VERBOSE_Y && basePos.getZ() == VERBOSE_Z);
        log("XD");
        if (verbose) log("found verbose block. name is " + baseBlock.getBlock().getName().toString());
        if (verbose) log("x " + basePos.getX() + " y " + basePos.getY() + " z " + basePos.getZ());
        //which sides are visible in original block
        boolean[] baseBlockSides = visibleSides(world, basePos, verbose);

        // if not visible, dont waste time
        if (verbose) log("Checking for visibility...");
        if (Arrays.equals(baseBlockSides, falseQuad)) {
            if (verbose) log("Failed.");
            return false;
        }
        if (verbose) log("Passed.");
        // check if blocks above are free from the same sides as OG block
        BlockPos currentPos = basePos.up();
        BlockState currentBlock = world.getBlockState(currentPos);
        while (compareBlocks(currentBlock, Blocks.DIRT)) {
            boolean[] sides = visibleSides(world, currentPos, verbose);
            if (isVisibleOnOriginalSides(baseBlockSides, sides)) {
                if (verbose) log("Loop broken.");
                break;
            }
            if (verbose) log("Loop not broken. Moving up...");
            currentPos = currentPos.up();
            currentBlock = world.getBlockState(currentPos);
        }
        if (verbose) log("Loop ended.");
        //check whether top block is grass and has air above it, if loop above broken it will not pass either way
        if (verbose) log("Top block is " + (world.isSkyVisible(currentPos.up()) ? "visible" : "invisible"));
        return (compareBlocks(currentBlock, Blocks.GRASS_BLOCK) && world.getBlockState(currentPos.up()).isAir());
    }

    // No touchy touchy, unless sure of changes
    public static boolean grassDirtSides(@NotNull BlockRenderView world, @NotNull BlockPos basePos, @NotNull BlockState baseBlock) {
        //which sides are visible in original block
        boolean[] baseBlockSides = visibleSides(world, basePos, false);

        // if not visible, dont waste time
        if (Arrays.equals(baseBlockSides, falseQuad)) {
            return false;
        }
        // check if blocks above are free from the same sides as OG block
        BlockPos currentPos = basePos.up();
        BlockState currentBlock = world.getBlockState(currentPos);
        while (compareBlocks(currentBlock, Blocks.DIRT)) {
            boolean[] sides = visibleSides(world, currentPos, false);
            if (!Arrays.equals(baseBlockSides, sides)) {
                break;
            }
            currentPos = currentPos.up();
            currentBlock = world.getBlockState(currentPos);
        }
        //check whether top block is grass and has air above it, if loop above broken it will not pass either way
        return (compareBlocks(currentBlock, Blocks.GRASS_BLOCK) && world.getBlockState(currentPos.up()).isAir());
    }
}
