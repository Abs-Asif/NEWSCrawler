package abdullah.bari.asif.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage as CoilAsyncImage

@Composable
actual fun AsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    if (!url.isNullOrEmpty()) {
        CoilAsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
