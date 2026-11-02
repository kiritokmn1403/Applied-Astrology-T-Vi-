package com.appliedastrology.tuvi;

import android.content.res.AssetManager;

/** Thư viện native libaacore.so (biên dịch bằng Android NDK cho arm64-v8a / armeabi-v7a / x86_64). */
public final class NativeCore {
    private static boolean loaded;
    static {
        try { System.loadLibrary("aacore"); loaded = true; }
        catch (Throwable t) { loaded = false; }
    }
    private NativeCore() { }

    public static String abi() { return loaded ? nativeAbi() : "none"; }

    public static String search(AssetManager am, String foldedQuery, int max) {
        if (!loaded) return null;
        return nativeSearch(am, foldedQuery, max);
    }

    private static native String nativeAbi();
    private static native String nativeSearch(AssetManager am, String foldedQuery, int max);
}
