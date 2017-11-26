package instantiator.pairing.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import instantiator.pairing.R;
import instantiator.pairing.data.TouchEventData;
import instantiator.pairing.ui.views.BlurringBitmapSurfaceView;
import instantiator.pairing.interfaces.TouchReceiver;
import instantiator.pairing.interfaces.TouchSource;

public class MirrorDisplayFragment extends Fragment implements TouchReceiver, TouchSource {
  private static final String ARG_RENDERING = "rendering";

  @BindView(R.id.panel_surface) public BlurringBitmapSurfaceView panel_surface;

  private String rendering;

  private TouchReceiver receiver;
  private MirrorFragmentListener listener;

  private int personal_radius = 3;
  private int personal_colour = Color.CYAN;
  private int remote_colour = Color.YELLOW;
  private boolean force_colours = true;

  public MirrorDisplayFragment() { }

  public static MirrorDisplayFragment create(String rendering) {
    MirrorDisplayFragment fragment = new MirrorDisplayFragment();
    Bundle args = new Bundle();
    args.putString(ARG_RENDERING, rendering);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      rendering = getArguments().getString(ARG_RENDERING);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (panel_surface != null) {
      panel_surface.stop();
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    if (panel_surface != null) {
      panel_surface.start();
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_mirror_display, container, false);
    ButterKnife.bind(this, view);

    panel_surface.initBitmap(40,60);

    panel_surface.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (view == panel_surface) {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
              float x = event.getX();
              float y = event.getY();
              int cell_x = panel_surface.cellCoordinateForTouchX(x);
              int cell_y = panel_surface.cellCoordinateForTouchY(y);
              panel_surface.draw_touch(cell_x, cell_y, personal_radius, personal_colour);
              if (receiver != null) {
                TouchEventData ted = new TouchEventData(cell_x, cell_y, personal_colour, personal_radius);
                receiver.receive_touch(ted);
              }
              return true;
            default:
              return true;
          }

        } else {
          return false;
        }
      }
    });

    panel_surface.start();
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof MirrorFragmentListener) {
      listener = (MirrorFragmentListener) context;
    } else {
      throw new RuntimeException(context.getClass().getName() + " not a MirrorFragmentListener.");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    if (listener != null) {
      listener = null;
    }
  }

  @Override
  public void register(TouchReceiver receiver) {
    this.receiver = receiver;
  }

  @Override
  public void unregister(TouchReceiver receiver) {
    this.receiver = null;
  }

  @Override
  public void receive_touch(TouchEventData data) {
    int colour = force_colours ? remote_colour : data.colour;
    panel_surface.draw_touch(data.grid_x, data.grid_y, data.radius, colour);
  }

  public void reset() {
    panel_surface.reset();
  }

  public interface MirrorFragmentListener {
    // fragment-specific events... (N/K)
  }
}
