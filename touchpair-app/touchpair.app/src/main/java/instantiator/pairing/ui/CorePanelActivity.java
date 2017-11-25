package instantiator.pairing.ui;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.widget.Button;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import instantiator.pairing.PairingApp;
import instantiator.pairing.R;
import instantiator.pairing.service.IPairingService;
import instantiator.pairing.service.PairingService;
import instantiator.pairing.ui.fragments.MirrorDisplayFragment;

public class CorePanelActivity extends AbstractServiceBoundAppCompatActivity<PairingService, IPairingService> implements MirrorDisplayFragment.MirrorFragmentListener {

  @BindView(R.id.button_reset) Button tick;
  @BindView(R.id.button_permissions) Button permissions;

  MirrorDisplayFragment mirror;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_core_panel);
    ButterKnife.bind(this);

    FragmentManager fragmentManager = getSupportFragmentManager();
    mirror = (MirrorDisplayFragment) fragmentManager.findFragmentById(R.id.mirror_fragment);
  }

  @OnClick(R.id.button_permissions)
  public void permissions_click() {
    requestAllPermissions();
  }

  @OnClick(R.id.button_reset)
  public void reset_click() {
    mirror.reset();
  }

  private void updateUI() {
    boolean has_all_permissions = !anyOutstandingPermissions();
    permissions.setEnabled(!has_all_permissions);
    tick.setEnabled(bound && has_all_permissions);
  }

  @Override protected void onBoundChanged(boolean isBound) { updateUI(); }
  @Override protected void onGrantedOverlayPermission() { }
  @Override protected void onRefusedOverlayPermission() { }
  @Override protected String[] getRequiredPermissions() { return PairingApp.all_permissions; }
  @Override protected void onPermissionsGranted() { updateUI(); }
  @Override protected void onNotAllPermissionsGranted() { updateUI(); }
  @Override protected void onUnecessaryCallToRequestOverlayPermission() { }
}
