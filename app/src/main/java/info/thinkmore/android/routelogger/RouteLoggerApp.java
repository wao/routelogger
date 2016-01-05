package info.thinkmore.android.routelogger;

import android.app.Application;
import android.os.StrictMode;

import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(
    socketTimeout = 10000,
    httpMethod = HttpSender.Method.POST,
    reportType = HttpSender.Type.JSON,
    formUri = "http://yangchen.cloudant.com/acra-internal/_design/acra-storage/_update/report",
    formUriBasicAuthLogin = "scasheyeveduespackiedgai",
    formUriBasicAuthPassword = "75de5a4fec27bba90752d63069274a19ca0f9605"
)
public class RouteLoggerApp  extends Application {
    static final String TAG = "RouteLoggerApp";

    @Override
    public void onCreate(){
        //TODO: Disabled in production release
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    .detectAll().penaltyLog().penaltyDeath().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
    .penaltyLog().penaltyDeath().build());
    }
}
