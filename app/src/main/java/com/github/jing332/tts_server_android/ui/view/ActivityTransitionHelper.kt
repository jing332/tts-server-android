package com.github.jing332.tts_server_android.ui.view

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.Window
import com.github.jing332.tts_server_android.R
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

object ActivityTransitionHelper {
    /**
     * startActivity() 的 Activity 初始化
     */
    fun Activity.initSourceTransition(requestFeature: Int = Window.FEATURE_ACTIVITY_TRANSITIONS) {
        window.requestFeature(requestFeature)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
    }

    /**
     * 目标 的 Activity
     */
    fun Activity.initTargetTransition(view: View = findViewById(android.R.id.content)) {
        view.transitionName = getString(R.string.key_activity_shared_container_trans)
        // 附加共享元素回调，以便接收来自 StartActivity 的共享元素
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        // 将此 Activity 的进入和返回转换设置为 MaterialContainerTransform
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(view)
            startContainerColor = Color.TRANSPARENT
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(view)
            endContainerColor = Color.TRANSPARENT
            duration = 250L
        }
    }
}