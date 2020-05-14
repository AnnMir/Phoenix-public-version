package nsu.fit.g14201.marchenko.phoenix.voicelauncher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.MainActivity;

public class VoiceRecognizerService extends Service{
    private NotificationManager notificationManager;
    private static final int DEFAULT_NOTIFICATION_ID = 101;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        Log.i(App.getTag(), "Service: onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(App.getTag(), "Service: onStartCommand");
        sendNotification("Service started", "Service", "started");
        //doTask();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //Removing any notifications
        notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
        //Disabling service
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(App.getTag(), "Service: onTaskRemoved");
    }

    //Send custom notification
    public void sendNotification(String Ticker,String Title,String Text) {

        //These three lines makes Notification to open main activity after clicking on it
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.large))   // большая картинка
                .setTicker(Ticker)
                .setContentTitle(Title) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis());

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT<=15) {
            notification = builder.getNotification(); // API-15 and lower
        }else{
            notification = builder.build();
        }

        startForeground(DEFAULT_NOTIFICATION_ID, notification);
    }
}
/*Таким образом, мы получаем сервис, который автоматически запускается после старта системы.
Пользовательский интерфейс может быть вызван нажатием на уведомление, которое невозможно убрать из панели уведомлений.
После открытия пользовательского интерфейса сервис может быть отключён или включён вручную нажатием на
соответствующую кнопку. В случае закрытия приложения, включая свайп приложения из Recent Task Bar'а,
сервис останется включённым и продолжит свою работу.

Единственным возможным способом закончить работу Service без использования пользовательского интерфейса
является закрытие процесса Application при помощи Force Stop в списке приложений или же остановка
самого процесса вручную в меню настроек системы.*/