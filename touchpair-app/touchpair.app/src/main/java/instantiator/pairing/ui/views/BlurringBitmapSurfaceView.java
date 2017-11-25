package instantiator.pairing.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import at.favre.lib.dali.Dali;
import at.favre.lib.dali.builder.processor.RenderScriptColorFilter;

public class BlurringBitmapSurfaceView extends AbstractBitmapSurfaceView {

  protected Dali dali;
  Thread redraw_thread;

  public BlurringBitmapSurfaceView(Context context) {
    super(context);
  }

  public BlurringBitmapSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BlurringBitmapSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public BlurringBitmapSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void doInit(Context context) {
    dali = Dali.create(context);

    redraw_thread = new Thread(new Runnable() {
      @Override
      public void run() {
        do {
          if (running) { tick(); }
          try {
            Thread.sleep(50);
          } catch (InterruptedException ex) { /* nop */ }
        } while (running);
      }
    });
  }

  @Override
  protected Bitmap processBitmap(Bitmap grid_bitmap, long duration) {
    if (!safe_to_draw) { return grid_bitmap; }

    float secs = duration / 1000.0f;
    return dali.load(grid_bitmap)
      .blurRadius(2)
      .colorFilter(Color.argb(25, 0, 0, 0))
      .getAsBitmap();
  }

  public void draw_touch(int grid_x, int grid_y, int radius, int colour) {
    grid_bitmap_lock.writeLock().lock();

    if (!grid_bitmap.isMutable()) {
      Bitmap mutable = grid_bitmap.copy(grid_bitmap.getConfig(), true);
      grid_bitmap.recycle();
      grid_bitmap = mutable;
    }

    Canvas canvas = new Canvas(grid_bitmap);

    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(colour);
    canvas.drawCircle(grid_x, grid_y, radius, paint);

    grid_bitmap_lock.writeLock().unlock();
  }

  private boolean running;

  public void start() {
    if (!running) {
      running = true;
      redraw_thread.start();
    }
  }

  public void stop() {
    running = false;
  }

  public void reset() {
    grid_bitmap_lock.writeLock().lock();

    if (!grid_bitmap.isMutable()) {
      Bitmap mutable = grid_bitmap.copy(grid_bitmap.getConfig(), true);
      grid_bitmap.recycle();
      grid_bitmap = mutable;
    }

    Canvas canvas = new Canvas(grid_bitmap);

    canvas.drawColor(Color.BLACK);
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(Color.WHITE);
    canvas.drawCircle(grid_bitmap.getWidth()/2, grid_bitmap.getHeight()/2, 4.0f, paint);

    grid_bitmap_lock.writeLock().unlock();
  }
}
