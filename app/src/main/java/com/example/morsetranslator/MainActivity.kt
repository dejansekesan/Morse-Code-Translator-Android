package com.example.morseapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

// ── Colors matching the Python dark theme ──────────────────────────────────
private val BgColor        = Color(0xFF1E1E1E)
private val InputBg        = Color(0xFF252526)
private val FgColor        = Color(0xFFD4D4D4)
private val HighlightColor = Color(0xFFFF8800)
private val AccentBlue     = Color(0xFF007ACC)
private val AccentGreen    = Color(0xFF28A745)
private val AccentYellow   = Color(0xFFFFDA33)
private val AccentRed      = Color(0xFFDC3545)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MorseApp() }
    }
}

// Copy text to system clipboard
private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("morse", text))
}

@Composable
fun MorseApp() {
    val context = LocalContext.current

    // ── SoundPool ─────────────────────────────────────────────────────────
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
    var soundsLoaded by remember { mutableStateOf(false) }
    val dotId  = remember { mutableStateOf(0) }
    val dashId = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        soundPool.setOnLoadCompleteListener { _, _, _ -> soundsLoaded = true }
        dotId.value  = soundPool.load(context, R.raw.dot,  1)
        dashId.value = soundPool.load(context, R.raw.dash, 1)
    }
    DisposableEffect(Unit) { onDispose { soundPool.release() } }

    // ── UI state ──────────────────────────────────────────────────────────
    var mode       by remember { mutableStateOf("text_to_morse") }
    var inputText  by remember { mutableStateOf(TextFieldValue("")) }
    var outputText by remember { mutableStateOf("") }

    // Playback
    var isPlaying    by remember { mutableStateOf(false) }
    var isPaused     by remember { mutableStateOf(false) }
    var playbackPos  by remember { mutableStateOf(0) }
    var highlightIdx by remember { mutableStateOf(-1) }

    var volume by remember { mutableStateOf(0.5f) }
    var speed  by remember { mutableStateOf(0.5f) }
    var gap    by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()
    var playJob: Job? by remember { mutableStateOf(null) }

    // Which string is the morse source
    fun morseSource(): String =
        if (mode == "text_to_morse") outputText else inputText.text

    // Build annotated string with one character highlighted
    fun annotatedMorse(src: String, hlIdx: Int): AnnotatedString = buildAnnotatedString {
        src.forEachIndexed { i, ch ->
            if (i == hlIdx) {
                withStyle(SpanStyle(background = HighlightColor, color = Color.Black)) { append(ch) }
            } else {
                withStyle(SpanStyle(color = FgColor)) { append(ch) }
            }
        }
    }

    // ── Playback coroutine ────────────────────────────────────────────────
    fun startPlayback(startFrom: Int) {
        playJob?.cancel()
        playJob = scope.launch(Dispatchers.IO) {
            isPlaying = true
            val src = morseSource()
            var i = startFrom
            while (i < src.length) {
                ensureActive()
                val ch = src[i]
                withContext(Dispatchers.Main) { highlightIdx = i }
                when (ch) {
                    '.' -> {
                        if (soundsLoaded) soundPool.play(dotId.value, volume, volume, 0, 0, 1f)
                        delay((100 * speed).toLong())
                    }
                    '-' -> {
                        if (soundsLoaded) soundPool.play(dashId.value, volume, volume, 0, 0, 1f)
                        delay((300 * speed).toLong())
                    }
                    ' '  -> delay((300 * speed + gap * 1000).toLong())
                    '/'  -> delay((700 * speed + gap * 1000).toLong())
                    '\n' -> delay((1000 * speed + gap * 1100).toLong())
                }
                delay((100 * speed).toLong())
                i++
                playbackPos = i
            }
            withContext(Dispatchers.Main) {
                highlightIdx = -1
                isPlaying    = false
                isPaused     = false
                playbackPos  = 0
            }
        }
    }

    fun stopPlayback() {
        playJob?.cancel()
        playJob      = null
        isPlaying    = false
        isPaused     = false
        playbackPos  = 0
        highlightIdx = -1
    }

    // ── Tappable highlighted morse text ───────────────────────────────────
    // Shows the morse string with highlight; a tap while paused sets playbackPos.
    @Composable
    fun TappableMorseText(
        src: String,
        hlIdx: Int,
        onTapOffset: (Int) -> Unit
    ) {
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        val annotated = annotatedMorse(src, hlIdx)
        Text(
            text       = annotated,
            fontFamily = FontFamily.Monospace,
            fontSize   = 13.sp,
            onTextLayout = { layoutResult = it },
            modifier   = Modifier.pointerInput(src) {
                detectTapGestures { offset ->
                    layoutResult?.let { lr ->
                        val charOffset = lr.getOffsetForPosition(offset)
                        onTapOffset(charOffset)
                    }
                }
            }
        )
    }

    // ── Layout ────────────────────────────────────────────────────────────
    Surface(modifier = Modifier.fillMaxSize(), color = BgColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            // Mode radio buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(
                    "text_to_morse" to "Text → Morse",
                    "morse_to_text" to "Morse → Text"
                ).forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = mode == value,
                            onClick  = {
                                stopPlayback()
                                mode       = value
                                inputText  = TextFieldValue("")
                                outputText = ""
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor   = AccentBlue,
                                unselectedColor = FgColor
                            )
                        )
                        Text(label, color = FgColor, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Input area ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Input:", color = FgColor, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { copyToClipboard(context, inputText.text) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) { Text("Copy", color = FgColor, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(InputBg)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // In morse_to_text mode during playback, show tappable highlighted morse
                val showInputHighlight = mode == "morse_to_text" && (isPlaying || isPaused)
                if (showInputHighlight) {
                    TappableMorseText(
                        src     = inputText.text,
                        hlIdx   = highlightIdx,
                        onTapOffset = { offset ->
                            // Pause and set resume position to tapped character
                            if (isPlaying) {
                                playJob?.cancel()
                                isPlaying = false
                                isPaused  = true
                            }
                            playbackPos  = offset
                            highlightIdx = offset
                        }
                    )
                } else {
                    BasicTextField(
                        value         = inputText,
                        onValueChange = { inputText = it },
                        textStyle     = TextStyle(
                            color      = FgColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 13.sp
                        ),
                        cursorBrush = SolidColor(FgColor),
                        modifier    = Modifier.fillMaxSize()
                    )
                    if (inputText.text.isEmpty()) {
                        Text(
                            "Enter text here…",
                            color      = Color(0xFF555555),
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Output area ───────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Output:", color = FgColor, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { copyToClipboard(context, outputText) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) { Text("Copy", color = FgColor, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(InputBg)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // text_to_morse: output is the morse — tappable for cursor-resume + highlight
                // morse_to_text: output is decoded plain text — just display it
                if (mode == "text_to_morse") {
                    TappableMorseText(
                        src     = outputText,
                        hlIdx   = highlightIdx,
                        onTapOffset = { offset ->
                            if (isPlaying) {
                                playJob?.cancel()
                                isPlaying = false
                                isPaused  = true
                            }
                            playbackPos  = offset
                            highlightIdx = offset
                        }
                    )
                } else {
                    Text(
                        text       = outputText,
                        color      = FgColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Transport buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        val src = inputText.text
                        outputText = if (mode == "text_to_morse")
                            MorseConverter.textToMorse(src)
                        else
                            MorseConverter.morseToText(src)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("Convert", fontSize = 12.sp) }

                Button(
                    onClick = {
                        when {
                            !isPlaying && !isPaused -> startPlayback(0)
                            isPaused -> {
                                isPaused = false
                                startPlayback(playbackPos)
                            }
                            else -> { /* already playing */ }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) { Text("Play", fontSize = 12.sp) }

                Button(
                    onClick = {
                        if (!isPlaying && !isPaused) return@Button
                        if (isPaused) {
                            isPaused = false
                            startPlayback(playbackPos)
                        } else {
                            playJob?.cancel()
                            isPaused     = true
                            isPlaying    = false
                            highlightIdx = -1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) AccentGreen else AccentYellow
                    )
                ) {
                    Text(
                        if (isPaused) "Resume" else "Pause",
                        color    = if (isPaused) Color.White else Color.Black,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { stopPlayback() },
                    colors  = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) { Text("Stop", fontSize = 12.sp) }
            }

            Spacer(Modifier.height(8.dp))

            SliderRow("Volume",   volume, { volume = it }, 0f..1f)
            SliderRow("Speed (s)", speed, { speed  = it }, 0.1f..2f)
            SliderRow("Gap (s)",   gap,   { gap    = it }, 0f..3f)
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text     = "$label: ${"%.2f".format(value)}",
            color    = FgColor,
            fontSize = 12.sp,
            modifier = Modifier.width(110.dp)
        )
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = valueRange,
            modifier      = Modifier.weight(1f),
            colors        = SliderDefaults.colors(
                thumbColor         = AccentBlue,
                activeTrackColor   = AccentBlue,
                inactiveTrackColor = InputBg
            )
        )
    }
}