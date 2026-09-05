package dev.mcpcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockBreakingManager {
    private static Minecraft client;
    private static BlockPos breakingPosition;
    private static int breakingProgress;
    private static int totalProgress;
    private static boolean isBreaking;

    private BlockBreakingManager() {}

    public static void bind(Minecraft minecraftClient) {
        client = minecraftClient;
    }

    public static void startBreaking(BlockPos position) {
        if (client == null || client.player == null || client.level == null) return;
        if (isBreaking && breakingPosition != null && breakingPosition.equals(position)) return;

        BlockState state = client.level.getBlockState(position);
        if (state.isAir()) return;

        breakingPosition = position;
        breakingProgress = 0;
        totalProgress = calculateBreakingTime(state);
        isBreaking = true;

        Direction face = calculateFace(position);
        client.gameMode.startDestroyBlock(position, face);
    }

    public static void tick() {
        if (!isBreaking || client == null || client.player == null || client.level == null) return;

        BlockState state = client.level.getBlockState(breakingPosition);
        if (state.isAir()) {
            stopBreaking();
            return;
        }

        breakingProgress++;

        Direction face = calculateFace(breakingPosition);
        boolean finished = client.gameMode.continueDestroyBlock(breakingPosition, face);

        if (finished) {
            client.gameMode.stopDestroyBlock();
            stopBreaking();
        }
    }

    public static void stopBreaking() {
        if (client != null && client.gameMode != null) {
            client.gameMode.stopDestroyBlock();
        }
        isBreaking = false;
        breakingPosition = null;
        breakingProgress = 0;
        totalProgress = 0;
    }

    public static boolean isBreaking() {
        return isBreaking;
    }

    public static float getProgress() {
        if (!isBreaking || totalProgress == 0) return 0;
        return (float) breakingProgress / totalProgress;
    }

    public static BlockPos getBreakingPosition() {
        return breakingPosition;
    }

    private static int calculateBreakingTime(BlockState state) {
        float hardness = state.getDestroySpeed(client.level, breakingPosition);
        if (hardness < 0) return -1;

        float baseTime = hardness * 1.5f;

        if (client.player != null) {
            float toolMultiplier = getToolMultiplier(state);
            baseTime /= toolMultiplier;
        }

        return Math.max(1, (int) (baseTime * 20));
    }

    private static float getToolMultiplier(BlockState state) {
        if (client.player == null) return 1.0f;

        var heldItem = client.player.getMainHandItem();
        if (heldItem.isEmpty()) return 1.0f;

        float destroySpeed = heldItem.getDestroySpeed(state);
        if (destroySpeed > 1.0f) {
            return 1.0f + (destroySpeed - 1.0f) * 0.2f;
        }

        return 1.0f;
    }

    private static Direction calculateFace(BlockPos position) {
        double dx = client.player.getX() - (position.getX() + 0.5);
        double dy = client.player.getY() + client.player.getEyeHeight() - (position.getY() + 0.5);
        double dz = client.player.getZ() - (position.getZ() + 0.5);

        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);
        double absDz = Math.abs(dz);

        if (absDy > absDx && absDy > absDz) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        } else if (absDx > absDz) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
