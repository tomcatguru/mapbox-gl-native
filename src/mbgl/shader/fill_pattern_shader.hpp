#pragma once

#include <mbgl/gl/shader.hpp>
#include <mbgl/shader/fill_uniforms.hpp>
#include <mbgl/shader/fill_attributes.hpp>

namespace mbgl {

class FillPatternShader : public gl::Shader {
public:
    FillPatternShader(gl::Context&, Defines defines = None);

    using AttributesType = FillAttributes;
    using UniformsType = FillPatternUniforms;

    typename FillAttributes::State attributesState;
    typename FillPatternUniforms::State uniformsState;
};

} // namespace mbgl
