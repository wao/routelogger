package info.thinkmore.android.routelogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;

@EReceiver
public class RouteLoggerReceiver extends BroadcastReceiver {
    final static String TAG = "RouteLoggerReceiver";

    public RouteLoggerReceiver() {
    }

    @ReceiverAction( "android.intent.action.BOOT_COMPLETED" )
    void bootCompleted(Context context){
        Log.v(TAG, "Boot completed!");
        RouteLoggerService_.intent(context).start();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
