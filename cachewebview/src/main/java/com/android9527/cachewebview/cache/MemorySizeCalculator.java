package com.android9527.cachewebview.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;


/**
 * A calculator that tries to intelligently determine cache sizes for a given device based on some
 * constants and the devices screen density, width, and height.
 */
public final class MemorySizeCalculator {
    private static final String TAG = "MemorySizeCalculator";
    static final int LOW_MEMORY_BYTE_ARRAY_POOL_DIVISOR = 2;

    private final Context context;
    private final int memoryCacheSize;


    MemorySizeCalculator(Context context, ActivityManager activityManager,
                         int targetArrayPoolSize) {
        this.context = context;
        memoryCacheSize =
                isLowMemoryDevice(activityManager)
                        ? targetArrayPoolSize / LOW_MEMORY_BYTE_ARRAY_POOL_DIVISOR
                        : targetArrayPoolSize;


        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                    TAG,
                    "Calculation complete"
                            + ", Calculated memory cache size: "
                            + ", max size: "
                            + ", memoryClass: "
                            + activityManager.getMemoryClass()
                            + ", isLowMemoryDevice: "
                            + isLowMemoryDevice(activityManager));
        }
    }

    /**
     * Returns the recommended memory cache size for the device it is run on in bytes.
     */
    public int getMemoryCacheSize() {
        return memoryCacheSize;
    }

    private String toMb(int bytes) {
        return Formatter.formatFileSize(context, bytes);
    }

    private static boolean isLowMemoryDevice(ActivityManager activityManager) {
        // Explicitly check with an if statement, on some devices both parts of boolean expressions
        // can be evaluated even if we'd normally expect a short circuit.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return activityManager.isLowRamDevice();
        } else {
            return false;
        }
    }

    /**
     * Constructs an {@link com.bumptech.glide.load.engine.cache.MemorySizeCalculator} with reasonable defaults that can be optionally
     * overridden.
     */
    public static final class Builder {
        // 4MB.
        static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;

        private final Context context;

        private ActivityManager activityManager;

        private int arrayPoolSizeBytes = ARRAY_POOL_SIZE_BYTES;

        public Builder(Context context) {
            this.context = context;
            activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }



        /**
         * Sets the size in bytes of the {@link
         * com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool} to use to store temporary
         * arrays while decoding data and returns this builder.
         * <p>
         * <p>This number will be halved on low memory devices that return {@code true} from
         * {@link ActivityManager#isLowRamDevice()}.
         */
        public Builder setArrayPoolSize(int arrayPoolSizeBytes) {
            this.arrayPoolSizeBytes = arrayPoolSizeBytes;
            return this;
        }

        public MemorySizeCalculator build() {
            return new MemorySizeCalculator(context, activityManager,
                    arrayPoolSizeBytes);
        }
    }

}
