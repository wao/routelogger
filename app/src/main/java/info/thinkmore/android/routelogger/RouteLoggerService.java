package info.thinkmore.android.routelogger;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.val;

/*@@ Listen to PhoneStateChange.onCellLocationChanged() and write to file

     Expected to be launch by intent, we expected to run as long as possible.
     And not support bind() because it doesn't provide extenal api
 */
@EService
public class RouteLoggerService extends Service{
    final static String TAG = "RouteLoggerService";

    @SystemService
    TelephonyManager telephonyMgr;


    OutputStreamWriter logFile = null;

    Handler handler = new Handler();

    DateFormat dateFormat = DateFormat.getDateTimeInstance();

    boolean alreadyListen = false;

    PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onCellLocationChanged(CellLocation location){
            recordCellLocationChanged(location);
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellinfo){
            recordCellInfoChanged( cellinfo );
        }

        @Override
        public void onServiceStateChanged(ServiceState state){
            //printf("Service State Changed:%s:%d", state.getOperatorNumeric(), state.getState());
        }

    };

    @Background(serial="update_log")
    void recordCellInfoChanged(List<CellInfo> cellinfo){
        if( cellinfo == null ) {
            try{
                logFile.write( "\n**********got null when recordCellInfoChanged*********\n");
            }catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        cellinfo = telephonyMgr.getAllCellInfo();

        try {
            logFile.write(String.format("<<|cellinfo|%d|%s|<<<\n", System.currentTimeMillis(), dateFormat.format(new Date())));
            for(val ci : cellinfo ){
                logFile.write(cellInfoToStr( ci ));
                logFile.write("\n");
            }
            logFile.write(String.format(">>|cellinfo|%d|%s|>>\n", System.currentTimeMillis(), dateFormat.format(new Date())));
            logFile.flush();
        }catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    String cellInfoToStr( CellInfo ci ){
        if( ci instanceof CellInfoCdma ){
            return cdmaCellInfoToStr( (CellInfoCdma) ci );
        }
        else if( ci instanceof CellInfoGsm){
            return gsmCellInfoToStr( (CellInfoGsm) ci );
        }
        else if( ci instanceof CellInfoLte){
            return lteCellInfoToStr( (CellInfoLte) ci );
        }
        else if( ci instanceof CellInfoWcdma){
            return wcdmaCellInfoToStr( (CellInfoWcdma) ci );
        }
        else {
            //TODO should upload log here
            return String.format("%s|%s", "unknown", ci.toString());
        }

    }

    String cdmaCellInfoToStr( CellInfoCdma ci ){
        CellIdentityCdma cii = ci.getCellIdentity();
        CellSignalStrengthCdma cis = ci.getCellSignalStrength();
        return String.format( "cdma|bid:%d|lat:%d|lon:%d|nid:%d|sid:%d|asu:%d|dbm:%d|level:%d",
                cii.getBasestationId(), cii.getLatitude(), cii.getLongitude(), cii.getNetworkId(),
                cii.getSystemId(), cis.getAsuLevel(), cis.getDbm(), cis.getLevel() );
    }

    String gsmCellInfoToStr( CellInfoGsm ci ){
        CellIdentityGsm cii = ci.getCellIdentity();
        CellSignalStrengthGsm cis = ci.getCellSignalStrength();

        return String.format("gsm|mnc:%d|mcc:%d|lac:%d|cid:%d|asu:%d:dbm:%d|level:%d",
                cii.getMcc(), cii.getMcc(), cii.getLac(), cii.getCid(),
                cis.getAsuLevel(), cis.getDbm(), cis.getLevel());
    }

    String lteCellInfoToStr( CellInfoLte ci ){
        CellIdentityLte cii = ci.getCellIdentity();
        CellSignalStrengthLte cis = ci.getCellSignalStrength();

        return String.format( "lte|mnc:%d|mcc:%d|ci:%d|pci:%d|tac:%d|asu:%d|dbm:%d|level:%d",
                cii.getMnc(),cii.getMcc(),cii.getCi(),cii.getPci(),cii.getTac(),
                cis.getAsuLevel(),cis.getDbm(),cis.getLevel());
    }

    String wcdmaCellInfoToStr( CellInfoWcdma ci ) {
        CellIdentityWcdma cii = ci.getCellIdentity();
        CellSignalStrengthWcdma cis = ci.getCellSignalStrength();


        return String.format( "gsm|mnc:%d|mcc:%d|lac:%d|cid:%d|asu:%d:dbm:%d|level:%d",
                cii.getMcc(), cii.getMcc(), cii.getLac(), cii.getCid(),
                cis.getAsuLevel(), cis.getDbm(), cis.getLevel());
    }

    @Background(serial="update_log")
    void recordCellLocationChanged(CellLocation location){
        try {
            logFile.write(String.format("%d|location|", System.currentTimeMillis()));
            logFile.write(locationToStr(location));
            logFile.write("\n");
            logFile.flush();
        }catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    String locationToStr(CellLocation location){
        if( location instanceof GsmCellLocation ){
            return gsmLocationToStr((GsmCellLocation) location);
        }
        else if( location instanceof CdmaCellLocation ){
            return cdmaLocationToStr((CdmaCellLocation) location);
        }
        else{
            //TODO should upload log here
            return String.format("%s|%s", "unknown", location.toString());
        }
    }

    String gsmLocationToStr(GsmCellLocation location){
        return String.format("%s|net:%s|lac:%d|cid:%d", "gsm", telephonyMgr.getNetworkOperator(),
                location.getLac(),location.getCid());
    }

    String cdmaLocationToStr(CdmaCellLocation location){
        return String.format("%s|net:%s|staid:%d|lat:%d|log:%d|nid:%d|sid:%d", "cdma",
                telephonyMgr.getNetworkOperator(), location.getBaseStationId(),
                location.getBaseStationLatitude(), location.getBaseStationLongitude(),
                location.getNetworkId(), location.getSystemId());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        Log.v(TAG, "Started");
        doInit();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    void doInit() {
        if (!alreadyListen) {
            Log.v(TAG, "Do real initialization.");
            telephonyMgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO);
            alreadyListen = true;
        }

        doOpenFile();
    }



    @Background
    void doOpenFile(){
        try {
            logFile = new OutputStreamWriter(new BufferedOutputStream(openFileOutput("locations.txt", MODE_APPEND)));
        }catch(FileNotFoundException ex){
            //Should not reach here, since if it is not found, android will create it for us.
            throw new RuntimeException(ex);
        }
    }
}
