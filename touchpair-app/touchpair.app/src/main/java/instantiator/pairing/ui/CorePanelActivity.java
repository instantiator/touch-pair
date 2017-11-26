package instantiator.pairing.ui;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import instantiator.pairing.PairingApp;
import instantiator.pairing.R;
import instantiator.pairing.data.TouchEventData;
import instantiator.pairing.events.NearbyStateChangeEvent;
import instantiator.pairing.events.TouchEventDataReceivedEvent;
import instantiator.pairing.interfaces.TouchReceiver;
import instantiator.pairing.service.IPairingService;
import instantiator.pairing.service.PairingService;
import instantiator.pairing.ui.fragments.MirrorDisplayFragment;

public class CorePanelActivity extends AbstractServiceBoundAppCompatActivity<PairingService, IPairingService> implements MirrorDisplayFragment.MirrorFragmentListener {

  @BindView(R.id.button_permissions) Button permissions;
  @BindView(R.id.button_nearby_advertising) FloatingActionButton nearby_advertising;
  @BindView(R.id.button_nearby_discovery) FloatingActionButton nearby_discovery;
  @BindView(R.id.text_nearby_state) TextView text_nearby_state;

  MirrorDisplayFragment mirror;

  private PairingService.NearbyStates nearby_state = PairingService.NearbyStates.Off;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_core_panel);
    ButterKnife.bind(this);

    FragmentManager fragmentManager = getSupportFragmentManager();
    mirror = (MirrorDisplayFragment) fragmentManager.findFragmentById(R.id.mirror_fragment);

    EventBus.getDefault().register(this);

    setTitleBarToVersionWith(getString(R.string.app_name));
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    service.stop_nearby();
    super.onStop();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on_nearby_state_change(NearbyStateChangeEvent event) {
    nearby_state = event.state;
    if (nearby_state == PairingService.NearbyStates.Connected) {
      mirror.register(mirror_touch_receiver);
    } else {
      mirror.unregister(mirror_touch_receiver);
    }
    updateUI();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on_nearby_state_change(TouchEventDataReceivedEvent event) {
    mirror.receive_touch(event.data);
  }

  @OnClick(R.id.button_permissions)
  public void permissions_click() {
    requestAllPermissions();
  }

  @OnClick(R.id.button_nearby_discovery)
  public void nearby_discovery_click() {
    service.start_discovery();
  }

  @OnClick(R.id.button_nearby_advertising)
  public void nearby_advertising_click() {
    service.start_advertising();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, R.string.menu_advertise, 10, R.string.menu_advertise);
    menu.add(0, R.string.menu_discover, 10, R.string.menu_discover);
    menu.add(0, R.string.menu_stop_nearby, 10, R.string.menu_stop_nearby);
    menu.add(0, R.string.menu_clear_view, 10, R.string.menu_clear_view);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.string.menu_advertise:
        service.start_advertising();
        return true;
      case R.string.menu_discover:
        service.start_discovery();
        return true;
      case R.string.menu_stop_nearby:
        service.stop_nearby();
        return true;
      case R.string.menu_clear_view:
        mirror.reset();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void updateUI() {
    boolean has_all_permissions = !anyOutstandingPermissions();
    permissions.setVisibility(has_all_permissions ? View.GONE : View.VISIBLE);
    nearby_advertising.setEnabled(bound && has_all_permissions && nearby_state == PairingService.NearbyStates.Off);
    nearby_discovery.setEnabled(bound && has_all_permissions && nearby_state == PairingService.NearbyStates.Off);
    text_nearby_state.setText(nearby_state.name());
  }

  @Override protected void onBoundChanged(boolean isBound) {
    if (bound) {
      on_nearby_state_change(new NearbyStateChangeEvent(service.get_nearby_state()));
    }
    updateUI();
  }

  private TouchReceiver mirror_touch_receiver = new TouchReceiver() {
    @Override
    public void receive_touch(TouchEventData data) {
      if (bound && nearby_state == PairingService.NearbyStates.Connected) {
        service.transmit(data);
      }
    }
  };

  @Override protected void onGrantedOverlayPermission() { }
  @Override protected void onRefusedOverlayPermission() { }
  @Override protected String[] getRequiredPermissions() { return PairingApp.all_permissions; }
  @Override protected void onPermissionsGranted() { updateUI(); }
  @Override protected void onNotAllPermissionsGranted() { updateUI(); }
  @Override protected void onUnecessaryCallToRequestOverlayPermission() { }
}
