#include <mbgl/renderer/painter.hpp>
#include <mbgl/renderer/paint_parameters.hpp>
#include <mbgl/style/layers/background_layer.hpp>
#include <mbgl/style/layers/background_layer_impl.hpp>
#include <mbgl/programs/programs.hpp>
#include <mbgl/programs/fill_program.hpp>
#include <mbgl/sprite/sprite_atlas.hpp>
#include <mbgl/util/tile_cover.hpp>

namespace mbgl {

using namespace style;

void Painter::renderBackground(PaintParameters& parameters, const BackgroundLayer& layer) {
    // Note that for bottommost layers without a pattern, the background color is drawn with
    // glClear rather than this method.
    const BackgroundPaintProperties::Evaluated& properties = layer.impl->paint.evaluated;

    if (!properties.get<BackgroundPattern>().to.empty()) {
        optional<SpriteAtlasPosition> imagePosA = spriteAtlas->getPosition(
            properties.get<BackgroundPattern>().from, SpritePatternMode::Repeating);
        optional<SpriteAtlasPosition> imagePosB = spriteAtlas->getPosition(
            properties.get<BackgroundPattern>().to, SpritePatternMode::Repeating);

        if (!imagePosA || !imagePosB)
            return;

        spriteAtlas->bind(true, context, 0);

        for (const auto& tileID : util::tileCover(state, state.getIntegerZoom())) {
            context.draw({
                depthModeForSublayer(0, gl::DepthMode::ReadOnly),
                gl::StencilMode::disabled(),
                colorModeForRenderPass(),
                parameters.programs.fillPattern,
                FillPatternUniforms::values(
                    matrixForTile(tileID),
                    properties.get<BackgroundOpacity>(),
                    context.viewport.getCurrentValue().size,
                    *imagePosA,
                    *imagePosB,
                    properties.get<BackgroundPattern>(),
                    tileID,
                    state
                ),
                gl::Unindexed<gl::TriangleStrip>(tileTriangleVertexBuffer)
            });
        }
    } else {
        for (const auto& tileID : util::tileCover(state, state.getIntegerZoom())) {
            context.draw({
                depthModeForSublayer(0, gl::DepthMode::ReadOnly),
                gl::StencilMode::disabled(),
                colorModeForRenderPass(),
                parameters.programs.fill,
                FillUniforms::values(
                    matrixForTile(tileID),
                    properties.get<BackgroundOpacity>(),
                    properties.get<BackgroundColor>(),
                    properties.get<BackgroundColor>(),
                    context.viewport.getCurrentValue().size
                ),
                gl::Unindexed<gl::TriangleStrip>(tileTriangleVertexBuffer)
            });
        }
    }
}

} // namespace mbgl
