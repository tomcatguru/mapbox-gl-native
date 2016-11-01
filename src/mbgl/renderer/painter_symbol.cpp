#include <mbgl/renderer/painter.hpp>
#include <mbgl/renderer/paint_parameters.hpp>
#include <mbgl/renderer/symbol_bucket.hpp>
#include <mbgl/renderer/render_tile.hpp>
#include <mbgl/style/layers/symbol_layer.hpp>
#include <mbgl/style/layers/symbol_layer_impl.hpp>
#include <mbgl/text/glyph_atlas.hpp>
#include <mbgl/sprite/sprite_atlas.hpp>
#include <mbgl/programs/programs.hpp>
#include <mbgl/programs/symbol_program.hpp>
#include <mbgl/programs/collision_box_program.hpp>
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

    auto draw = [&] (auto& program,
                     auto&& uniformValues,
                     const auto& buffers,
                     const SymbolPropertyValues& values_)
    {
        // In some cases, we disable the stencil test so that labels aren't clipped
        // to tile boundaries.
        //
        // Layers with features that may be drawn overlapping aren't clipped. These
        // layers are sorted in the y direction, and to draw the correct ordering near
        // tile edges the icons are included in both tiles and clipped when drawing.
        //
        // TODO remove the `true ||` when #1673 is implemented
        const bool drawAcrossEdges = (frame.mapMode == MapMode::Continuous) && (true || !(layout.get<TextAllowOverlap>() || layout.get<IconAllowOverlap>() ||
              layout.get<TextIgnorePlacement>() || layout.get<IconIgnorePlacement>()));

        context.draw({
            values_.pitchAlignment == AlignmentType::Map
                ? depthModeForSublayer(0, gl::DepthMode::ReadOnly)
                : gl::DepthMode::disabled(),
            drawAcrossEdges
                ? gl::StencilMode::disabled()
                : stencilModeForClipping(tile.clip),
            colorModeForRenderPass(),
            program,
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
                draw(parameters.programs.symbolIconSDF,
                     SymbolSDFProgram::haloUniformValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                     bucket.icon,
                     values);
            }

            if (values.hasForeground()) {
                draw(parameters.programs.symbolIconSDF,
                     SymbolSDFProgram::foregroundUniformValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                     bucket.icon,
                     values);
            }
        } else {
            draw(parameters.programs.symbolIcon,
                 SymbolIconProgram::uniformValues(values, texsize, pixelsToGLUnits, tile, state),
                 bucket.icon,
                 values);
        }
    }

    if (bucket.hasTextData()) {
        glyphAtlas->bind(context, 0);

        auto values = layer.impl->textPropertyValues(layout);

        std::array<uint16_t, 2> texsize {{ glyphAtlas->width, glyphAtlas->height }};

        if (values.hasHalo()) {
            draw(parameters.programs.symbolGlyph,
                 SymbolSDFProgram::haloUniformValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                 bucket.text,
                 values);
        }

        if (values.hasForeground()) {
            draw(parameters.programs.symbolGlyph,
                 SymbolSDFProgram::foregroundUniformValues(values, texsize, pixelsToGLUnits, tile, state, frame.pixelRatio),
                 bucket.text,
                 values);
        }
    }

    if (bucket.hasCollisionBoxData()) {
        context.draw({
            gl::DepthMode::disabled(),
            gl::StencilMode::disabled(),
            colorModeForRenderPass(),
            programs->collisionBox,
            CollisionBoxProgram::UniformValues(
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
