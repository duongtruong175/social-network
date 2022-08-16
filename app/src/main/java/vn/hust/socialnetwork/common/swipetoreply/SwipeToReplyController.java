package vn.hust.socialnetwork.common.swipetoreply;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import vn.hust.socialnetwork.R;

public class SwipeToReplyController extends ItemTouchHelper.Callback {

    private static final int MESSAGE_LEFT_TYPE = 1;
    private static final int MESSAGE_RIGHT_TYPE = 2;

    private Context mContext;
    private OnSwipeToReplyListener mOnSwipeToReplyListener;

    private Drawable mReplyIcon;
    private Drawable mReplyIconBackground;

    private RecyclerView.ViewHolder mCurrentViewHolder;
    private View mView;

    private float mDx = 0f;

    private float mReplyButtonProgress = 0f;
    private long mLastReplyButtonAnimationTime = 0;

    private boolean mSwipeBack = false;
    private boolean mIsVibrating = false;
    private boolean mStartTracking = false;

    private int mBackgroundColor = 0x20606060;

    private int mReplyBackgroundOffset = 18;

    private int mReplyIconXOffset = 12;
    private int mReplyIconYOffset = 11;

    public SwipeToReplyController(Context context, OnSwipeToReplyListener onSwipeToReplyListener) {
        mContext = context;
        mOnSwipeToReplyListener = onSwipeToReplyListener;

        mReplyIcon = AppCompatResources.getDrawable(mContext, R.drawable.ic_reply_black);
        mReplyIconBackground = AppCompatResources.getDrawable(mContext, R.drawable.ic_round_shape);
    }

    public SwipeToReplyController(Context context, OnSwipeToReplyListener onSwipeToReplyListener, int replyIcon, int replyIconBackground, int backgroundColor) {
        mContext = context;
        mOnSwipeToReplyListener = onSwipeToReplyListener;

        mReplyIcon = AppCompatResources.getDrawable(mContext, replyIcon);
        mReplyIconBackground = AppCompatResources.getDrawable(mContext, replyIconBackground);
        mBackgroundColor = backgroundColor;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        mView = viewHolder.itemView;
        if(viewHolder.getItemViewType() == MESSAGE_LEFT_TYPE) {
            return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT);
        } else {
            return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (mSwipeBack) {
            mSwipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder);
        }
        int messageType = viewHolder.getItemViewType();
        // MESSAGE_LEFT_TYPE
        if (messageType == MESSAGE_LEFT_TYPE && (mView.getTranslationX() < convertToDp(130) || dX < mDx)) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            mDx = dX;
            mStartTracking = true;
        }
        // MESSAGE_RIGHT_TYPE
        if (messageType == MESSAGE_RIGHT_TYPE && (mView.getTranslationX() > convertToDp(-130) || dX > mDx)) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            mDx = dX;
            mStartTracking = true;
        }
        mCurrentViewHolder = viewHolder;
        drawReplyButton(c, messageType);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mSwipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (mSwipeBack) {
                    if (Math.abs(mView.getTranslationX()) >= convertToDp(100)) {
                        mOnSwipeToReplyListener.onShowReplyUI(viewHolder.getBindingAdapterPosition());
                    }
                }
                return false;
            }
        });
    }

    private int convertToDp(int pixels) {
        if (pixels == 0) {
            return 0;
        } else {
            float density = mContext.getResources().getDisplayMetrics().density;
            return (int) Math.ceil((density * pixels));
        }
    }


    private void drawReplyButton(Canvas canvas, int messageType) {
        if (mCurrentViewHolder == null) {
            return;
        }

        float translationX = Math.abs(mView.getTranslationX());
        long newTime = System.currentTimeMillis();
        long dt = Math.min(17, newTime - mLastReplyButtonAnimationTime);
        mLastReplyButtonAnimationTime = newTime;
        boolean showing = translationX >= convertToDp(30);
        if (showing) {
            if (mReplyButtonProgress < 1.0f) {
                mReplyButtonProgress += dt / 180.0f;
                if (mReplyButtonProgress > 1.0f) {
                    mReplyButtonProgress = 1.0f;
                } else {
                    mView.invalidate();
                }
            }
        } else if (translationX <= 0.0f) {
            mReplyButtonProgress = 0f;
            mStartTracking = false;
            mIsVibrating = false;
        } else {
            if (mReplyButtonProgress > 0.0f) {
                mReplyButtonProgress -= dt / 180.0f;
                if (mReplyButtonProgress < 0.1f) {
                    mReplyButtonProgress = 0f;
                }
            }
            mView.invalidate();
        }
        int alpha;
        float scale;
        if (showing) {
            if (mReplyButtonProgress <= 0.8f) {
                scale = 1.2f * (mReplyButtonProgress / 0.8f);
            } else {
                scale = 1.2f - 0.2f * ((mReplyButtonProgress - 0.8f) / 0.2f);
            }
            alpha = Math.min(255, 255 * ((int) (mReplyButtonProgress / 0.8f)));
        } else {
            scale = mReplyButtonProgress;
            alpha = Math.min(255, 255 * (int) mReplyButtonProgress);
        }
        mReplyIconBackground.setAlpha(alpha);
        mReplyIcon.setAlpha(alpha);
        if (mStartTracking) {
            if (!mIsVibrating && translationX >= convertToDp(100)) {
                mView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            mIsVibrating = true;
        }

        int x;
        float y;
        if (translationX > convertToDp(130)) {
            x = convertToDp(130) / 2;
        } else {
            x = (int) translationX / 2;
        }

        y = mView.getTop() + ((float) mView.getMeasuredHeight() / 2);
        mReplyIconBackground.setColorFilter(mBackgroundColor, PorterDuff.Mode.MULTIPLY);

        // draw icon reply
        if (messageType == MESSAGE_LEFT_TYPE) {
            mReplyIconBackground.setBounds(new Rect(
                    (int) (x - convertToDp(mReplyBackgroundOffset) * scale),
                    (int) (y - convertToDp(mReplyBackgroundOffset) * scale),
                    (int) (x + convertToDp(mReplyBackgroundOffset) * scale),
                    (int) (y + convertToDp(mReplyBackgroundOffset) * scale)
            ));
            mReplyIconBackground.draw(canvas);

            mReplyIcon.setBounds(new Rect(
                    (int) (x - convertToDp(mReplyIconXOffset) * scale),
                    (int) (y - convertToDp(mReplyIconYOffset) * scale),
                    (int) (x + convertToDp(mReplyIconXOffset) * scale),
                    (int) (y + convertToDp(mReplyIconYOffset) * scale)
            ));
            mReplyIcon.draw(canvas);
        } else if (messageType == MESSAGE_RIGHT_TYPE){
            int maxScreenWidth = mView.getMeasuredWidth();
            mReplyIconBackground.setBounds(new Rect(
                    (int) (maxScreenWidth - (x + convertToDp(mReplyBackgroundOffset) * scale)),
                    (int) (y - convertToDp(mReplyBackgroundOffset) * scale),
                    (int) (maxScreenWidth - (x - convertToDp(mReplyBackgroundOffset) * scale)),
                    (int) (y + convertToDp(mReplyBackgroundOffset) * scale)
            ));
            mReplyIconBackground.draw(canvas);

            mReplyIcon.setBounds(new Rect(
                    (int) (maxScreenWidth - (x + convertToDp(mReplyIconXOffset) * scale)),
                    (int) (y - convertToDp(mReplyIconYOffset) * scale),
                    (int) (maxScreenWidth - (x - convertToDp(mReplyIconXOffset) * scale)),
                    (int) (y + convertToDp(mReplyIconYOffset) * scale)
            ));
            mReplyIcon.draw(canvas);
        }

        mReplyIconBackground.setAlpha(255);
        mReplyIcon.setAlpha(255);
    }
}
