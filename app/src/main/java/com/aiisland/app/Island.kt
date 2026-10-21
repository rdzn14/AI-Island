package com.aiisland.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Island(phone: Phone) {
    var open by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val mood = moodOf(phone)
    val anim = spring<Dp>(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)
    val w by animateDpAsState(if (open) 300.dp else 132.dp, anim, label = "w")
    val h by animateDpAsState(if (open) 76.dp else 38.dp, anim, label = "h")
    val r by animateDpAsState(if (open) 30.dp else 19.dp, anim, label = "r")

    Box(
        Modifier
            .padding(12.dp)
            .size(w, h)
            .clip(RoundedCornerShape(r))
            .background(Color.Black)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                open = !open
            },
        contentAlignment = if (open) Alignment.Center else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (open) 16.dp else 10.dp)
            Robot(mood, if (open) 48.dp else 28.dp, tilt = phone.tilt)


            if (mood == Mood.SLEEP && !open) {
                Spacer(Modifier.width(6.dp))
                val za by rememberInfiniteTransition(label = "z").animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                    label = "za"
                )
                Text(
                    "zZz",
                    color = Color(0xFF9AA3B2).copy(alpha = za),
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
            if (open) {
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "${phone.battery}%" + if (phone.charging) " · cas" else "",
                        color = Color(0xFF9AA3B2),
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        mood.label,
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
