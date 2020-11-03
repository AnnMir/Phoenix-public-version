package nsu.fit.g14201.marchenko.phoenix.voicelauncher;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.registration.RegistrationActivity;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.MainActivity;

public class VoiceActivationService extends Service implements
        SpeechActivationListener{

    public static final String NOTIFICATION_ICON_RESOURCE_INTENT_KEY =
            "NOTIFICATION_ICON_RESOURCE_INTENT_KEY";
    public static final String ACTIVATION_STOP_INTENT_KEY =
            "ACTIVATION_STOP_INTENT_KEY";

    private NotificationManager notificationManager;
    private boolean isStarted;
    private SpeechActivator activator;
    private static final int DEFAULT_NOTIFICATION_ID = 101;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = false;
        notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        Log.i(App.getTag(), "Service: onCreate");
    }

    public static Intent makeStartServiceIntent(Context context)
    {
        Intent i = new Intent(context, VoiceActivationService.class);
        return i;
    }

    public static Intent makeServiceStopIntent(Context context)
    {
        Intent i = new Intent(context, VoiceActivationService.class);
        i.putExtra(ACTIVATION_STOP_INTENT_KEY, true);
        return i;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(App.getTag(), "Service: onStartCommand");
        //sendNotification("Service started", "Service", "started");
        //doTask();
        if (intent != null)
        {
            if (intent.hasExtra(ACTIVATION_STOP_INTENT_KEY))
            {
                Log.d(App.getTag(), "stop service intent");
                stopActivator();
                stopSelf();
            }
            else
            {
                if (isStarted)
                {
                    Log.d(App.getTag(), "service already started");
                    if (activator == null){
                        connectActivator();
                    }
                }
                else
                {
                    // activator not started, start it
                    startDetecting(intent);
                }
            }
        }

        // restart in case the Service gets canceled
        return START_REDELIVER_INTENT;
    }

    private void startDetecting(Intent intent)
    {
        if (activator == null)
        {
            Log.d(App.getTag(), "null activator");
        }

        isStarted = true;
        Log.i(App.getTag(),"connectActivator");
        connectActivator();
        startForeground(DEFAULT_NOTIFICATION_ID, getNotification(intent));
    }

    private void connectActivator(){
        activator = new WordActivator(this, this, "старт", "start", "запуск", "стат", "тарт");
        Log.d(App.getTag(), "started: " + activator.getClass().getSimpleName());
        activator.detectActivation();
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
        Log.d(App.getTag(), "Service on destroy");
        super.onDestroy();
        stopActivator();
        stopForeground(true);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(App.getTag(), "Service: onTaskRemoved");
    }

    //Send custom notification
    public Notification sendNotification(String Ticker,String Title,String Text) {

        //These three lines makes Notification to open main activity after clicking on it
        Intent notificationIntent = new Intent(this, RegistrationActivity.class);
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

        return notification;
    }

    @Override
    public void activated(boolean success) {
            stopActivator();
            Intent registrationActivity = new Intent(this, RegistrationActivity.class);
            startActivity(registrationActivity);
            stopSelf();
        }

    private void stopActivator()
    {
        if (activator != null)
        {
            Log.d(App.getTag(), "stopped: " + activator.getClass().getSimpleName());
            activator.stop();
            isStarted = false;
        }
    }

    @SuppressLint("NewApi")
    private Notification getNotification(Intent intent)
    {
        // determine label based on the class
        String name = getLabel(this);
        String message =
                getString(R.string.speech_activation_notification_listening)
                        + " " + name;
        String title = getString(R.string.speech_activation_notification_title);

        PendingIntent pi =
                PendingIntent.getService(this, 0, makeServiceStopIntent(this),
                        0);

        int icon = intent.getIntExtra(NOTIFICATION_ICON_RESOURCE_INTENT_KEY, R.drawable.ic_launcher_foreground);

        Notification notification = sendNotification("Ticker",title,message);

        return notification;
    }

    public String getLabel(Context context)
    {
        return context.getString(R.string.speech_activation_speak);
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