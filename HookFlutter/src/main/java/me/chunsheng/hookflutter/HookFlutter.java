package me.chunsheng.hookflutter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;
import java.lang.reflect.Field;

import io.flutter.embedding.engine.loader.FlutterLoader;

/**
 * @description: 提供一个反射工具类，把Tinker下发的libapp.so替换到Flutter初始化流程中
 * @author: weichunsheng
 * @date: 2020/8/10 8:52 AM
 * @version: 1.0
 */
public class HookFlutter {

    private HookFlutter() {
    }

    public static String getLibPath(Context context) {
        String libPath = findLibraryFromTinker(context, "lib" + File.separator + getCpuABI(), "libapp.so");
        if (!TextUtils.isEmpty(libPath) && libPath.equals("libapp.so")) {
            return null;
        }
        return libPath;
    }

    /**
     * 核心反射代码，不同Flutter SDK版本，可能有出入，需要自己根据自己需求修改
     *
     * @param context
     */
    public static void hookFlutterSDKInit(Context context) {

        try {
            String libPath = findLibraryFromTinker(context, "lib" + File.separator + getCpuABI(), "libapp.so");

            FlutterLoader flutterLoader = FlutterLoader.getInstance();

            Field field = FlutterLoader.class.getDeclaredField("aotSharedLibraryName");
            field.setAccessible(true);
            field.set(flutterLoader, libPath);

            TinkerLog.i("HookFlutter", "flutter patch is loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插桩方法
     * 此方法不可修改，否则不会成功
     *
     * @param obj
     */
    public static void hook(Object obj) {
        if (obj instanceof Context) {

            Context context = (Context) obj;
            TinkerLog.i("HookFlutter", "find FlutterMain");

            hookFlutterSDKInit(context);
        } else {

            TinkerLog.i("HookFlutter", "Object: " + obj.getClass().getName());
        }

    }

    public static String findLibraryFromTinker(Context context, String relativePath, String libName) throws UnsatisfiedLinkError {
        final Tinker tinker = Tinker.with(context);

        libName = libName.startsWith("lib") ? libName : "lib" + libName;
        libName = libName.endsWith(".so") ? libName : libName + ".so";
        String relativeLibPath = relativePath + File.separator + libName;

        TinkerLog.i("HookFlutter", "flutterPatchInit() called   " + tinker.isTinkerLoaded() + " " + tinker.isEnabledForNativeLib());

        if (tinker.isEnabledForNativeLib() && tinker.isTinkerLoaded()) {
            TinkerLoadResult loadResult = tinker.getTinkerLoadResultIfPresent();
            if (loadResult.libs == null) {
                return libName;
            }
            for (String name : loadResult.libs.keySet()) {
                if (!name.equals(relativeLibPath)) {
                    continue;
                }
                String patchLibraryPath = loadResult.libraryDirectory + "/" + name;
                File library = new File(patchLibraryPath);
                if (!library.exists()) {
                    continue;
                }

                boolean verifyMd5 = tinker.isTinkerLoadVerify();
                if (verifyMd5 && !SharePatchFileUtil.verifyFileMd5(library, loadResult.libs.get(name))) {
                    tinker.getLoadReporter().onLoadFileMd5Mismatch(library, ShareConstants.TYPE_LIBRARY);
                } else {
                    TinkerLog.i("HookFlutter", "findLibraryFromTinker success:" + patchLibraryPath);
                    return patchLibraryPath;
                }
            }
        }

        return libName;
    }

    /**
     * 获取当前设备abi
     *
     * @return
     */
    public static String getCpuABI() {

        if (Build.VERSION.SDK_INT >= 21) {
            for (String cpu : Build.SUPPORTED_ABIS) {
                if (!TextUtils.isEmpty(cpu)) {
                    TinkerLog.i("HookFlutter", "cpu abi is:" + cpu);
                    return cpu;
                }
            }
        } else {
            TinkerLog.i("HookFlutter", "cpu abi is:" + Build.CPU_ABI);
            return Build.CPU_ABI;
        }

        return "";
    }
}