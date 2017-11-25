package instantiator.pairing.service;

import android.app.Activity;

public interface IPairingService {

  void start_advertising(Activity activity);
  void start_discovery(Activity activity);
  void stop_nearby();

  PairingService.NearbyStates get_nearby_state();


}
