package com.example.androidplayground.ui.apidashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// ── Design tokens ──

private val ScreenBg = Color(0xFFF9F9F9)
private val CardBg = Color(0xFFFFFFFF)
private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val SubtitleColor = Color(0xFF49454F)
private val ErrorColor = Color(0xFFEF5350)
private val CodeBg = Color(0xFF1C1B1F)

// Syntax colors for JSON
private val JsonKey = Color(0xFF82AAFF)       // blue
private val JsonString = Color(0xFFC3E88D)    // green
private val JsonNumber = Color(0xFFFFCB6B)    // orange
private val JsonBool = Color(0xFFC792EA)      // purple
private val JsonNull = Color(0xFFC792EA)
private val JsonPunctuation = Color(0xFF89DDFF) // cyan
private val JsonPlain = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiDashboardScreen(
    navController: NavController,
    viewModel: ApiDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("API Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── URL Input ──
            UrlInputCard(
                url = state.url,
                onUrlChange = viewModel::updateUrl,
                method = state.method,
                onMethodChange = viewModel::updateMethod,
                isLoading = state.isLoading,
                onSend = viewModel::sendRequest
            )

            // ── POST Body (visible only for POST) ──
            AnimatedVisibility(
                visible = state.method == HttpMethod.POST,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                PostBodyCard(
                    body = state.postBody,
                    onBodyChange = viewModel::updatePostBody
                )
            }

            // ── Headers ──
            HeadersCard(
                headers = state.headers,
                expanded = state.showHeaders,
                onToggle = viewModel::toggleHeaders,
                onAdd = viewModel::addHeader,
                onUpdate = viewModel::updateHeader,
                onRemove = viewModel::removeHeader
            )

            // ── Response metadata ──
            if (state.statusCode != null || state.isLoading) {
                ResponseMetaCard(
                    statusCode = state.statusCode,
                    responseTimeMs = state.responseTimeMs,
                    isLoading = state.isLoading
                )
            }

            // ── Error message ──
            if (state.errorMessage != null && !state.isLoading) {
                ErrorCard(message = state.errorMessage!!)
            }

            // ── Response body ──
            if (state.responseBody.isNotEmpty() && !state.isLoading) {
                ResponseCard(
                    body = state.responseBody,
                    isJson = state.isJsonResponse,
                    context = context
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── URL Input Card ──

@Composable
private fun UrlInputCard(
    url: String,
    onUrlChange: (String) -> Unit,
    method: HttpMethod,
    onMethodChange: (HttpMethod) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(20.dp)
    ) {
        // Method chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HttpMethod.entries.forEach { m ->
                FilterChip(
                    selected = method == m,
                    onClick = { onMethodChange(m) },
                    label = {
                        Text(
                            m.name,
                            fontWeight = if (method == m) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGreen.copy(alpha = 0.15f),
                        selectedLabelColor = AccentGreenDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // URL text field
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            placeholder = {
                Text("https://api.example.com/data", color = SubtitleColor.copy(alpha = 0.5f))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                cursorColor = AccentGreen
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Send button
        Button(
            onClick = onSend,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sending...", fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Request", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── POST Body Card ──

@Composable
private fun PostBodyCard(
    body: String,
    onBodyChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(20.dp)
    ) {
        Text(
            "REQUEST BODY",
            style = MaterialTheme.typography.labelMedium.copy(
                color = AccentGreenDark,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = body,
            onValueChange = onBodyChange,
            placeholder = { Text("{\"key\": \"value\"}", color = SubtitleColor.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                cursorColor = AccentGreen
            )
        )
    }
}

// ── Headers Card ──

@Composable
private fun HeadersCard(
    headers: List<HeaderEntry>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    onUpdate: (Int, String, String) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "HEADERS",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AccentGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )
            Row {
                if (expanded) {
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "Add header", tint = AccentGreen, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle headers",
                        tint = SubtitleColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (headers.isEmpty()) {
                    Text(
                        "No custom headers",
                        style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                headers.forEachIndexed { index, header ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = header.key,
                            onValueChange = { onUpdate(index, it, header.value) },
                            placeholder = { Text("Key", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                        OutlinedTextField(
                            value = header.value,
                            onValueChange = { onUpdate(index, header.key, it) },
                            placeholder = { Text("Value", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                        IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = ErrorColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Response Metadata Card ──

@Composable
private fun ResponseMetaCard(
    statusCode: Int?,
    responseTimeMs: Long?,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = AccentGreen,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyMedium.copy(color = SubtitleColor))
            }
        } else {
            // Status code with color coding
            val codeColor = when {
                statusCode == null -> SubtitleColor
                statusCode in 200..299 -> AccentGreenDark
                statusCode in 300..399 -> Color(0xFFFF9800)
                else -> ErrorColor
            }
            Column {
                Text(
                    "STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SubtitleColor,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    "${statusCode ?: "—"}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = codeColor
                    )
                )
            }

            // Response time
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "RESPONSE TIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SubtitleColor,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    "${responseTimeMs ?: "—"} ms",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGreenDark
                    )
                )
            }
        }
    }
}

// ── Error Card ──

@Composable
private fun ErrorCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ErrorColor.copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ErrorColor,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// ── Response Body Card with Syntax Highlighting ──

@Composable
private fun ResponseCard(
    body: String,
    isJson: Boolean,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CodeBg)
            .padding(16.dp)
    ) {
        // Header row with copy button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "RESPONSE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("API Response", body))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Copy",
                    tint = AccentGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable code view
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                val displayText = if (isJson) highlightJson(body) else AnnotatedString(body)
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = JsonPlain
                    )
                )
            }
        }
    }
}

// ── JSON Syntax Highlighter ──

private fun highlightJson(json: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = json.length
        var inString = false
        var isKey = false
        var stringStart = 0

        while (i < len) {
            val c = json[i]

            when {
                // String literal
                c == '"' && (i == 0 || json[i - 1] != '\\') -> {
                    if (!inString) {
                        // Starting a string — determine if it's a key
                        inString = true
                        stringStart = i

                        // Lookahead: is this a key? (followed by ':' after the closing quote)
                        val closeQuote = findClosingQuote(json, i + 1)
                        isKey = if (closeQuote != -1) {
                            val afterQuote = json.substring(closeQuote + 1).trimStart()
                            afterQuote.startsWith(":")
                        } else false

                        withStyle(SpanStyle(color = if (isKey) JsonKey else JsonString)) {
                            append('"')
                        }
                    } else {
                        // Closing quote
                        withStyle(SpanStyle(color = if (isKey) JsonKey else JsonString)) {
                            append('"')
                        }
                        inString = false
                    }
                }

                inString -> {
                    withStyle(SpanStyle(color = if (isKey) JsonKey else JsonString)) {
                        append(c)
                    }
                }

                // Punctuation: { } [ ] , :
                c in "{}[],:".toSet() -> {
                    withStyle(SpanStyle(color = JsonPunctuation)) { append(c) }
                }

                // Numbers
                c.isDigit() || (c == '-' && i + 1 < len && json[i + 1].isDigit()) -> {
                    val numStart = i
                    while (i < len && (json[i].isDigit() || json[i] == '.' || json[i] == '-' || json[i] == 'e' || json[i] == 'E' || json[i] == '+')) {
                        i++
                    }
                    withStyle(SpanStyle(color = JsonNumber)) {
                        append(json.substring(numStart, i))
                    }
                    continue  // i already advanced past the number
                }

                // Boolean / null keywords
                json.startsWith("true", i) -> {
                    withStyle(SpanStyle(color = JsonBool)) { append("true") }
                    i += 4; continue
                }
                json.startsWith("false", i) -> {
                    withStyle(SpanStyle(color = JsonBool)) { append("false") }
                    i += 5; continue
                }
                json.startsWith("null", i) -> {
                    withStyle(SpanStyle(color = JsonNull)) { append("null") }
                    i += 4; continue
                }

                // Whitespace and anything else
                else -> {
                    append(c)
                }
            }
            i++
        }
    }
}

private fun findClosingQuote(json: String, startAfterOpenQuote: Int): Int {
    var i = startAfterOpenQuote
    while (i < json.length) {
        if (json[i] == '"' && json[i - 1] != '\\') return i
        i++
    }
    return -1
}
