#include <mbgl/renderer/painter.hpp>
#include <mbgl/renderer/paint_parameters.hpp>
#include <mbgl/renderer/fill_bucket.hpp>
#include <mbgl/renderer/render_tile.hpp>
#include <mbgl/style/layers/fill_layer.hpp>
#include <mbgl/style/layers/fill_layer_impl.hpp>
#include <mbgl/sprite/sprite_atlas.hpp>
#include <mbgl/programs/programs.hpp>
#include <mbgl/programs/fill_program.hpp>
#include <mbgl/util/convert.hpp>

namespace mbgl {

using namespace style;

void Painter::renderFill(PaintParameters& parameters,
                         FillBucket& bucket,
                         const FillLayer& layer,
                         const RenderTile& tile) {
    const FillPaintProperties::Evaluated& properties = layer.impl->paint.evaluated;

    if (!properties.get<FillPattern>().from.empty()) {
        if (pass != RenderPass::Translucent) {
            return;
        }

        optional<SpriteAtlasPosition> imagePosA = spriteAtlas->getPosition(
            properties.get<FillPattern>().from, SpritePatternMode::Repeating);
        optional<SpriteAtlasPosition> imagePosB = spriteAtlas->getPosition(
            properties.get<FillPattern>().to, SpritePatternMode::Repeating);

        if (!imagePosA || !imagePosB) {
            return;
        }

        spriteAtlas->bind(true, context, 0);

        auto draw = [&] (uint8_t sublayer, auto& program, const auto& subject) {
            context.draw({
                depthModeForSublayer(sublayer, gl::DepthMode::ReadWrite),
                stencilModeForClipping(tile.clip),
                colorModeForRenderPass(),
                program,
                FillPatternUniforms::values(
                    tile.translatedMatrix(properties.get<FillTranslate>(),
                                          properties.get<FillTranslateAnchor>(),
                                          state),
                    properties.get<FillOpacity>(),
                    context.viewport.getCurrentValue().size,
                    *imagePosA,
                    *imagePosB,
                    properties.get<FillPattern>(),
                    tile.id,
                    state
                ),
                subject
            });
        };

        draw(0,
             parameters.programs.fillPattern,
             gl::Segmented<gl::Triangles>(
                 *bucket.vertexBuffer,
                 *bucket.triangleIndexBuffer,
                 bucket.triangleSegments));

        if (!properties.get<FillAntialias>() || !layer.impl->paint.unevaluated.get<FillOutlineColor>().isUndefined()) {
            return;
        }

        draw(2,
             parameters.programs.fillOutlinePattern,
             gl::Segmented<gl::Lines>(
                 *bucket.vertexBuffer,
                 *bucket.lineIndexBuffer,
                 bucket.lineSegments,
                 2.0f));
    } else {
        auto draw = [&] (uint8_t sublayer, auto& program, Color outlineColor, const auto& subject) {
            context.draw({
                depthModeForSublayer(sublayer, gl::DepthMode::ReadWrite),
                stencilModeForClipping(tile.clip),
                colorModeForRenderPass(),
                program,
                FillUniforms::values(
                    tile.translatedMatrix(properties.get<FillTranslate>(),
                                          properties.get<FillTranslateAnchor>(),
                                          state),
                    properties.get<FillOpacity>(),
                    properties.get<FillColor>(),
                    outlineColor,
                    context.viewport.getCurrentValue().size
                ),
                subject
            });
        };

        if (properties.get<FillAntialias>() && !layer.impl->paint.unevaluated.get<FillOutlineColor>().isUndefined() && pass == RenderPass::Translucent) {
            draw(2,
                 parameters.programs.fillOutline,
                 properties.get<FillOutlineColor>(),
                 gl::Segmented<gl::Lines>(
                     *bucket.vertexBuffer,
                     *bucket.lineIndexBuffer,
                     bucket.lineSegments,
                     2.0f));
        }

        // Only draw the fill when it's opaque and we're drawing opaque fragments,
        // or when it's translucent and we're drawing translucent fragments.
        if ((properties.get<FillColor>().a >= 1.0f && properties.get<FillOpacity>() >= 1.0f) == (pass == RenderPass::Opaque)) {
            draw(1,
                 parameters.programs.fill,
                 properties.get<FillOutlineColor>(),
                 gl::Segmented<gl::Triangles>(
                     *bucket.vertexBuffer,
                     *bucket.triangleIndexBuffer,
                     bucket.triangleSegments));
        }

        if (properties.get<FillAntialias>() && layer.impl->paint.unevaluated.get<FillOutlineColor>().isUndefined() && pass == RenderPass::Translucent) {
            draw(2,
                 parameters.programs.fillOutline,
                 properties.get<FillColor>(),
                 gl::Segmented<gl::Lines>(
                     *bucket.vertexBuffer,
                     *bucket.lineIndexBuffer,
                     bucket.lineSegments,
                     2.0f));
        }
    }
}

} // namespace mbgl
