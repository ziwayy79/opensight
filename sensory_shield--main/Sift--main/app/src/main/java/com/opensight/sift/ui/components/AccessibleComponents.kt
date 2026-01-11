package com.opensight.sift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opensight.sift.ui.theme.SiftAccent
import com.opensight.sift.ui.theme.SiftButtonBackground
import com.opensight.sift.ui.theme.SiftButtonPressed
import com.opensight.sift.ui.theme.SiftText

/**
 * Large accessible button designed for users with visual impairment
 * and those who may experience overstimulation.
 * 
 * Features:
 * - Extra large touch target (minimum 80dp height)
 * - High contrast text
 * - Clear visual feedback on press
 * - Screen reader friendly
 */
@Composable
fun AccessibleButton(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityDescription: String = "$title. $description"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor = if (isPressed) SiftButtonPressed else SiftButtonBackground
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = accessibilityDescription
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Large icon
            Text(
                text = icon,
                fontSize = 48.sp,
                modifier = Modifier.size(64.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Large, bold title
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiftText,
                    lineHeight = 30.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Clear description
                Text(
                    text = description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = SiftText.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                )
            }
            
            // Arrow indicator
            Text(
                text = "→",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SiftAccent
            )
        }
    }
}

/**
 * Large header text for screen titles
 */
@Composable
fun AccessibleHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = SiftText,
            textAlign = TextAlign.Center
        )
        
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = SiftText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
