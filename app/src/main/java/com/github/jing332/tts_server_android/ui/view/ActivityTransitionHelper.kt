package com.github.jing332.tts_server_android.ui.view

import android.app.Activity
import android.view.View
import android.view.Window
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.view.Attributes.colorSurface
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

object ActivityTransitionHelper {
    /**
     * 回退动画
     */
    fun Activity.initExitSharedTransition(requestFeature: Int = Window.FEATURE_ACTIVITY_TRANSITIONS) {
        window.requestFeature(requestFeature)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
    }

    /**
     * 目标 的 Activity
     */
    fun Activity.initEnterSharedTransition(view: View = findViewById(android.R.id.content)) {
        view.transitionName = getString(R.string.key_activity_shared_container_trans)
        // 附加共享元素回调，以便接收来自 StartActivity 的共享元素
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        // 将此 Activity 的进入和返回转换设置为 MaterialContainerTransform
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(view)
            startContainerColor = colorSurface
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(view)
            startContainerColor = colorSurface
            duration = 250L
        }
    }
}