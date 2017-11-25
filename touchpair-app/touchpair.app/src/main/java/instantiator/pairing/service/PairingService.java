package instantiator.pairing.service;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

import instantiator.pairing.PairingApp;
import instantiator.pairing.R;
import instantiator.pairing.events.NearbyStateChangeEvent;
import instantiator.pairing.ui.CorePanelActivity;

public class PairingService extends AbstractBackgroundBindingService<IPairingService> implements IPairingService {
  private static final String TAG = "PS";

  public enum NearbyStates { Off, Advertising, Discovering, Requesting, Connected }
  public static final String NEARBY_SERVICE_ID = "Nearby.PairingService";

  private NearbyStates nearby_state;
  private ConnectionsClient nearby_connections_client;
  private String user_id;

  public PairingService() { }

  @Override
  public void onCreate() {
    super.onCreate();
    nearby_state = NearbyStates.Off;
    user_id = UUID.randomUUID().toString();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stop_nearby();
  }

  @Override
  public void start_advertising(Activity activity) {
    if (anyOutstandingPermissions()) {
      informUser(R.string.toast_warning_cannot_initiate_nearby_permissions_missing);
      return;
    }

    if (nearby_state == NearbyStates.Off) {
      nearby_connections_client = Nearby.getConnectionsClient(activity);
      nearby_connections_client
        .startAdvertising(
          getUserNickname(),
          NEARBY_SERVICE_ID,
          connection_lifecycle_callback,
          new AdvertisingOptions(Strategy.P2P_CLUSTER))
        .addOnSuccessListener(
          new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unusedResult) {
              informUser(R.string.toast_inform_nearby_advertising_success);
              setNearbyState(NearbyStates.Advertising);
            }
          })
        .addOnFailureListener(
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              informUser(R.string.toast_warning_unable_to_advertise_nearby);
              stop_nearby();
            }
          });
    }
  }

  @Override
  public void stop_nearby() {
    if (nearby_connections_client != null) {
      nearby_connections_client.stopAllEndpoints();
    }
    setNearbyState(NearbyStates.Off);
  }

  private void setNearbyState(NearbyStates state) {
    if (state != nearby_state) {
      this.nearby_state = state;
      EventBus.getDefault().post(new NearbyStateChangeEvent(state));
    }
  }

  @Override
  public void start_discovery(Activity activity) {
    if (anyOutstandingPermissions()) {
      informUser(R.string.toast_warning_cannot_initiate_nearby_permissions_missing);
      return;
    }

    if (nearby_state == NearbyStates.Off) {
      nearby_connections_client = Nearby.getConnectionsClient(activity);
      nearby_connections_client
        .startDiscovery(
          NEARBY_SERVICE_ID,
          endpoint_discovery_callback,
          new DiscoveryOptions(Strategy.P2P_CLUSTER))
        .addOnSuccessListener(
          new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unusedResult) {
              informUser(R.string.toast_inform_nearby_discovering_success);
              setNearbyState(NearbyStates.Discovering);
            }
          })
        .addOnFailureListener(
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              informUser(R.string.toast_warning_unable_to_discover_nearby);
              stop_nearby();
            }
          });
    }
  }

  private EndpointDiscoveryCallback endpoint_discovery_callback = new EndpointDiscoveryCallback() {
    @Override
    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
      // TODO: offer these to the user... somehow... christ - in the meantime, just connect...
      nearby_connections_client.requestConnection(
        getUserNickname(),
        endpointId,
        connection_lifecycle_callback)
        .addOnSuccessListener(
          new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unusedResult) {
              informUser(R.string.toast_inform_nearby_requesting_success);
              setNearbyState(NearbyStates.Requesting);
            }
          })
        .addOnFailureListener(
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              informUser(R.string.toast_warning_unable_to_request_nearby_connection);
              stop_nearby();
            }
          });
    }

    @Override
    public void onEndpointLost(String s) {
      informUser(R.string.toast_warning_nearby_connection_lost);
    }
  };

  private ConnectionLifecycleCallback connection_lifecycle_callback = new ConnectionLifecycleCallback() {
    @Override
    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
      // see the Authenticate Connection section of the documentation an important auth step!
      // https://developers.google.com/nearby/connections/android/manage-connections
      nearby_connections_client.acceptConnection(endpointId, nearby_payload_callback); // accept
    }

    @Override
    public void onConnectionResult(String s, ConnectionResolution result) {
      switch (result.getStatus().getStatusCode()) {
        case ConnectionsStatusCodes.STATUS_OK:
          informUser(R.string.toast_inform_nearby_connected);
          setNearbyState(NearbyStates.Connected);
          break;

        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
          informUser(R.string.toast_warning_nearby_connection_result_rejected);
          stop_nearby();
          break;

        case ConnectionsStatusCodes.STATUS_ERROR:
          informUser(R.string.toast_warning_nearby_connection_result_error);
          stop_nearby();
          break;
      }
    }

    @Override
    public void onDisconnected(String s) {
      informUser(R.string.toast_inform_nearby_disconnected);
      stop_nearby();
    }
  };

  private PayloadCallback nearby_payload_callback = new PayloadCallback() {
    @Override
    public void onPayloadReceived(String s, Payload payload) {
      // TODO: what in the hell do I do with this?!
      Log.d(TAG, "Payload received!");
    }

    @Override
    public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
      Log.v(TAG, "Payload transfer update: " + s);
    }
  };

  @Override
  public NearbyStates get_nearby_state() {
    return nearby_state;
  }

  private String getUserNickname() { return user_id; }

  @Override
  protected void restoreFrom(SharedPreferences prefs) {
    // TODO: restore state from prefs
  }

  @Override
  protected void storeTo(SharedPreferences.Editor editor) {
    // TODO: store any state in the prefs
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
