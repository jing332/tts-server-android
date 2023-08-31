package com.github.jing332.tts_server_android.compose.widgets

import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.github.jing332.tts_server_android.R

// https://gist.github.com/tkuenneth/ddf598663f041dc79960cda503d14448?permalink_comment_id=4660486#gistcomment-4660486
@Composable
fun adaptiveIconPainterResource(@DrawableRes id: Int): Painter {
    val res = LocalContext.current.resources
    val theme = LocalContext.current.theme

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // A8
        // Android O supports adaptive icons
        val adaptiveIcon = ResourcesCompat.getDrawable(res, id, theme) as? AdaptiveIconDrawable
        if (adaptiveIcon != null)
            BitmapPainter(adaptiveIcon.toBitmap().asImageBitmap())
        else
            painterResource(id)
    } else
        painterResource(id)
}

@Composable
fun AppLauncherIcon(modifier: Modifier) {
    Image(
        modifier = modifier.clip(CircleShape),
        painter = adaptiveIconPainterResource(R.mipmap.ic_app_launcher_round),
        contentDescription = "LOGO"
    )
//    ResourcesCompat.getDrawable(
//        LocalContext.current.resources,
//        R.mipmap.ic_app_launcher_round, LocalContext.current.theme
//    )?.let { drawable ->
//        val bitmap = Bitmap.createBitmap(
//            drawable.intrinsicWidth, drawable.intrinsicHeight,
//            Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(bitmap)
//        drawable.setBounds(0, 0, canvas.width, canvas.height)
//        drawable.draw(canvas)
//        Image(
//            // painter = painterResource(R.mipmap.ic_launcher),
//            bitmap = bitmap.asImageBitmap(),
//            "LOGO",
//            modifier = modifier
//        )
//    }
}