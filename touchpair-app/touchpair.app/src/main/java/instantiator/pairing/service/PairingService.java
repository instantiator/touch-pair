package instantiator.pairing.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import instantiator.pairing.PairingApp;
import instantiator.pairing.R;
import instantiator.pairing.ui.CorePanelActivity;

public class PairingService extends AbstractBackgroundBindingService<IPairingService> {
  public PairingService() { }

  @Override
  protected void restoreFrom(SharedPreferences prefs) {

  }

  @Override
  protected void storeTo(SharedPreferences.Editor editor) {

  }

  @Override
  protected BackgroundServiceConfig configure(BackgroundServiceConfig defaults) {
    defaults.setNotification(
      getString(R.string.notification_pairing_service_title),
      getString(R.string.notification_pairing_service_content),
      getString(R.string.notification_pairing_service_ticker),
      R.drawable.ic_people_black_24dp,
      CorePanelActivity.class
    );
    return defaults;
  }

  @Override
  protected String[] getRequiredPermissions() {
    return PairingApp.all_permissions;
  }

}
