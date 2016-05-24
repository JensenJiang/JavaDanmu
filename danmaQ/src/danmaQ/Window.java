package danmaQ;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.List;

public class Window extends QWidget
{
    App app;
    List<Boolean> flySlots = new ArrayList<>();
    List<Boolean> fixedSlots = new ArrayList<>();

    static
    {
        System.loadLibrary("danmaQ_Window");
    }

    native void xHacks(long winId);

    Window(int screenNumber, App parent)
    {
        this.setParent(parent);
        this.app = parent;
        QDesktopWidget desktop = new QDesktopWidget();
        QRect geo = desktop.screenGeometry(screenNumber);
        int sw = geo.width(), sh = geo.height();
        this.resize(sw, sh);
        this.setWindowTitle("Danmaku");
        this.setWindowFlags(Qt.WindowType.WindowStaysOnTopHint, Qt.WindowType.ToolTip, Qt.WindowType.FramelessWindowHint);
        this.setAttribute(Qt.WidgetAttribute.WA_TranslucentBackground, true);
        this.setAttribute(Qt.WidgetAttribute.WA_DeleteOnClose, true);
        this.setAttribute(Qt.WidgetAttribute.WA_Disabled, true);
        this.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents, true);
        this.setStyleSheet("background: transparent");

        this.move(geo.topLeft());
        this.initSlots();

        this.show();

        this.xHacks(this.winId());
    }

    void initSlots()
    {
        int height = this.height();
        int nlines = (height - 2 * UI.VMARGIN) / (this.app.lineHeight);

        for (int i = 0; i < nlines; i++)
        {
            this.flySlots.add(false);
            this.fixedSlots.add(false);
        }
    }
}
