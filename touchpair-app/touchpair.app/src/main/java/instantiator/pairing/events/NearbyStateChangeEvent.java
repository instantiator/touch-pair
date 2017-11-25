package instantiator.pairing.events;

import instantiator.pairing.service.PairingService;

public class NearbyStateChangeEvent {

  public PairingService.NearbyStates state;

  public NearbyStateChangeEvent(PairingService.NearbyStates state) {
    this.state = state;
  }

}
