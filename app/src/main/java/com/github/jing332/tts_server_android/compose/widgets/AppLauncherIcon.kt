package com.github.jing332.tts_server_android.compose.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.github.jing332.tts_server_android.R

@Composable
fun AppLauncherIcon(modifier: Modifier) {
    ResourcesCompat.getDrawable(
        LocalContext.current.resources,
        R.mipmap.ic_app_launcher_round, LocalContext.current.theme
    )?.let { drawable ->
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        Image(
            // painter = painterResource(R.mipmap.ic_launcher),
            bitmap = bitmap.asImageBitmap(),
            "LOGO",
            modifier = modifier
        )
    }
}