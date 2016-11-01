#pragma once

#include <mbgl/util/chrono.hpp>
#include <mbgl/util/optional.hpp>

namespace mbgl {
namespace style {

class TransitionOptions {
public:
    optional<Duration> duration = {};
    optional<Duration> delay = {};

    TransitionOptions defaults(const TransitionOptions& defaults) const {
        return {
            duration ? duration : defaults.duration,
            delay ? delay : defaults.delay
        };
    }

    explicit operator bool() const {
        return duration || delay;
    }
};

} // namespace style
} // namespace mbgl
