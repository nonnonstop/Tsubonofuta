package space.aioilight.tsubonofuta

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ModuleMain : IXposedHookLoadPackage {
    companion object {
        private const val PACKAGE_NAME = "jp.co.airfront.android.a2chMate"
    }

    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam?) {
        val packageName = lpParam?.packageName ?: return
        if (packageName != PACKAGE_NAME)
            return

        val config = AppConfig.newInstanceForHookedApp()
        NativeAdRemover(config, lpParam).register()
        VideoAdRemover(config, lpParam).register()
        UserAgentReplacer(config, lpParam).register()
        ApiIdRemover(config, lpParam).register()
    }
}