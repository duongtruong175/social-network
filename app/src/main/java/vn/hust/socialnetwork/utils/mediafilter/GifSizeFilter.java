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

public class GifSizeFilter extends Filter {
    private int mMinWidth;
    private int mMinHeight;
    private long mMaxSize;

    public GifSizeFilter(int mMinWidth, int mMinHeight, long mMaxSize) {
        this.mMinWidth = mMinWidth;
        this.mMinHeight = mMinHeight;
        this.mMaxSize = mMaxSize;
    }

    @Override
    protected Set<MimeType> constraintTypes() {
        return MimeType.ofGif();
    }

    @Override
    public IncapableCause filter(Context context, Item item) {
        if (!needFiltering(context, item)) {
            return null;
        }
        ContentResolver contentResolver;
        if (context == null) {
            contentResolver = null;
        } else {
            contentResolver = context.getContentResolver();
        }
        Point bitmapBound = PhotoMetadataUtils.getBitmapBound(contentResolver, item == null ? null : item.getContentUri());
        if (context != null && item != null) {
            if (bitmapBound.x < this.mMinWidth || bitmapBound.y < this.mMinHeight || item.size > this.mMaxSize) {
                String error = context.getString(R.string.error_size_gif_image, this.mMinWidth, String.valueOf((int) PhotoMetadataUtils.getSizeInMB(this.mMaxSize)));
                return new IncapableCause(IncapableCause.DIALOG, error);
            }
        }
        return null;
    }
}
