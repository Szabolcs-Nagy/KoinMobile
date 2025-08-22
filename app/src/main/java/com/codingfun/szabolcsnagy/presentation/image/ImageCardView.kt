package com.codingfun.szabolcsnagy.presentation.image

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.codingfun.szabolcsnagy.constants.model.WikiPhoto
import com.codingfun.szabolcsnagy.constants.model.WikiPhotoDesc

/**
 * Composable for displaying a Wikipedia image with its associated text
 *
 * Displays an [androidx.compose.foundation.Image] composable fetched from a URI with a
 * [androidx.compose.material3.CircularWavyProgressIndicator] while the image is loading, with the
 * image title and description at the bottom of a card.
 *
 * @param photo A (nullable) WikiPhoto object. The image url and aspect ratio are provided by this
 * object
 * @param photoDesc A WikiPhotoDesc object that provides the image title and description
 */

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageCard(
    photo: WikiPhoto?,
    photoDesc: WikiPhotoDesc,
    title: String,
    imageLoader: ImageLoader,
    showPhoto: Boolean,
    background: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelBottomPadding =
        if (photoDesc.description == null) 16.dp
        else 8.dp
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .widthIn(max = 512.dp)
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .animateContentSize(motionScheme.defaultSpatialSpec())
        ) {
            if (photo != null && showPhoto) {
                PageImage(
                    photo = photo,
                    photoDesc = photoDesc,
                    contentScale = ContentScale.Crop,
                    imageLoader = imageLoader,
                    background = background,
                    modifier = Modifier.fillMaxWidth()
                )
            }
                Text(
                    text = photoDesc.label?.get(0) ?: title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = labelBottomPadding)
                        .fillMaxWidth()
                )
            if (photoDesc.description != null) {
                Text(
                    text = photoDesc.description[0],
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}