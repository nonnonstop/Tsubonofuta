package space.aioilight.tsubonofuta

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class NativeAdRemover(private val config: AppConfig, lpParam: XC_LoadPackage.LoadPackageParam) {
    companion object {
        private const val INLINE_AD_CLASS = "jp.syoboi.a2chMate.view.ad.InlineAdContainer"
        private const val HEAD_AD_CLASS = "jp.syoboi.a2chMate.view.MyAdView"
        private const val TARGET_ACTIVITY = "jp.syoboi.a2chMate.activity.ResListActivity"
    }

    private val classLoader = lpParam.classLoader
    private val inlineAdClass = XposedHelpers.findClass(INLINE_AD_CLASS, classLoader)
    private val headAdClass = XposedHelpers.findClass(HEAD_AD_CLASS, classLoader)
    private val targetActivityClass = XposedHelpers.findClass(TARGET_ACTIVITY, classLoader)

    fun register() {
        if (!config[AppConfig.Booleans.HIDE_INLINE_AD] && !config[AppConfig.Booleans.HIDE_THREAD_AD]) {
            XposedBridge.log("Hide no native ad")
            return
        }

        XposedBridge.log("Start NativeAdRemover")
        try {
            XposedHelpers.findAndHookMethod(
                ViewGroup::class.java,
                "onViewAdded",
                View::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val view = param.thisObject as ViewGroup
                            if (!isTargetView(view))
                                return
                            view.removeAllViews()
                            param.result = null
                        } catch (e: Exception) {
                            XposedBridge.log(e)
                        }
                    }
                }
            )

            val methodSetMeasuredDimension = XposedHelpers.findMethodExact(
                View::class.java,
                "setMeasuredDimension",
                Int::class.java,
                Int::class.java
            )
            XposedHelpers.findAndHookMethod(
                FrameLayout::class.java,
                "onMeasure",
                Int::class.java,
                Int::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val view = param.thisObject as ViewGroup
                            if (!isTargetView(view))
                                return
                            methodSetMeasuredDimension.invoke(view, 0, 0)
                            param.result = null
                        } catch (e: Exception) {
                            XposedBridge.log(e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }

    private fun isTargetView(view: View): Boolean {
        if (config[AppConfig.Booleans.HIDE_INLINE_AD] && view::class.java == inlineAdClass) {
            return true
        }
        if (config[AppConfig.Booleans.HIDE_THREAD_AD] && view::class.java == headAdClass) {
            if (view.context::class.java == targetActivityClass) {
                return true
            }
        }
        return false
    }
}