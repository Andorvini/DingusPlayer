package site.andorvini.miscellaneous;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatteryChanger {

    private static AtomicBoolean isFireAlarmSystemEnabled = new AtomicBoolean(false);

    public static boolean getIsFireAlarmSystemEnabled(){
        return isFireAlarmSystemEnabled.get();
    }

    public static void setIsFireAlarmSystemEnabled(boolean a){
        isFireAlarmSystemEnabled.set(a);
    }

    private static AtomicBoolean enabled = new AtomicBoolean(false);

    public static void setEnabled(boolean a){
        enabled.set(a);
    }

    public static boolean getEnabled(){
        return enabled.get();
    }

    private static Timer fireAlarmTimer = new Timer();

    public static void stopFireAlarmTimer(){
        fireAlarmTimer.cancel();
    }

    public static void startFireAlarmTimer(){
        int fireAlarmTriggerSeconds = 60;

        System.out.println("starting fire timer");

        fireAlarmTimer.scheduleAtFixedRate(new TimerTask() {
            int i = 0;
            @Override
            public void run() {
                if (i == fireAlarmTriggerSeconds){
                    System.out.println("enabled firealarmtrigger");

                    enabled.set(true);

                    fireAlarmTimer.cancel();
                }
                i++;
            }
        },0, 1000);
    }
}
