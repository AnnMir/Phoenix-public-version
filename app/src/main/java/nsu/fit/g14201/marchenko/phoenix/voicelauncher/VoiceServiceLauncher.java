package nsu.fit.g14201.marchenko.phoenix.voicelauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VoiceServiceLauncher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, VoiceActivationService.class));
    }
}
