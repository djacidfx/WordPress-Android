package org.wordpress.android.ui.compose.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun UnelevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Card(
        modifier = Modifier.clip(shape).then(modifier),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        ),
        elevation = 0.dp,
        shape = shape,
    ) {
        content()
    }
}
