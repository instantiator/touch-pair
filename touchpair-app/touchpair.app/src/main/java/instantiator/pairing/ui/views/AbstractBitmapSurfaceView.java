package instantiator.pairing.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractBitmapSurfaceView extends SurfaceView implements SurfaceHolder.Callback2 {
  private int COLOUR_BACKGROUND = Color.BLACK;

  protected boolean surface_created = false;
  protected boolean bitmap_available = false;
  protected boolean safe_to_draw = false;

  protected Bitmap grid_bitmap;
  protected long previous_time;

  protected ReadWriteLock main_bitmap_lock;
  protected ReadWriteLock grid_bitmap_lock;

  public AbstractBitmapSurfaceView(Context context) {
    super(context);
    init(context);
  }

  public AbstractBitmapSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public AbstractBitmapSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public AbstractBitmapSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  public int cellCoordinateForTouchX(float x) {
    float cell_width = getWidth() / grid_bitmap.getWidth();
    return (int)Math.floor(x / cell_width);
  }

  public int cellCoordinateForTouchY(float y) {
    float cell_height = getHeight() / grid_bitmap.getHeight();
    return (int)Math.floor(y / cell_height);
  }

  private void init(Context context) {
    getHolder().addCallback(this);
    previous_time = new Date().getTime();
    main_bitmap_lock = new ReentrantReadWriteLock();
    grid_bitmap_lock = new ReentrantReadWriteLock();
    doInit(context);
  }

  protected abstract void doInit(Context context);

  private void doClear(SurfaceHolder holder) {
    if (safe_to_draw) {
      Canvas canvas = holder.lockCanvas();
      canvas.drawColor(COLOUR_BACKGROUND);
      holder.unlockCanvasAndPost(canvas);
    }
  }

  public void initBitmap(int width, int height) {
    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    grid_bitmap = Bitmap.createBitmap(width, height, conf);
    bitmap_available = true;
    recheck_safeToDraw();
  }

  public Bitmap getBitmap() {
    return grid_bitmap;
  }

  public void tick() {
    long time = new Date().getTime();
    long duration = time - previous_time;

    grid_bitmap_lock.writeLock().lock();

    if (!grid_bitmap.isMutable()) {
      Bitmap mutable = grid_bitmap.copy(grid_bitmap.getConfig(), true);
      grid_bitmap.recycle();
      grid_bitmap = mutable;
    }

    Bitmap processed_grid_bitmap = processBitmap(grid_bitmap, duration);

    if (grid_bitmap != processed_grid_bitmap) {
      grid_bitmap.recycle();
      grid_bitmap = processed_grid_bitmap;
    }

    grid_bitmap_lock.writeLock().unlock();

    previous_time = time;
    doDraw(getHolder());
  }

  protected abstract Bitmap processBitmap(Bitmap grid_bitmap, long duration);

  private void doDraw(SurfaceHolder holder) {
    if (safe_to_draw) {
      Canvas canvas = holder.lockCanvas();

      Rect source_rect = new Rect(0, 0, grid_bitmap.getWidth(), grid_bitmap.getHeight());
      Rect dest_rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());

      Paint paint = new Paint();
      paint.setAntiAlias(false);
      paint.setFilterBitmap(false);

      grid_bitmap_lock.readLock().lock();
      main_bitmap_lock.writeLock().lock();
      canvas.drawBitmap(grid_bitmap, source_rect, dest_rect, paint);
      main_bitmap_lock.writeLock().unlock();
      grid_bitmap_lock.readLock().unlock();

      holder.unlockCanvasAndPost(canvas);
    }
  }

  @Override
  public void surfaceRedrawNeeded(SurfaceHolder holder) {
    //doDraw(holder);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    surface_created = true;
    recheck_safeToDraw();
    doDraw(holder);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
    //recheck_safeToDraw();
    //doDraw(holder);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    surface_created = false;
    recheck_safeToDraw();
  }

  private void recheck_safeToDraw() {
    safe_to_draw = bitmap_available && surface_created;
  }
}
