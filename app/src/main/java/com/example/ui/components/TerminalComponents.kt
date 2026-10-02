package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DimGreen
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.ScanlineOverlay
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalBorderDim
import com.example.ui.theme.TerminalDark
import com.example.ui.theme.TerminalPanelBg
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Scanline overlay that adds CRT terminal horizontal scanning lines.
 */
fun Modifier.terminalScanlines(): Modifier = this.drawWithContent {
    drawContent()
    val scanlineSpacing = 4.dp.toPx()
    val lineCount = (size.height / scanlineSpacing).toInt()
    for (i in 0..lineCount) {
        val y = i * scanlineSpacing
        drawLine(
            color = Color(0x0E00FF41),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }
}

/**
 * Terminal Button:
 * Transparent fill, 1px neon green border, green uppercase text.
 * On press: fill green, text turns black.
 */
@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = NeonGreen,
    testTag: String = "terminal_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = when {
        !enabled -> Color(0x22333333)
        isPressed -> color
        else -> Color.Transparent
    }

    val textColor = when {
        !enabled -> DimGreen
        isPressed -> TerminalBlack
        else -> color
    }

    val borderColor = when {
        !enabled -> DimGreen
        else -> color
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp, minWidth = 120.dp)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
            .background(bg, shape = RoundedCornerShape(2.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.5.sp,
                color = textColor
            )
        )
    }
}

/**
 * Terminal Panel:
 * Black background with thin green border and faint glow.
 */
@Composable
fun TerminalPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = TerminalBorder,
    glow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(TerminalPanelBg, shape = RoundedCornerShape(2.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
            .drawBehind {
                if (glow) {
                    drawRect(
                        color = borderColor.copy(alpha = 0.03f),
                        topLeft = Offset.Zero,
                        size = size
                    )
                }
            }
            .padding(16.dp),
        content = content
    )
}

/**
 * Blinking cursor underscore `_`.
 */
@Composable
fun BlinkingCursor(
    color: Color = NeonGreen,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Text(
        text = "_",
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color.copy(alpha = if (alpha > 0.5f) 1f else 0f)
        ),
        modifier = modifier
    )
}

/**
 * Typewriter text:
 * Types out letter by letter.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    speedMs: Long = 25L,
    style: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = NeonGreen,
        letterSpacing = 0.5.sp
    ),
    onComplete: () -> Unit = {}
) {
    var displayedChars by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        displayedChars = 0
        for (i in 1..text.length) {
            displayedChars = i
            delay(speedMs)
        }
        onComplete()
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.take(displayedChars),
            style = style
        )
        if (displayedChars < text.length) {
            BlinkingCursor(color = style.color)
        }
    }
}

/**
 * Terminal Header:
 * > CIPHERLOCK v1.0
 * > STATUS: SECURE
 */
@Composable
fun TerminalHeader(
    title: String = "CIPHERLOCK v1.0",
    statusText: String = "STATUS: SECURE",
    statusColor: Color = NeonGreen,
    onLockClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalDark)
            .border(width = 1.dp, color = TerminalBorderDim)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "> $title",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp,
                        color = NeonGreen
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "> ",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DimGreen
                        )
                    )
                    Text(
                        text = statusText.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = statusColor
                        )
                    )
                }
            }

            if (onLockClick != null) {
                TerminalButton(
                    text = "[ LOCK ]",
                    onClick = onLockClick,
                    color = NeonRed,
                    modifier = Modifier.height(36.dp),
                    testTag = "quick_lock_button"
                )
            }
        }
    }
}

/**
 * Terminal Input Field:
 * Monospace, uppercase styling, neon green border on focus, with blinking cursor.
 */
@Composable
fun TerminalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    testTag: String = "terminal_input"
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "> ${label.uppercase()}",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.0.sp,
                color = if (isFocused) CyanAccent else DimGreen
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalDark)
                .border(
                    width = 1.dp,
                    color = if (isFocused) NeonGreen else TerminalBorderDim,
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = DimGreen.copy(alpha = 0.5f)
                        )
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = NeonGreen,
                        letterSpacing = 1.0.sp
                    ),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    cursorBrush = SolidColor(NeonGreen)
                )
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

/**
 * Matrix Rain Canvas Animation:
 * Flowing neon green digital characters.
 */
@Composable
fun MatrixRain(
    modifier: Modifier = Modifier,
    alpha: Float = 0.7f
) {
    val characters = remember {
        "01アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンABCDEF0123456789".toList()
    }

    val streamCount = 24
    val columns = remember {
        List(streamCount) {
            MatrixColumn(
                x = it * 1f / streamCount,
                y = Random.nextFloat(),
                speed = 0.005f + Random.nextFloat() * 0.012f,
                length = 10 + Random.nextInt(16)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "matrix_tick")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrixTick"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val charHeight = 22f

        columns.forEach { col ->
            col.y = (col.y + col.speed) % 1.2f
            val startY = col.y * height

            for (i in 0 until col.length) {
                val cy = startY - (i * charHeight)
                if (cy in 0f..height) {
                    val charAlpha = when (i) {
                        0 -> 1f * alpha // Leading bright char
                        1 -> 0.8f * alpha
                        else -> ((col.length - i).toFloat() / col.length) * 0.5f * alpha
                    }
                    val charColor = if (i == 0) CyanAccent else NeonGreen

                    // Render small glowing code points
                    drawCircle(
                        color = charColor.copy(alpha = charAlpha.coerceIn(0f, 1f)),
                        radius = if (i == 0) 3.5f else 2.2f,
                        center = Offset(col.x * width + 10f, cy)
                    )
                }
            }
        }
    }
}

private class MatrixColumn(
    val x: Float,
    var y: Float,
    val speed: Float,
    val length: Int
)

/**
 * Glitch text:
 * Flickers on the logo when the app opens.
 */
@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val glitchOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glitchOffset"
    )

    val isGlitching = glitchOffset in 0.85f..0.92f || glitchOffset in 0.20f..0.24f
    val offsetX = if (isGlitching) (Random.nextInt(6) - 3).dp else 0.dp
    val offsetY = if (isGlitching) (Random.nextInt(4) - 2).dp else 0.dp

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isGlitching) {
            // Cyan split
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize.sp,
                    letterSpacing = 3.sp,
                    color = CyanAccent.copy(alpha = 0.7f)
                ),
                modifier = Modifier.offset(x = offsetX + 2.dp, y = 0.dp)
            )
            // Red split
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize.sp,
                    letterSpacing = 3.sp,
                    color = NeonRed.copy(alpha = 0.7f)
                ),
                modifier = Modifier.offset(x = -offsetX - 2.dp, y = 0.dp)
            )
        }
        // Main text
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                letterSpacing = 3.sp,
                color = NeonGreen
            ),
            modifier = Modifier.offset(x = offsetX, y = offsetY)
        )
    }
}
