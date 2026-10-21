package com.wynprice.secretrooms.world;

import com.wynprice.secretrooms.server.blocks.SecretBaseBlock;
import com.wynprice.secretrooms.server.data.SecretData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Wraps a level (or any other block getter) so that every secret block inside of it looks like the block that it is
 * mirroring. This lets vanilla code that is asked about the mirrored block (shape, neighbour updates, tinting, etc)
 * see the world as if the secret blocks were not there.
 * <p>
 * The wrapper implements every public interface of the wrapped object, so it can be used wherever any of those
 * interfaces is expected. Everything that isn't about reading or writing a block is passed straight through.
 */
public final class MirrorWorld {

    private static final ClassValue<Class<?>[]> INTERFACES = new ClassValue<>() {
        @Override
        protected Class<?>[] computeValue(Class<?> type) {
            Set<Class<?>> out = new LinkedHashSet<>();
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                collect(c, out);
            }
            return out.toArray(new Class<?>[0]);
        }

        private void collect(Class<?> type, Set<Class<?>> out) {
            for (Class<?> iface : type.getInterfaces()) {
                if (Modifier.isPublic(iface.getModifiers()) && out.add(iface)) {
                    collect(iface, out);
                }
            }
        }
    };

    private MirrorWorld() {
    }

    /**
     * @param world the world to wrap. The result implements every interface that this object does, so it is safe
     *              to cast to any of them.
     */
    @SuppressWarnings("unchecked")
    public static <T> T wrap(T world) {
        if (world == null || Proxy.isProxyClass(world.getClass()) && Proxy.getInvocationHandler(world) instanceof Handler) {
            return world;
        }
        return (T) Proxy.newProxyInstance(MirrorWorld.class.getClassLoader(), INTERFACES.get(world.getClass()), new Handler(world));
    }

    private record Handler(Object world) implements InvocationHandler {

        private BlockGetter getter() {
            return (BlockGetter) this.world;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?>[] types = method.getParameterTypes();
            boolean singlePos = types.length == 1 && types[0] == BlockPos.class;
            switch (method.getName()) {
                case "getBlockState" -> {
                    if (singlePos) {
                        BlockPos pos = (BlockPos) args[0];
                        Optional<BlockState> mirror = SecretBaseBlock.getMirrorState(this.getter(), pos);
                        if (mirror.isPresent()) {
                            return mirror.get();
                        }
                    }
                }
                case "getBlockEntity" -> {
                    if (singlePos) {
                        Optional<SecretData> data = SecretBaseBlock.getMirrorData(this.getter(), (BlockPos) args[0]);
                        if (data.isPresent()) {
                            return data.get().getTileEntityCache();
                        }
                    }
                }
                case "getFluidState" -> {
                    if (singlePos) {
                        Optional<BlockState> mirror = SecretBaseBlock.getMirrorState(this.getter(), (BlockPos) args[0]);
                        if (mirror.isPresent()) {
                            return mirror.get().getFluidState();
                        }
                    }
                }
                case "setBlock" -> {
                    if (types.length >= 3 && types[0] == BlockPos.class && types[1] == BlockState.class) {
                        Optional<SecretData> data = SecretBaseBlock.getMirrorData(this.getter(), (BlockPos) args[0]);
                        if (data.isPresent()) {
                            data.get().setBlockState((BlockState) args[1]);
                            return true;
                        }
                    }
                }
                case "removeBlock", "destroyBlock" -> {
                    return false;
                }
                case "playSound" -> {
                    if (method.getReturnType() == void.class) {
                        return null;
                    }
                }
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "equals" -> {
                    return proxy == args[0];
                }
                case "toString" -> {
                    return "MirrorWorld[" + this.world() + "]";
                }
                default -> {
                }
            }

            try {
                return method.invoke(this.world(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
