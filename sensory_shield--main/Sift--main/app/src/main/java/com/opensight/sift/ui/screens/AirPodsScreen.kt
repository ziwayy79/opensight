package com.opensight.sift.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opensight.sift.features.sensoryshield.SensoryShieldManager
import com.opensight.sift.ui.components.AccessibleButton
import com.opensight.sift.ui.theme.SiftAccent
import com.opensight.sift.ui.theme.SiftBackground
import com.opensight.sift.ui.theme.SiftButtonBackground
import com.opensight.sift.ui.theme.SiftSuccess
import com.opensight.sift.ui.theme.SiftText

@Composable
fun AirPodsScreen(
    sensoryShieldManager: SensoryShieldManager,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isEnabled by sensoryShieldManager.isEnabled.collectAsState()
    val currentPreset by sensoryShieldManager.currentPreset.collectAsState()
    val highFreqDamping by sensoryShieldManager.highFreqDamping.collectAsState()
    val voiceAmplification by sensoryShieldManager.voiceAmplification.collectAsState()
    val lowFreqDamping by sensoryShieldManager.lowFreqDamping.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SiftBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Back button
            AccessibleBackButton(onClick = onNavigateBack)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Page Title
            Text(
                text = "🎧 AirPods & Sensory Shield",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SiftText,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Manage your audio settings for sensory comfort",
                fontSize = 18.sp,
                color = SiftText.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sensory Shield Card
            SensoryShieldCard(
                isEnabled = isEnabled,
                onToggle = { sensoryShieldManager.setEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preset Selection
            Text(
                text = "Environment Presets",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SiftText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PresetButton(
                label = "🚇 Transit",
                description = "Subway & trains",
                isSelected = currentPreset == SensoryShieldManager.PRESET_TRANSIT,
                onClick = { sensoryShieldManager.setPreset(SensoryShieldManager.PRESET_TRANSIT) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PresetButton(
                label = "🛒 Shopping",
                description = "Malls & crowds",
                isSelected = currentPreset == SensoryShieldManager.PRESET_SHOPPING,
                onClick = { sensoryShieldManager.setPreset(SensoryShieldManager.PRESET_SHOPPING) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PresetButton(
                label = "🏢 Office",
                description = "Work environment",
                isSelected = currentPreset == SensoryShieldManager.PRESET_OFFICE,
                onClick = { sensoryShieldManager.setPreset(SensoryShieldManager.PRESET_OFFICE) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Preset description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SiftButtonBackground)
            ) {
                Text(
                    text = sensoryShieldManager.getPresetDescription(currentPreset),
                    fontSize = 16.sp,
                    color = SiftText.copy(alpha = 0.8f),
                    modifier = Modifier.padding(20.dp),
                    lineHeight = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Fine-tune Settings
            Text(
                text = "Fine-tune Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SiftText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sliders
            SettingSlider(
                label = "High Frequency Reduction",
                description = "Reduce harsh, screeching sounds",
                value = 1f - highFreqDamping,
                onValueChange = { sensoryShieldManager.setCustomSettings(highFreqDamping = 1f - it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingSlider(
                label = "Voice Amplification",
                description = "Enhance human speech clarity",
                value = (voiceAmplification - 0.5f) / 1.5f,
                onValueChange = { sensoryShieldManager.setCustomSettings(voiceAmplification = 0.5f + (it * 1.5f)) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingSlider(
                label = "Low Frequency Reduction",
                description = "Reduce rumbles and bass",
                value = 1f - lowFreqDamping,
                onValueChange = { sensoryShieldManager.setCustomSettings(lowFreqDamping = 1f - it) }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AccessibleBackButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SiftButtonBackground)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SiftAccent
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Back to Home",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SiftText
            )
        }
    }
}

@Composable
private fun SensoryShieldCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) SiftSuccess.copy(alpha = 0.15f) else SiftButtonBackground,
        label = "background"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shield icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) SiftSuccess else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sensory Shield",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiftText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEnabled) "Active - filtering sounds" else "Tap switch to enable",
                    fontSize = 16.sp,
                    color = SiftText.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SiftSuccess,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                ),
                modifier = Modifier.size(width = 60.dp, height = 40.dp)
            )
        }
    }
}

@Composable
private fun PresetButton(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SiftAccent else SiftButtonBackground,
        label = "presetBg"
    )
    val textColor = if (isSelected) Color.White else SiftText
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = description,
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.7f)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "✓",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SiftButtonBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = SiftText
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = SiftText.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${(value * 100).toInt()}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiftAccent
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = SiftAccent,
                    activeTrackColor = SiftAccent,
                    inactiveTrackColor = SiftAccent.copy(alpha = 0.3f)
                )
            )
        }
    }
}
