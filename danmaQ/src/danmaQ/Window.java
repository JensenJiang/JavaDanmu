package danmaQ;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Window extends QWidget
{
    App app;
    Boolean[] flySlots;
    Boolean[] fixedSlots;

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
            flySlots[i] = false;
            fixedSlots[i] = false;
        }
    }
    
    int allocate_slot(Position position) {
//     	if(position == "fly")
    	
    	int slot = -1;
    	Random r = new Random();

    	switch (position) {
    	case FLY:
    		for (int i=0; i < 6; i++) {
    			int try_slot;
    			if (i < 3) {
    				try_slot = r.nextInt() % (this.flySlots.length / 2);
    			} else {
    				try_slot = r.nextInt() % (this.flySlots.length);
    			}
    			if(this.flySlots[try_slot] == false) {
    				this.flySlots[try_slot] = true;
    				slot = try_slot;
    				break;
    			}
    		}
    		break;
    	case TOP:
    		for(int i=0; i < this.fixedSlots.length; i++) {
    			if(this.fixedSlots[i] == false) {
    				this.fixedSlots[i] = true;
    				slot = i;
    				break;
    			}
    		}
    		break;
    	case BOTTOM:
    		for(int i=this.fixedSlots.length-1; i >= 0; i--) {
    			if(this.fixedSlots[i] == false) {
    				this.fixedSlots[i] = true;
    				slot = i;
    				break;
    			}
    		}
    		break;
    	}
    	// myDebug << "Slot: " << slot;
    	return slot;
    }
    
    int slot_y(int slot)
    {
    	return (this.app.lineHeight * slot + UI.VMARGIN);
    }
    
    String escape_text(String text) {
    	String escaped = escape(text);
    	
    	Pattern p = Pattern.compile("([^\\\\])\\\\n");
    	Matcher m = p.matcher(escaped);
    	m.replaceAll("\\1<br/>");
    	p = Pattern.compile("\\\\\\\\n");
    	m = p.matcher(escaped);
    	m.replaceAll("\\n");
    	p = Pattern.compile("\\[s\\](.+)\\[/s\\]");
    	m = p.matcher(escaped);
    	m.replaceAll("<s>\\1</s>");

    	return escaped;
    }
    
    String escape(String text)
    {
    	return text.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("\"", "&quot;");
    }
    
    void new_danmaku(String text, String color, String position)
    {
    	Position pos;
    	if(position.compareTo("fly") == 0) {
    		// myDebug << "fly";
    		pos = Position.FLY;
    	} else if (position.compareTo("top") == 0) {
    		// myDebug << "top";
    		pos = Position.TOP;
    	} else if (position.compareTo("bottom") == 0) {
    		// myDebug << "bottom";
    		pos = Position.BOTTOM;
    	} else {
    		// myDebug << "wrong position: " << position;
    		return;
    	}

    	int slot = allocate_slot(pos);
    	if (slot < 0) {
    		// myDebug << "Screen is Full!";
    		return;
    	}

    	Danmaku l = new Danmaku(escape_text(text), color, pos, slot, this, this.app);
    	l.exited.connect(this, "delete_danmaku(Danmaku)");
    	l.clear_fly_slot.connect(this, "clear_fly_slot(int)");
    	l.show();
    	// l->move(200, 200);
    }
    
    void clear_fly_slot(int slot) {
    	// myDebug << "Clear Flying Slot: " << slot;
    	// myDebug << this->fly_slots;
    	this.flySlots[slot] = false;
    }
    
    void delete_danmaku(Danmaku dm) {
    	if (dm.position == Position.TOP || dm.position == Position.BOTTOM) {
    		this.fixedSlots[dm.slot] = false;
    	}
    	// myDebug << "danmaku closed";
    }
}
