package com.fakhrirasyids.sample.ui.view.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import com.fakhrirasyids.sample.utils.Constants
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val downloadItems by viewModel.observeAllDownloadItems().collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        DownloadButtonsLayout(viewModel = viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(downloadItems) { item ->
                DownloadItemCard(
                    item = item,
                    onCancelClick = { viewModel.cancelDownloadItem(item.id) }
                )
            }
        }
    }
}

@Composable
fun DownloadButtonsLayout(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        DownloadButton("MP4 File") {
            viewModel.downloadItem(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                path = Constants.getDownloadPath(),
                fileName = "Video_1.mp4"
            )
        }
        DownloadButton("MOV File") {
            viewModel.downloadItem(
                url = "https://file-examples.com/storage/fe504ae8c8672e49a9e2d51/2018/04/file_example_MOV_1920_2_2MB.mov",
                path = Constants.getDownloadPath(),
                fileName = "Video_2.mov"
            )
        }
        DownloadButton("WEBM File") {
            viewModel.downloadItem(
                url = "https://file-examples.com/storage/fe504ae8c8672e49a9e2d51/2020/03/file_example_WEBM_1920_3_7MB.webm",
                path = Constants.getDownloadPath(),
                fileName = "Video_3.webm"
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        DownloadButton("PNG File") {
            viewModel.downloadItem(
                url = "https://sample-videos.com/img/Sample-png-image-200kb.png",
                path = Constants.getDownloadPath(),
                fileName = "Image_1.png"
            )
        }
        DownloadButton("JPG File") {
            viewModel.downloadItem(
                url = "https://sample-videos.com/img/Sample-jpg-image-100kb.jpg",
                path = Constants.getDownloadPath(),
                fileName = "Image_2.jpg"
            )
        }
        DownloadButton("MP3 File") {
            viewModel.downloadItem(
                url = "https://file-examples.com/storage/fe504ae8c8672e49a9e2d51/2017/11/file_example_MP3_1MG.mp3",
                path = Constants.getDownloadPath(),
                fileName = "Music_1.mp3"
            )
        }
    }
}

@Composable
fun DownloadItemCard(item: LeechDownloadEntity, onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "File Name: ${item.fileName}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Path: ${item.filePath}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "URL: ${item.url}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Size: ${item.fileByteSize} bytes",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCancelClick,
            ) {
                Text(text = "Cancel/Delete Download", color = Color.White)
            }
        }
    }
}

@Composable
fun DownloadButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}