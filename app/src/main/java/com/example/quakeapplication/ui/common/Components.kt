package com.example.quakeapplication.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.quakeapplication.R
import com.example.quakeapplication.domain.model.DataError

// A coloured circle showing the magnitude, used in the list and on the detail screen
@Composable
fun MagnitudeBadge(magnitude: Double?, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    // Screen readers hear "Magnitude 3.1" rather than a bare number
    val description = magnitude?.let { stringResource(R.string.magnitude_content_description, formatMagnitude(it)) }
        ?: stringResource(R.string.magnitude_unknown_content_description)
    Box(
        modifier = modifier
            .size(size)
            .background(color = magnitudeColor(magnitude), shape = CircleShape)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatMagnitude(magnitude),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = if (size > 56.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
        )
    }
}

// A bar shown when a refresh fails; the saved data stays visible underneath it
@Composable
fun ErrorBanner(
    error: DataError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = error.message(),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                // weight(1f) lets the text take the free space and wrap, leaving room for the buttons
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_dismiss)) }
        }
    }
}
