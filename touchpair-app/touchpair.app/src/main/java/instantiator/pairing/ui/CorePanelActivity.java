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
import instantiator.pairing.events.NearbyStateChangeEvent;
import instantiator.pairing.service.IPairingService;
import instantiator.pairing.service.PairingService;
import instantiator.pairing.ui.fragments.MirrorDisplayFragment;

public class CorePanelActivity extends AbstractServiceBoundAppCompatActivity<PairingService, IPairingService> implements MirrorDisplayFragment.MirrorFragmentListener {

  @BindView(R.id.button_reset) Button tick;
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
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on_nearby_state_change(NearbyStateChangeEvent event) {
    nearby_state = event.state;
    updateUI();
  }

  @OnClick(R.id.button_permissions)
  public void permissions_click() {
    requestAllPermissions();
  }

  @OnClick(R.id.button_reset)
  public void reset_click() {
    mirror.reset();
  }

  @OnClick(R.id.button_nearby_discovery)
  public void nearby_discovery_click() {
    service.start_discovery(this);
  }

  @OnClick(R.id.button_nearby_advertising)
  public void nearby_advertising_click() {
    service.start_advertising(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, R.string.menu_advertise, 10, R.string.menu_advertise);
    menu.add(0, R.string.menu_discover, 10, R.string.menu_discover);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.string.menu_advertise:
        service.start_advertising(this);
        return true;
      case R.string.menu_discover:
        service.start_discovery(this);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void updateUI() {
    boolean has_all_permissions = !anyOutstandingPermissions();
    permissions.setVisibility(has_all_permissions ? View.GONE : View.VISIBLE);
    tick.setEnabled(bound && has_all_permissions);
    nearby_advertising.setEnabled(bound && has_all_permissions && nearby_state == PairingService.NearbyStates.Off);
    nearby_discovery.setEnabled(bound && has_all_permissions && nearby_state == PairingService.NearbyStates.Off);
    text_nearby_state.setText(nearby_state.name());
  }

  @Override protected void onBoundChanged(boolean isBound) {
    nearby_state = service.get_nearby_state();
    updateUI();
  }

  @Override protected void onGrantedOverlayPermission() { }
  @Override protected void onRefusedOverlayPermission() { }
  @Override protected String[] getRequiredPermissions() { return PairingApp.all_permissions; }
  @Override protected void onPermissionsGranted() { updateUI(); }
  @Override protected void onNotAllPermissionsGranted() { updateUI(); }
  @Override protected void onUnecessaryCallToRequestOverlayPermission() { }
}
