package vn.hust.socialnetwork.utils.mediafilter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Point;

import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;

import java.util.Set;

import vn.hust.socialnetwork.R;

public class VideoSizeFilter extends Filter {
    private int mMinWidth;
    private int mMinHeight;
    private long mMaxSize;

    public VideoSizeFilter(int mMinWidth, int mMinHeight, long mMaxSize) {
        this.mMinWidth = mMinWidth;
        this.mMinHeight = mMinHeight;
        this.mMaxSize = mMaxSize;
    }

    @Override
    protected Set<MimeType> constraintTypes() {
        return MimeType.ofVideo();
    }

    @Override
    public IncapableCause filter(Context context, Item item) {
        if (!needFiltering(context, item)) {
            return null;
        }
        if (context != null && item != null) {
            if (item.size > this.mMaxSize) {
                String error = context.getString(R.string.error_size_video, this.mMinWidth, String.valueOf((int) PhotoMetadataUtils.getSizeInMB(this.mMaxSize)));
                return new IncapableCause(IncapableCause.DIALOG, error);
            }
        }
        return null;
    }
}
