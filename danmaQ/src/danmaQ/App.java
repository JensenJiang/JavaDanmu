package danmaQ;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class App extends QWidget
{
    int lineHeight;
    int fontSize;
    int screenCount;
    String fontFamily;
    float speedScale;

    QLineEdit server = new QLineEdit("http://danmaku.cc:5000", this);
    QLineEdit channel = new QLineEdit("demo", this);
    QLineEdit passwd = new QLineEdit("", this);

    QPushButton configBtn = new QPushButton("&config", this);
    QPushButton hideBtn = new QPushButton("&Hide", this);
    QPushButton mainBtn = new QPushButton("&Subscribe", this);

    List<QWidget> dmWindows = new ArrayList<>();
    Subscriber subscriber;
    TrayIcon trayIcon;

    Signal0 stop_subscription = new Signal0();

    public static void main(String []args)
    {
        QApplication app = new QApplication(args);

        QDesktopWidget desktop = QApplication.desktop();
        App dm_app = new App();
        app.exec();
    }

    App()
    {
        this.setWindowTitle("Danmaku");
        this.setWindowIcon(new QIcon("res/statusicon.png"));
        this.trayIcon = new TrayIcon(this);

        QVBoxLayout layout = new QVBoxLayout(this);
        QHBoxLayout hbox = new QHBoxLayout(layout.widget());
        hbox.addWidget(new QLabel("Server: ", this));
        hbox.addWidget(this.server);
        layout.addLayout(hbox);

        hbox = new QHBoxLayout(layout.widget());
        hbox.addWidget(new QLabel("Channel: ", this));
        hbox.addWidget(this.channel);
        layout.addLayout(hbox);

        hbox = new QHBoxLayout(layout.widget());
        hbox.addWidget(new QLabel("Password: ", this));
        this.passwd.setEchoMode(QLineEdit.EchoMode.Password);
        hbox.addWidget(this.passwd);
        layout.addLayout(hbox);

        hbox = new QHBoxLayout(layout.widget());
        this.configBtn.setEnabled(false);
        hbox.addWidget(this.hideBtn);
        hbox.addWidget(this.configBtn);
        hbox.addWidget(this.mainBtn);
        layout.addLayout(hbox);

        this.setLayout(layout);

        this.fontSize = 36;
        this.lineHeight = (int)(this.fontSize * 1.2);
        this.fontFamily =
            "WenQuanYi Micro Hei, Source Han Sans CN, WenQuanYi Zen Hei," +
                    "Microsoft YaHei, SimHei, " +
                    "STHeiti, Hiragino Sans GB, " +
                    "sans-serif";
        this.speedScale = 1.0f;

        this.subscriber = null;
        this.initWindows();

        this.mainBtn.released.connect(this, "toggleSubscription()");
        this.hideBtn.released.connect(this, "hide()");
        this.trayIcon.toggleAction.triggered.connect(this, "toggleSubscription()");
        this.trayIcon.refreshScreenAction.triggered.connect(this, "resetWindows()");
        this.trayIcon.showAction.triggered.connect(this, "show()");
        this.trayIcon.aboutAction.triggered.connect(this, "showAboutDialog()");
        this.trayIcon.exitAction.triggered.connect(this, "close()");

        this.show();
        QDesktopWidget desktop = new QDesktopWidget();
        QPoint center = desktop.screenGeometry(desktop.primaryScreen()).center();
        this.move(center.x() - this.width() / 2, center.y() - this.height() / 2);
    }

    void initWindows()
    {
        QDesktopWidget desktop = new QDesktopWidget();
        this.screenCount = desktop.screenCount();
        for (int i = 0; i < desktop.screenCount(); i++)
        {
            Window w = new Window(i, this);
            this.dmWindows.add(w);

            if (this.subscriber != null && this.subscriber.thread().isAlive())
            {
                this.subscriber.new_danmaku.connect(w, "new_danmaku(String, String, String)");
            }
        }
    }

    void toggleSubscription()
    {
        /* TODO: signals on QThread not connected */
        if (this.subscriber == null || !this.subscriber.thread().isAlive())
        {
            this.subscriber = new Subscriber(server.text(), channel.text(), passwd.text(), this);
            for (Iterator<QWidget> w = this.dmWindows.iterator(); w.hasNext();)
            {
                Window window = (Window)(w.next());
                this.subscriber.new_danmaku.connect(window, "new_danmaku(String, String, String)");
            }
            this.subscriber.new_alert.connect(this, "onNewAlert(String)");
            this.subscriber.thread().start();
            onSubscriptionStarted();
        }
        else
        {
            this.subscriber.thread().interrupt();
            stop_subscription.emit();
            onSubscriptionStopped();
        }
    }

    void resetWindows()
    {
        this.dmWindows.clear();
        /* TODO: GC is not reliable, destructor needed */
        System.gc();
        this.initWindows();
    }

    void showAboutDialog()
    {
        this.show();
        QMessageBox.about(this, "About",
                "<strong>DanmaQ</strong>" +
                "<p>Copyright &copy; 2015 Justin Wong<br />" +
                "Tsinghua University TUNA Association</p>" +
                "<p> Source Code Available under GPLv3<br />" +
                "<a href='https://github.com/JensenJiang/JavaDanmu'>" +
                "https://github.com/JensenJiang/JavaDanmu" +
                "</a></p>");
    }

    void onSubscriptionStarted()
    {
        this.hide();
        this.trayIcon.setIconRunning();
        this.mainBtn.setText("&Unsubscribe");
        this.trayIcon.showMessage("Subscription Started", "Let's Go");
    }

    void onSubscriptionStopped()
    {
        this.trayIcon.setIconStopped();
        this.mainBtn.setText("&Subscribe");
    }

    void onNewAlert(String msg)
    {
        this.trayIcon.showMessage("Ooops!", msg, QSystemTrayIcon.MessageIcon.Critical);
        this.subscriber.thread().interrupt();
        stop_subscription.emit();
        onSubscriptionStopped();
        this.subscriber = null;
    }
}

class TrayIcon extends QSystemTrayIcon
{
    QAction toggleAction;
    QAction showAction;
    QAction aboutAction;
    QAction exitAction;
    QAction refreshScreenAction;

    QIcon iconRunning;
    QIcon iconStopped;

    TrayIcon(QWidget parent)
    {
        super(parent);

        this.iconRunning = new QIcon("res/statusicon.png");
        this.iconStopped = new QIcon("res/statusicon_disabled.png");
        this.setIcon(this.iconStopped);

        QMenu menu = new QMenu(parent);
        this.toggleAction = menu.addAction("Toggle Subscription");
        this.refreshScreenAction = menu.addAction("Refresh Screen");
        this.showAction = menu.addAction("Show Main Window");
        this.aboutAction = menu.addAction("About");
        this.exitAction = menu.addAction("Exit");

        this.setContextMenu(menu);

        this.activated.connect(this, "onActivated(com.trolltech.qt.gui.QSystemTrayIcon$ActivationReason)");
        this.show();
    }

    void onActivated(ActivationReason e)
    {
        if (e.equals(ActivationReason.Trigger))
        {
            QWidget parent = (QWidget) this.parent();
            if (parent == null)
                return;
            if (parent.isVisible())
                parent.hide();
            else
                parent.show();
        }
    }

    void setIconRunning()
    {
        this.setIcon(this.iconRunning);
    }

    void setIconStopped()
    {
        this.setIcon(this.iconStopped);
    }
}