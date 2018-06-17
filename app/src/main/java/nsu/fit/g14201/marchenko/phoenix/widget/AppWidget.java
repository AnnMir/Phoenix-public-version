package nsu.fit.g14201.marchenko.phoenix.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.registration.RegistrationActivity;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.MainActivity;

public class AppWidget extends AppWidgetProvider {
    private final static String ACTION_START_RECORDING = "nsu.fit.g14201.marchenko.phoenix.widget.start_recording";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (action == null || !action.equalsIgnoreCase(AppWidget.ACTION_START_RECORDING)) {
            return;
        }

        GoogleUserConnection userConnection = GoogleUserConnection.getInstance();
        if (userConnection.isSignedIn(context)) {
            Intent recordingIntent = new Intent(context, MainActivity.class);
            recordingIntent.setAction(MainActivity.ACTION_START_RECORDING);
            context.startActivity(recordingIntent);
        } else {
            context.startActivity(new Intent(context, RegistrationActivity.class));
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(AppWidget.ACTION_START_RECORDING);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

