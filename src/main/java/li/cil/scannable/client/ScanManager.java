package li.cil.scannable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexSorting;
import li.cil.scannable.api.scanning.ScanResult;
import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.api.scanning.ScanResultRenderContext;
import li.cil.scannable.api.scanning.ScannerModule;
import li.cil.scannable.client.renderer.ScannerRenderer;
import li.cil.scannable.common.config.CommonConfig;
import li.cil.scannable.common.item.ScannerModuleItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class ScanManager {
    public static final int SCAN_COMPUTE_DURATION = 40;
    private static final int SCAN_INITIAL_RADIUS = 10;
    private static final int SCAN_TIME_OFFSET = 200;
    private static final int SCAN_GROWTH_DURATION = 2000;
    private static final int REFERENCE_RENDER_DISTANCE = 12;

    // --------------------------------------------------------------------- //

    private static float computeTargetRadius() {
        return Minecraft.getInstance().gameRenderer.getRenderDistance();
    }

    public static int computeScanGrowthDuration() {
        return SCAN_GROWTH_DURATION * Minecraft.getInstance().options.renderDistance().get() / REFERENCE_RENDER_DISTANCE;
    }

    public static float computeRadius(final long start, final float duration) {
        final float r1 = computeTargetRadius();
        final float t1 = duration;
        final float b = SCAN_TIME_OFFSET;
        final float n = 1f / ((t1 + b) * (t1 + b) - b * b);
        final float a = -r1 * b * b * n;
        final float c = r1 * n;

        final float t = (float) (System.currentTimeMillis() - start);

        return SCAN_INITIAL_RADIUS + a + (t + b) * (t + b) * c;
    }

    // --------------------------------------------------------------------- //

    private static final Set<ScanResultProvider> collectingProviders = new HashSet<>();
    private static final Map<ScanResultProvider, List<ScanResult>> collectingResults = new HashMap<>();

    private static final Map<ScanResultProvider, List<ScanResult>> pendingResults = new HashMap<>();
    private static final Map<ScanResultProvider, List<ScanResult>> renderingResults = new HashMap<>();
    private static final List<ScanResult> renderingList = new ArrayList<>();

    private static int scanningTicks = -1;
    private static long currentStart = -1;
    @Nullable private static Vec3 lastScanCenter;

    private static PoseStack worldViewModelStack;
    private static Matrix4f worldProjectionMatrix;

    // --------------------------------------------------------------------- //

    public static void beginScan(final Player player, final List<ItemStack> stacks) {
        cancelScan();

        float scanRadius = CommonConfig.baseScanRadius;

        final List<ScannerModule> modules = new ArrayList<>();
        for (final ItemStack stack : stacks) {
            final Optional<ScannerModule> module = ScannerModuleItem.getModule(stack);
            module.ifPresent(modules::add);
        }
        for (final ScannerModule module : modules) {
            final ScanResultProvider provider = module.getResultProvider();
            if (provider != null) {
                collectingProviders.add(provider);
            }

            scanRadius = module.adjustGlobalRange(scanRadius);
        }

        if (collectingProviders.isEmpty()) {
            return;
        }

        final Vec3 center = player.position();
        for (final ScanResultProvider provider : collectingProviders) {
            provider.initialize(player, stacks, center, scanRadius, SCAN_COMPUTE_DURATION);
        }
    }

    public static void updateScan(final Entity entity, final boolean finish) {
        final int remaining = SCAN_COMPUTE_DURATION - scanningTicks;

        if (!finish) {
            if (remaining <= 0) {
                return;
            }

            for (final ScanResultProvider provider : collectingProviders) {
                provider.computeScanResults();
            }

            ++scanningTicks;

            return;
        }

        for (int i = 0; i < remaining; i++) {
            for (final ScanResultProvider provider : collectingProviders) {
                provider.computeScanResults();
            }
        }

        for (final ScanResultProvider provider : collectingProviders) {
            provider.collectScanResults(entity.level(), result -> collectingResults.computeIfAbsent(provider, p -> new ArrayList<>()).add(result));
            provider.reset();
        }

        clear();

        lastScanCenter = Objects.requireNonNull(entity.position());
        currentStart = System.currentTimeMillis();

        pendingResults.putAll(collectingResults);
        pendingResults.values().forEach(list -> list.sort(Comparator.comparing(result -> -lastScanCenter.distanceTo(result.getPosition()))));

        ScannerRenderer.INSTANCE.ping(lastScanCenter);

        cancelScan();
    }

    public static void cancelScan() {
        collectingProviders.clear();
        collectingResults.clear();
        scanningTicks = 0;
    }

    public static void tick() {
        if (lastScanCenter == null || currentStart < 0) {
            return;
        }

        if (CommonConfig.scanStayDuration < (int) (System.currentTimeMillis() - currentStart)) {
            pendingResults.forEach((provider, results) -> results.forEach(ScanResult::close));
            pendingResults.clear();
            synchronized (renderingResults) {
                if (!renderingResults.isEmpty()) {
                    for (final Iterator<Map.Entry<ScanResultProvider, List<ScanResult>>> iterator = renderingResults.entrySet().iterator(); iterator.hasNext(); ) {
                        final Map.Entry<ScanResultProvider, List<ScanResult>> entry = iterator.next();
                        final List<ScanResult> list = entry.getValue();
                        for (int i = Mth.ceil(list.size() * 0.5f); i > 0; i--) {
                            list.remove(list.size() - 1).close();
                        }
                        if (list.isEmpty()) {
                            iterator.remove();
                        }
                    }
                }

                if (renderingResults.isEmpty()) {
                    clear();
                }
            }
            return;
        }

        if (pendingResults.isEmpty()) {
            return;
        }

        final float radius = computeRadius(currentStart, computeScanGrowthDuration());
        final float sqRadius = radius * radius;

        final Iterator<Map.Entry<ScanResultProvider, List<ScanResult>>> iterator = pendingResults.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<ScanResultProvider, List<ScanResult>> entry = iterator.next();
            final ScanResultProvider provider = entry.getKey();
            final List<ScanResult> results = entry.getValue();

            while (!results.isEmpty()) {
                final int index = results.size() - 1;
                final Vec3 position = results.get(index).getPosition();
                if (lastScanCenter.distanceToSqr(position) <= sqRadius) {
                    final ScanResult result = results.remove(index);
                    synchronized (renderingResults) {
                        renderingResults.computeIfAbsent(provider, p -> new ArrayList<>()).add(result);
                    }
                } else {
                    break;
                }
            }

            if (results.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public static void setMatrices(final PoseStack poseStack, final Matrix4f projectionMatrix) {
        worldViewModelStack = new PoseStack();
        worldViewModelStack.last().pose().set(poseStack.last().pose());
        worldProjectionMatrix = projectionMatrix;
    }

    public static void renderLevel(final float partialTick) {
        synchronized (renderingResults) {
            if (renderingResults.isEmpty()) {
                return;
            }

            render(ScanResultRenderContext.WORLD, partialTick, worldViewModelStack, worldProjectionMatrix);
        }
    }

    public static void renderGui(final float partialTick) {
        synchronized (renderingResults) {
            if (renderingResults.isEmpty()) {
                return;
            }

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(worldProjectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);

            render(ScanResultRenderContext.GUI, partialTick, worldViewModelStack, worldProjectionMatrix);

            RenderSystem.restoreProjectionMatrix();
        }
    }

    private static void render(final ScanResultRenderContext context, final float partialTicks, final PoseStack poseStack, final Matrix4f projectionMatrix) {
        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        final Vec3 pos = camera.getPosition();

        final Frustum frustum = new Frustum(poseStack.last().pose(), projectionMatrix);
        frustum.prepare(pos.x(), pos.y(), pos.z());

        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        poseStack.pushPose();
        poseStack.translate(-pos.x, -pos.y, -pos.z);

        final MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        try {
            for (final Map.Entry<ScanResultProvider, List<ScanResult>> entry : renderingResults.entrySet()) {
                for (final ScanResult result : entry.getValue()) {
                    final AABB bounds = result.getRenderBounds();
                    if (bounds == null || frustum.isVisible(bounds)) {
                        renderingList.add(result);
                    }
                }

                if (!renderingList.isEmpty()) {
                    entry.getKey().render(context, renderTypeBuffer, poseStack, camera, partialTicks, renderingList);
                    renderingList.clear();
                }
            }
        } finally {
            renderingList.clear();
        }

        renderTypeBuffer.endBatch();

        poseStack.popPose();

        RenderSystem.enableDepthTest();
    }

    // --------------------------------------------------------------------- //

    private static void clear() {
        pendingResults.clear();

        synchronized (renderingResults) {
            renderingResults.forEach((provider, results) -> {
                provider.reset();
                results.forEach(ScanResult::close);
            });
            renderingResults.clear();
        }

        lastScanCenter = null;
        currentStart = -1;
    }
}
