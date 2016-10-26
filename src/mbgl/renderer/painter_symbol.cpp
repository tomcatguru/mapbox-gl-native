#include <mbgl/renderer/painter.hpp>
#include <mbgl/renderer/paint_parameters.hpp>
#include <mbgl/renderer/symbol_bucket.hpp>
#include <mbgl/renderer/render_tile.hpp>
#include <mbgl/style/layers/symbol_layer.hpp>
#include <mbgl/style/layers/symbol_layer_impl.hpp>
#include <mbgl/text/glyph_atlas.hpp>
#include <mbgl/sprite/sprite_atlas.hpp>
#include <mbgl/shader/shaders.hpp>
#include <mbgl/shader/symbol_uniforms.hpp>
#include <mbgl/shader/collision_box_uniforms.hpp>
#include <mbgl/util/math.hpp>
#include <mbgl/tile/tile.hpp>

#include <cmath>

namespace mbgl {

using namespace style;

void Painter::renderSymbol(PaintParameters& parameters,
                           SymbolBucket& bucket,
                           const SymbolLayer& layer,
                           const RenderTile& tile) {
    if (pass == RenderPass::Opaque) {
        return;
    }

    const auto& layout = bucket.layout;

    frameHistory.bind(context, 1);

    auto draw = [&] (auto& shader,
                     auto&& uniformValues,
                     const auto& buffers,
                     const SymbolPropertyValues& values_)
    {
        // XXX Update render tests prior to removing this.
        const bool needsClipping = frame.mapMode == MapMode::Still;

        context.draw({
            values_.pitchAlignment == AlignmentType::Map
                ? depthModeForSublayer(0, gl::DepthMode::ReadOnly)
                : gl::DepthMode::disabled(),
            needsClipping
                ? stencilModeForClipping(tile.clip)
                : gl::StencilMode::disabled(),
            colorModeForRenderPass(),
            shader,
            std::move(uniformValues),
            gl::Segmented<gl::Triangles>(
                *buffers.vertexBuffer,
                *buffers.indexBuffer,
                buffers.segments
            )
        });
    };

    if (bucket.hasIconData()) {
        auto values = layer.impl->iconPropertyValues(layout);

        SpriteAtlas& atlas = *layer.impl->spriteAtlas;
        const bool iconScaled = values.paintSize != 1.0f || frame.pixelRatio != atlas.getPixelRatio() || bucket.iconsNeedLinear;
        const bool iconTransformed = values.rotationAlignment == AlignmentType::Map || state.getPitch() != 0;
        atlas.bind(bucket.sdfIcons || state.isScaling() || state.isRotating() || iconScaled || iconTransformed, context, 0);

        std::array<uint16_t, 2> texsize {{ atlas.getWidth(), atlas.getHeight() }};

        if (bucket.sdfIcons) {
            if (values.hasHalo()) {
                draw(parameters.shaders.symbolIconSDF,
                     SymbolSDFUniforms::haloValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                     bucket.icon,
                     values);
            }

            if (values.hasForeground()) {
                draw(parameters.shaders.symbolIconSDF,
                     SymbolSDFUniforms::foregroundValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                     bucket.icon,
                     values);
            }
        } else {
            draw(parameters.shaders.symbolIcon,
                 SymbolIconUniforms::values(values, texsize, pixelsToGLUnits, tile, state),
                 bucket.icon,
                 values);
        }
    }

    if (bucket.hasTextData()) {
        glyphAtlas->bind(context, 0);

        auto values = layer.impl->textPropertyValues(layout);

        std::array<uint16_t, 2> texsize {{ glyphAtlas->width, glyphAtlas->height }};

        if (values.hasHalo()) {
            draw(parameters.shaders.symbolGlyph,
                 SymbolSDFUniforms::haloValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                 bucket.text,
                 values);
        }

        if (values.hasForeground()) {
            draw(parameters.shaders.symbolGlyph,
                 SymbolSDFUniforms::foregroundValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                 bucket.text,
                 values);
        }
    }

    if (bucket.hasCollisionBoxData()) {
        context.draw({
            gl::DepthMode::disabled(),
            gl::StencilMode::disabled(),
            colorModeForRenderPass(),
            shaders->collisionBox,
            CollisionBoxUniforms::values(
                tile.matrix,
                std::pow(2.0f, state.getZoom() - tile.tile.id.overscaledZ),
                state.getZoom() * 10,
                (tile.id.canonical.z + 1) * 10
            ),
            gl::Unindexed<gl::Lines>(
                *bucket.collisionBox.vertexBuffer,
                1.0f
            )
        });
    }
}

} // namespace mbgl
