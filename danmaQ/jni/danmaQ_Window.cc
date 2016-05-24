#include "danmaQ_Window.h"

#ifdef __linux
#include <X11/Xlib.h>
#include <X11/Xregion.h>
#include <X11/Xutil.h>
#include <X11/extensions/shape.h>

extern "C" Display *_ZN8QX11Info7displayEv();
#endif

#include <iostream>

extern "C" JNIEXPORT void JNICALL Java_danmaQ_Window_xHacks(JNIEnv *, jobject,
                                                            jlong winId) {
#ifdef __linux
  Region region = XCreateRegion();
  Display *display = _ZN8QX11Info7displayEv();
  std::cout << display << std::endl;
  XShapeCombineRegion(display, winId, ShapeInput, 0, 0, region, ShapeSet);
#endif
}
