package vn.hust.socialnetwork.ui.mediaviewer;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;
import com.tbruyelle.rxpermissions3.RxPermissions;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import me.saket.flick.ContentSizeProvider2;
import me.saket.flick.FlickCallbacks;
import me.saket.flick.FlickDismissLayout;
import me.saket.flick.FlickGestureListener;
import me.saket.flick.InterceptResult;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaDownloader;

public class MediaViewerActivity extends AppCompatActivity {

    private String media_url;
    private String media_type;

    private AppCompatImageView ivBack, ivActionDownload;
    private Toolbar toolbar;

    private FlickDismissLayout fdlDismiss;
    private PhotoView pvImageViewer;
    private PlayerView pvVideoViewer; // view video
    private ExoPlayer exoPlayer; // controller video
    private AppCompatImageButton btnVolume;
    private boolean isMute;

    private ProgressBar progressBar;

    private LinearLayoutCompat lLoadError;
    private FrameLayout lRootView;
    private Drawable activityBackgroundDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_media_viewer);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            media_url = extras.getString("media_url");
            media_type = extras.getString("media_type");
        }

        // get view
        toolbar = findViewById(R.id.toolbar);
        ivBack = findViewById(R.id.iv_back);
        ivActionDownload = findViewById(R.id.iv_action_download);
        fdlDismiss = findViewById(R.id.fdl_dismiss);
        pvImageViewer = findViewById(R.id.pv_image_viewer);
        pvVideoViewer = findViewById(R.id.pv_video_viewer);
        btnVolume = pvVideoViewer.findViewById(R.id.btn_volume);
        progressBar = findViewById(R.id.progress_bar);
        lRootView = findViewById(R.id.l_root_view);
        lLoadError = findViewById(R.id.l_load_error);

        // init
        isMute = false;
        animateDimmingOnEntry();

        if (!checkValidValueString(media_url) || !checkValidValueString(media_type)) {
            showErrorMediaViewer();
        }
        if (media_type.equals(FileExtension.MEDIA_IMAGE_TYPE)) {
            // load image
            pvVideoViewer.setVisibility(View.GONE);
            loadImage();
        }
        if (media_type.equals(FileExtension.MEDIA_VIDEO_TYPE)) {
            pvImageViewer.setVisibility(View.GONE);
            // start video in lifecycle
            initializePlayer();
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaViewerActivity.this.finish();
            }
        });

        // download media
        ivActionDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxPermissions rxPermissions = new RxPermissions(MediaViewerActivity.this);
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                if (checkValidValueString(media_url)) {
                                    new MediaDownloader(MediaViewerActivity.this).download(media_url);
                                }
                            } else {
                                Toast.makeText(MediaViewerActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // when display image, single click to view/hide toolbar (Note: before fdlDismiss.setGestureListener())
        pvImageViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setVisibility(toolbar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        // drag drop to close activity
        fdlDismiss.setGestureListener(flickGestureListener());
    }

    /**
     * dimming background when scroll media
     */
    private void animateDimmingOnEntry() {
        activityBackgroundDrawable = lRootView.getBackground().mutate();
        lRootView.setBackground(activityBackgroundDrawable);

        ValueAnimator animator = ObjectAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(200);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                updateBackgroundDimmingAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void updateBackgroundDimmingAlpha(@FloatRange(from = 0.0, to = 1.0) float moveRatio) {
        // Increase dimming exponentially so that the background is
        // fully transparent while the image has been moved by half.
        float dimming = 1.0f - Math.min(1.0f, moveRatio * 2);
        if (activityBackgroundDrawable != null) {
            activityBackgroundDrawable.setAlpha((int) (dimming * 255));
        }
    }

    /**
     * create a FlickGestureListener for drag and drop view to finish()
     */
    private FlickGestureListener flickGestureListener() {
        ContentSizeProvider2 contentSizeProvider = new ContentSizeProvider2(new Function0<Integer>() {
            @Override
            public Integer invoke() {
                // default
                int def = getResources().getDimensionPixelSize(R.dimen.media_viewer_image_height_when_empty);
                int val = pvImageViewer.getMeasuredHeight();
                return Math.max(def, val);
            }
        });

        FlickCallbacks flickCallbacks = new FlickCallbacks() {
            @Override
            public void onMove(float moveRatio) {
                updateBackgroundDimmingAlpha(moveRatio);
            }

            @Override
            public void onFlickDismiss(long l) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MediaViewerActivity.this.finish();
                    }
                }, l);
            }
        };

        FlickGestureListener gestureListener = new FlickGestureListener(MediaViewerActivity.this, contentSizeProvider, flickCallbacks, false);

        // Block flick gestures if the image can pan further.
        gestureListener.setGestureInterceptor(new Function1<Float, InterceptResult>() {
            @Override
            public InterceptResult invoke(Float scrollY) {
                int directionInt = 1;
                if (scrollY < 0.0f) {
                    directionInt = -1;
                }
                boolean canPanFurther = false;
                if (pvImageViewer != null) {
                    canPanFurther = pvImageViewer.canScrollVertically(directionInt);
                }
                if (canPanFurther) {
                    return InterceptResult.INTERCEPTED;
                }
                return InterceptResult.IGNORED;
            }
        });

        return gestureListener;
    }

    /**
     * load image
     */
    private void loadImage() {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(MediaViewerActivity.this)
                .asBitmap()
                .load(media_url)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        showErrorMediaViewer();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(pvImageViewer);
        pvImageViewer.setMaximumScale(3.0f);
    }

    /**
     * show error when loading media error
     */
    private void showErrorMediaViewer() {
        ivActionDownload.setClickable(false);
        ivActionDownload.setVisibility(View.GONE);
        pvImageViewer.setVisibility(View.GONE);
        pvVideoViewer.setVisibility(View.GONE);
        lLoadError.setVisibility(View.VISIBLE);
    }

    /**
     * release the player
     */
    private void releasePlayer() {
        if (exoPlayer != null) {
            pvVideoViewer.setPlayer(null);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    /**
     * initialize the player
     */
    private void initializePlayer() {
        // Set up the factory for media sources.
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MediaViewerActivity.this);

        MediaSource.Factory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);

        // Create an ExoPlayer and set it as the player for content.
        exoPlayer = new ExoPlayer.Builder(MediaViewerActivity.this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();
        pvVideoViewer.setPlayer(exoPlayer);

        // Create the MediaItem to play, specifying the content URI.
        Uri contentUri = Uri.parse(media_url);
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(contentUri)
                .build();

        // Prepare the content and ad to be played with the ExoPlayer.
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();

        // Set PlayWhenReady. If true, content will autoplay.
        exoPlayer.setPlayWhenReady(true);

        // handing error
        exoPlayer.addListener(new PlayerEventListener());

        btnVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMute = !isMute;
                if (isMute) {
                    exoPlayer.setVolume(0.0f);
                    btnVolume.setImageResource(R.drawable.ic_volume_off);
                } else {
                    exoPlayer.setVolume(1.0f);
                    btnVolume.setImageResource(R.drawable.ic_volume_on);
                }
            }
        });
    }

    /**
     * Listener for handing error when playing video
     */
    private class PlayerEventListener implements Player.Listener {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                pvVideoViewer.setKeepScreenOn(false);
            } else { // STATE_READY || STATE_BUFFERING
                pvVideoViewer.setKeepScreenOn(true);
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            releasePlayer();
            showErrorMediaViewer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (media_type.equals(FileExtension.MEDIA_VIDEO_TYPE)) {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.getPlayWhenReady();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (media_type.equals(FileExtension.MEDIA_VIDEO_TYPE)) {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.getPlayWhenReady();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (media_type.equals(FileExtension.MEDIA_VIDEO_TYPE)) {
            if (pvVideoViewer != null) {
                pvVideoViewer.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}