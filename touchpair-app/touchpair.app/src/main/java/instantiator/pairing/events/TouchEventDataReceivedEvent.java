package instantiator.pairing.events;

import instantiator.pairing.data.TouchEventData;

public class TouchEventDataReceivedEvent {

  public TouchEventData data;

  public TouchEventDataReceivedEvent(TouchEventData data) {
    this.data = data;
  }

}
