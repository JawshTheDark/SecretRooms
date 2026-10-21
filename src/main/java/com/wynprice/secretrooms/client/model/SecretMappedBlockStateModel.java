package com.wynprice.secretrooms.client.model;

import com.google.common.math.DoubleMath;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used by blocks that don't have the shape of the block that they're mirroring, such as doors. The quads of the
 * mirrored block are squashed into the area that the model of the block's "mapped state" fills, so the texture of the
 * mirrored block ends up on the shape of (for example) a door.
 */
public class SecretMappedBlockStateModel extends SecretBlockStateModel {

    private final Map<BlockState, AABB> stateAreaCache = new ConcurrentHashMap<>();

    public SecretMappedBlockStateModel(Material.Baked overlay) {
        super(overlay);
    }

    @Override
    protected void emit(Context context) {
        Optional<BlockState> mapped = context.data().mappedState();
        if (mapped.isEmpty()) {
            super.emit(context);
            return;
        }

        AABB border = this.areaOf(mapped.get());
        // The quads have to be gathered from every side, as they will no longer be on the side they were
        MutableMesh mesh = collectMirror(context, direction -> false);
        this.emitWithOverlay(context, sink -> mesh.forEachMutable(quad -> {
            this.resizeQuad(quad, border);
            sink.accept(quad);
        }));
    }

    private AABB areaOf(BlockState state) {
        return this.stateAreaCache.computeIfAbsent(state, s -> {
            MutableMesh mesh = Renderer.get().mutableMesh();
            modelOf(s).emitQuads(mesh.emitter(), BlockAndTintGetter.EMPTY, BlockPos.ZERO, s, RandomSource.create(42L), direction -> false);

            Vector3d min = new Vector3d(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            Vector3d max = new Vector3d(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            mesh.forEach(quad -> {
                for (int i = 0; i < 4; i++) {
                    min.set(Math.min(min.x, quad.x(i)), Math.min(min.y, quad.y(i)), Math.min(min.z, quad.z(i)));
                    max.set(Math.max(max.x, quad.x(i)), Math.max(max.y, quad.y(i)), Math.max(max.z, quad.z(i)));
                }
            });
            if (mesh.size() == 0) {
                return new AABB(0, 0, 0, 1, 1, 1);
            }
            return new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
        });
    }

    private void resizeQuad(MutableQuadView quad, AABB range) {
        Vector3d[] vertices = new Vector3d[4];
        Vector3d[] clamped = new Vector3d[4];
        float[] u = new float[4];
        float[] v = new float[4];
        for (int i = 0; i < 4; i++) {
            vertices[i] = new Vector3d(quad.x(i), quad.y(i), quad.z(i));
            clamped[i] = new Vector3d(
                Mth.clamp(vertices[i].x, range.minX, range.maxX),
                Mth.clamp(vertices[i].y, range.minY, range.maxY),
                Mth.clamp(vertices[i].z, range.minZ, range.maxZ)
            );
            u[i] = quad.u(i);
            v[i] = quad.v(i);
        }

        this.resetUVPositions(u, v, vertices, clamped);

        for (int i = 0; i < 4; i++) {
            quad.pos(i, (float) clamped[i].x, (float) clamped[i].y, (float) clamped[i].z);
            quad.uv(i, u[i], v[i]);
        }
        // The quad doesn't sit on the edge of the block any more, so it can't be culled
        quad.cullFace(null);
    }

    private void resetUVPositions(float[] u, float[] v, Vector3d[] vertices, Vector3d[] clamped) {
        boolean uvRot = u[0] == u[1];

        int[] t = new int[]{ 1, 0, 3, 2 };
        int[] a = new int[]{ 3, 2, 1, 0 };
        int[] mappedU = uvRot ? a : t;
        int[] mappedV = uvRot ? t : a;

        for (int i = 0; i < 4; i++) {
            if (vertices[i].equals(clamped[i])) {
                continue;
            }
            Vector3d clampedVertex = clamped[i];
            Vector3d from = vertices[i];

            Vector3d toU = vertices[mappedU[i]];
            Vector3d toV = vertices[mappedV[i]];

            Direction.Axis uAxis = getDifferential(from, toU);
            Direction.Axis vAxis = getDifferential(from, toV);

            double uInterpolated = dist(clampedVertex, from, uAxis) / dist(toU, from, uAxis);
            double vInterpolated = dist(clampedVertex, from, vAxis) / dist(toV, from, vAxis);

            u[i] = (float) interpolate(u[i], u[mappedU[i]], uInterpolated);
            v[i] = (float) interpolate(v[i], v[mappedV[i]], vInterpolated);
        }
    }

    private static Direction.@Nullable Axis getDifferential(Vector3d v1, Vector3d v2) {
        if (!DoubleMath.fuzzyEquals(v1.x, v2.x, 1.0E-7D)) {
            return Direction.Axis.X;
        }
        if (!DoubleMath.fuzzyEquals(v1.y, v2.y, 1.0E-7D)) {
            return Direction.Axis.Y;
        }
        if (!DoubleMath.fuzzyEquals(v1.z, v2.z, 1.0E-7D)) {
            return Direction.Axis.Z;
        }
        return null;
    }

    private static double interpolate(double from, double to, double alpha) {
        return from + (to - from) * alpha;
    }

    private static double dist(Vector3d v1, Vector3d v2, Direction.@Nullable Axis axis) {
        if (axis == null) {
            return 1D;
        }
        return switch (axis) {
            case X -> Math.abs(v1.x - v2.x);
            case Y -> Math.abs(v1.y - v2.y);
            case Z -> Math.abs(v1.z - v2.z);
        };
    }
}
