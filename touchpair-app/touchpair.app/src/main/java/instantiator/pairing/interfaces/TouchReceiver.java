package instantiator.pairing.interfaces;

import instantiator.pairing.data.TouchEventData;

public interface TouchReceiver {

  void on_touch_received(TouchEventData data);

}
