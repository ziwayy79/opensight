package com.opensight.sift.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opensight.sift.R
import com.opensight.sift.ui.components.AccessibleButton
import com.opensight.sift.ui.components.AccessibleHeader
import com.opensight.sift.ui.theme.SiftAccent
import com.opensight.sift.ui.theme.SiftBackground
import com.opensight.sift.ui.theme.SiftText

@Composable
fun HomeScreen(
    onNavigateToAirPods: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SiftBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // App Logo/Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(SiftAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 56.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Title
            AccessibleHeader(
                title = stringResource(R.string.home_title),
                subtitle = stringResource(R.string.home_subtitle)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Main Navigation Buttons
            
            // AirPods & Sensory Shield Button
            AccessibleButton(
                title = stringResource(R.string.airpods_button),
                description = stringResource(R.string.airpods_description),
                icon = "🎧",
                onClick = onNavigateToAirPods,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Website Button
            AccessibleButton(
                title = stringResource(R.string.website_button),
                description = stringResource(R.string.website_description),
                icon = "🌐",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ziwayy79.github.io/opensight/"))
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Footer
            Text(
                text = "Designed for accessibility",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = SiftText.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
