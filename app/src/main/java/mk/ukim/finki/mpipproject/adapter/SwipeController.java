package mk.ukim.finki.mpipproject.adapter;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeController extends ItemTouchHelper.Callback {

    private final SwipeListener swipeListener;

    private final Paint paint;

    private boolean swipeBack;

    public SwipeController(SwipeListener swipeListener) {
        this.swipeListener = swipeListener;

        paint = new Paint();

        swipeBack = false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListeners(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            colorBackgroundOnSwipe(c, viewHolder, dX);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListeners(Canvas c, RecyclerView recyclerView,
                                   RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                   int actionState, boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) -> {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP;
            if (swipeBack) {
                if (dX < -300) {
                    swipeListener.onSwiped(viewHolder.getAdapterPosition(),
                            SwipeListener.SwipeDirection.LEFT);
                } else if (dX > 300) {
                    swipeListener.onSwiped(viewHolder.getAdapterPosition(),
                            SwipeListener.SwipeDirection.RIGHT);
                }
            }
            return false;
        });
    }

    private void colorBackgroundOnSwipe(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        if (dX > 0) {
            paint.setColor(Color.GREEN);
            c.drawRect(
                    (float) itemView.getLeft(),
                    (float) itemView.getTop(),
                    dX,
                    (float) itemView.getBottom(),
                    paint
            );
        } else if (dX < 0){
            paint.setColor(Color.RED);
            c.drawRect(
                    (float) itemView.getRight() + dX,
                    (float) itemView.getTop(),
                    (float) itemView.getRight(),
                    (float) itemView.getBottom(),
                    paint
            );
        }
    }

    public interface SwipeListener {
        enum SwipeDirection {
            LEFT, RIGHT
        }

        void onSwiped(int itemPosition, SwipeDirection direction);
    }
}