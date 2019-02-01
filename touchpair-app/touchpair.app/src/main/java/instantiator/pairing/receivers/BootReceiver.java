package instantiator.pairing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.flt.servicelib.AbstractBootReceiver;

import instantiator.pairing.service.PairingService;

public class BootReceiver extends AbstractBootReceiver<PairingService> {

  @Override
  protected Class<PairingService> getServiceClass() {
    return PairingService.class;
  }

}
