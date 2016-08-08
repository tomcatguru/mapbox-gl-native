#pragma once

#include <mbgl/gl/implementation.hpp>

#if MBGL_USE_CGL
#include <OpenGL/OpenGL.h>
#elif MBGL_USE_GLX
typedef struct _XDisplay Display;
typedef struct __GLXFBConfigRec* GLXFBConfig;
#endif

namespace mbgl {

class HeadlessDisplay {
public:
    HeadlessDisplay();
    ~HeadlessDisplay();

#if MBGL_USE_CGL
    CGLPixelFormatObj pixelFormat = nullptr;
#endif

#if MBGL_USE_EGL
    int fd;
    struct gbm_device *gbm = nullptr;
    EGLDisplay dpy = 0;
    EGLConfig config = 0;
#endif
};

} // namespace mbgl
