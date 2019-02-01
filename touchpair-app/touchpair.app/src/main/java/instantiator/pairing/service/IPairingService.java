package instantiator.pairing.service;

import android.app.Activity;

import instantiator.pairing.data.TouchEventData;

public interface IPairingService {

  void start_advertising();
  void start_discovery();
  void stop_nearby();

  PairingService.NearbyStates get_nearby_state();

  void transmit(TouchEventData data);

}
