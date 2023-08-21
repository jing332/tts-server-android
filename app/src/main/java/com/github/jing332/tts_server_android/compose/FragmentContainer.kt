package com.github.jing332.tts_server_android.compose

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.findFragment

@Composable
fun FragmentContainer(
    modifier: Modifier = Modifier,
    commit: FragmentTransaction.(containerId: Int) -> Unit
) {
    val localView = LocalView.current
    val parentFragment = remember(localView) {
        try {
            localView.findFragment<Fragment>()
        } catch (e: IllegalStateException) {
            // findFragment throws if no parent fragment is found
            null
        }
    }
    val containerId by rememberSaveable {
        mutableIntStateOf(View.generateViewId())
    }
    val container = remember {
        mutableStateOf<FragmentContainerView?>(null)
    }
    val viewBlock: (Context) -> View = remember(localView) {
        { context ->
            FragmentContainerView(context)
                .apply { id = containerId }
                .also {
                    val fragmentManager = parentFragment?.childFragmentManager
                        ?: (context as? FragmentActivity)?.supportFragmentManager
                    fragmentManager?.commit { commit(it.id) }
                    container.value = it
                }
        }
    }
    AndroidView(
        modifier = modifier,
        factory = viewBlock,
        update = {}
    )

    val localContext = LocalContext.current
    DisposableEffect(localView, localContext, container) {
        onDispose {
            val fragmentManager =
                parentFragment?.childFragmentManager
                    ?: (localContext as?
                            FragmentActivity)?.supportFragmentManager
            val existingFragment = fragmentManager?.findFragmentById(container.value?.id ?: 0)
            if (existingFragment != null &&
                !fragmentManager.isStateSaved
            ) {
                fragmentManager.commit {
                    remove(existingFragment)
                }
            }
        }
    }
}