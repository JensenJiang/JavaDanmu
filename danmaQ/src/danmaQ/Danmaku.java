/**
 * Created by hrsonion on 16/5/24.
 */
package danmaQ;
import com.trolltech.qt.core.QPropertyAnimation;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsDropShadowEffect;
import com.trolltech.qt.gui.QLabel;

import java.lang.*;

public class Danmaku extends QLabel{
    public Position position;
    public int slot;
    public Window dmwin;
    public App app;

    static String colormapFirst(String color) {
        switch(color) {
            case "white" :
                return "rgb(255, 255, 255)";
            case "black" :
                return "rgb(0, 0, 0)";
            case "blue" :
                return "rgb(20, 95, 198)";
            case "cyan" :
                return "rgb(0, 255, 255)";
            case "red" :
                return "rgb(231, 34, 0)";
            case "yellow" :
                return "rgb(255, 221, 2)";
            case "green" :
                return "rgb(4, 202, 0)";
            case "purple" :
                return "rgb(128, 0, 128)";
            default :
                break;
        }
        return "NULL";
    }
    static QColor colormapSecond(String color) {
        switch(color) {
            case "white" :
                return new QColor("black");
            case "black" :
                return new QColor("white");
            case "blue" :
                return new QColor("white");
            case "cyan" :
                return new QColor("black");
            case "red" :
                return new QColor("white");
            case "yellow" :
                return new QColor("black");
            case "green" :
                return new QColor("black");
            case "purple" :
                return new QColor("white");
            default :
                break;
        }
        return new QColor();
    }

    public enum Position {
        TOP,
        BOTTOM,
        FLY;
    }
    static final int VMARGIN;

    static {
        VMARGIN = 20;
    }

    public Danmaku(String text, String color, Position position, int slot, Window parent, App app) {
        super(escape_text(text), parent);
        this.dmwin = parent;
        this.app = app;
        this.setAttribute(Qt.WidgetAttribute.WA_DeleteOnClose); //
        String tcolor = colormapFirst(color); //
        QColor bcolor = colormapSecond(color); //

        String style = String.format(style_tmpl, this.app.fontSize, this.app.fontFamily, tcolor);

        QGraphicsDropShadowEffect effect = new QGraphicsDropShadowEffect(this);

        boolean enableShadow = false;
        // ifndef
        /*
        if(this.app.screenCount == 1) {
            this.setWindowFlags(Qt.ToolTip | Qt.FramelessWindowHint);
            this.setAttribute(Qt.WidgetAttribute.WA_TranslucentBackground, true);
            this.setAttribute(Qt.WidgetAttribute.WA_Disabled, true);
            this.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents, true);
        }*/
        // myDebug
        enableShadow = true;
        if(enableShadow) {
            effect.setBlurRadius(6);
            effect.setColor(bcolor);
            effect.setOffset(0,0);
            this.setGraphicsEffect(effect);
        }
        this.setStyleSheet(style);
        this.setContentsMargins(0, 0, 0, 0);

        Qsize _msize = this.minimumSizeHint();
        this.resize(_msize);

        this.position = position;
        this.slot = slot;
        this.init_position();
    }

    public void fly() {
        QPropertyAnimation animation = new QPropertyAnimation(this, "geometry", this);
        animation.setDuration(10 * 1000);
        animation.setStartValue(
                QRect(this._x, this._y, this.width(), this.height()));

        animation.setEndValue(
                QRect(-this.width(), this._y, this.width(), this.height()));
        animation.start(QAbstractAnimation.DeleteWhenStopped);

        connect(
                animation, SIGNAL(finished()),
                this, SLOT(clean_close())
        );
    }
    public void clean_close() {
        if(this.position == Position.FLY) {
            emit clear_fly_slot(this.slot);
        }
        this.close();
        emit exited(this);

        if(this.position == FLY) {
            emit clear_fly_slot(this.slot); //
        }
        this.close();
        emit exited(this); //
        master
    }

    // signals
    void exited() {

    }
    void clear_fly_slot(int slot) {

    }
    // signals

    private static String style_tmpl = new String("font-size: %dpx;font-weight: bold;font-family: %s;color: %s;");

    private int _x, _y;

    private static String escape_text(String text) {
        return text;
    }
    private void init_position() {
        int sw = this.parentWidget().width();
        this._y = this.dmwin.slot_y(this.slot);

        switch (this.position) {
            case FLY:
                // myDebug
                this._x = sw;
                this.fly();
                break;
            case TOP:
            case BOTTOM:
                this._x = (sw / 2) - (this.width() / 2);
                this.move(this._x, this._y);
                QTimer.singleShot(10 * 1000, this, SLOT(clean_close()));
        }
    }
}
