package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.mipmap.ic_launcher_nagram, R.mipmap.icon_background_nagram, R.string.AppIconDefault),
        GOOGLE("GoogleIcon", R.mipmap.icon_background_google, R.mipmap.icon_foreground_google, R.string.AppIconGoogle),
        NEON("NeonIcon", R.mipmap.icon_background_neon, R.mipmap.icon_foreground_neon, R.string.AppIconNeon),
        NOX("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground_sa, R.string.AppIconPremium),
        AMOLED("AmoledIcon", R.mipmap.ic_launcher_nagram_amoled, 0, R.string.AppIconAmoled),
        AQUA("AquaIcon", R.mipmap.ic_launcher_nagram_aqua, 0, R.string.AppIconAqua),
        CLASTER("ClasterIcon", R.mipmap.ic_launcher_nagram_claster, 0, R.string.AppIconClaster),
        GREY("GreyIcon", R.mipmap.ic_launcher_nagram_grey, 0, R.string.AppIconGrey),
        NEON_PREMIUM("NeonPremiumIcon", R.mipmap.ic_launcher_nagram_neon_premium, 0, R.string.AppIconNeonPremium),
        PIXEL("PixelIcon", R.mipmap.ic_launcher_nagram_pixel, 0, R.string.AppIconPixel),
        VIOLET("VioletIcon", R.mipmap.ic_launcher_nagram_violet, 0, R.string.AppIconViolet);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }

        public boolean isNekoX() {
            return this != GOOGLE && this != NEON && this != NOX;
        }
    }
}
