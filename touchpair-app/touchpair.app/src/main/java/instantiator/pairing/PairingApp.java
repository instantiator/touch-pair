package instantiator.pairing;

import android.Manifest;
import android.app.Application;
import android.content.Intent;

import instantiator.pairing.service.PairingService;

public class PairingApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Intent i = new Intent(this, PairingService.class);
    startService(i);
  }

  public static String[] all_permissions = new String[] {
    Manifest.permission.INTERNET,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_COARSE_LOCATION
  };

}
