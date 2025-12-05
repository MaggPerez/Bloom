package com.example.bloom.aifeatures

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.AIFeatureDataModel
import com.example.bloom.retrofitapi.RetrofitInstance
import com.example.bloom.ui.theme.BloomTheme
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

val PrimaryPurple = Color(0xFF8B5CF6)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatbotScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for text input
    var promptText by remember { mutableStateOf("") }
    
    // State for selected file
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    // State for chat messages
    val messages = remember { mutableStateListOf<ChatMessage>(
        ChatMessage("1", "Hello! I am your AI assistant. How can I help you today?", false)
    ) }

    // File picker launcher for PDF and CSV
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment ?: "document"
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Bloom AI Chatbot",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryPurple
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            // Clear chat and start new conversation
                            messages.clear()
                            messages.add(ChatMessage(UUID.randomUUID().toString(), "Starting a new conversation...", false))
                            selectedFileUri = null
                            selectedFileName = null
                            promptText = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = PrimaryPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Input Area
            Surface(
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Selected File Indicator
                    if (selectedFileName != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(PrimaryPurple.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Attached: $selectedFileName",
                                color = PrimaryPurple,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                            IconButton(
                                onClick = {
                                    selectedFileUri = null
                                    selectedFileName = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove file",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Upload File Button with circular background
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    // Launch file picker for PDF and CSV
                                    launcher.launch(arrayOf("application/pdf", "text/csv"))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Upload File",
                                    tint = PrimaryPurple
                                )
                            }
                        }

                        // Chat Text Field with subtle background
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = {
                                    Text(
                                        "Ask me anything...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                        }

                        // Send Button with circular background
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (promptText.isNotBlank() || selectedFileUri != null)
                                        PrimaryPurple.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                val currentFileUri = selectedFileUri
                                if (currentFileUri != null) {
                                    // Handle File Upload with optional text
                                    val fileName = selectedFileName ?: "file"
                                    val textToSend = promptText
                                    
                                    val displayMessage = if (textToSend.isNotBlank()) {
                                        "Uploaded file: $fileName\n\n$textToSend"
                                    } else {
                                        "Uploaded file: $fileName"
                                    }

                                    //adds user message of file being uploaded to chat and A.I response placeholder of it analyzing the file
                                    messages.add(ChatMessage(UUID.randomUUID().toString(), displayMessage, true))
                                    messages.add(ChatMessage(UUID.randomUUID().toString(), "Analyzing file...", false))
                                    
                                    // Clear inputs
                                    selectedFileUri = null
                                    selectedFileName = null
                                    promptText = ""

                                    scope.launch {
                                        
                                        //copy URI content to temp file
                                        val file = copyUriToTempFile(context, currentFileUri)


//                                      //checks if file conversion was successful
                                        if (file != null) {
                                            try {
                                                val requestFile = file.asRequestBody(context.contentResolver.getType(currentFileUri)?.toMediaTypeOrNull())
                                                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)


                                                //checks if the user provided any text along with the file
                                                val userQuestion = if (textToSend.isNotBlank()) {
                                                    textToSend.toRequestBody("text/plain".toMediaTypeOrNull())
                                                } else {
                                                    null
                                                }


                                                //makes api call to process the file and get the response
                                                val response = RetrofitInstance.instance.processFile(body, userQuestion)
                                                messages.removeAt(messages.lastIndex) // Remove "Analyzing file..."
                                                messages.add(ChatMessage(UUID.randomUUID().toString(), response.message, false))

                                                //display error message if exception occurs during api call
                                            } catch (e: Exception) {
                                                messages.removeAt(messages.lastIndex)
                                                messages.add(ChatMessage(UUID.randomUUID().toString(), "Error analyzing file: ${e.message}", false))
                                            }

                                            //display error if file conversion failed
                                        } else {
                                            messages.removeAt(messages.lastIndex)
                                            messages.add(ChatMessage(UUID.randomUUID().toString(), "Error processing file", false))
                                        }
                                    }

                                    //if no file is selected, but text is present, it will send to backend as text only
                                } else if (promptText.isNotBlank()) {

                                    //stores user message into textToSend variable
                                    val textToSend = promptText

                                    //adds user message to chat
                                    messages.add(ChatMessage(UUID.randomUUID().toString(), textToSend, true))
                                    promptText = ""


                                    //adds placeholder AI response while waiting for backend
                                    messages.add(ChatMessage(UUID.randomUUID().toString(), "Thinking...", false))


                                    //fetching AI response from backend
                                    scope.launch {
                                        try {

                                            //fetching a response from the backend
                                            val request = AIFeatureDataModel.ChatRequest(message = textToSend)
                                            val response = RetrofitInstance.instance.chat(request)

                                            //displaying the AI response in chat
                                            messages.removeAt(messages.lastIndex) // Remove "Thinking..."
                                            messages.add(ChatMessage(UUID.randomUUID().toString(), response.message, false))

                                            //display error message if exception occurs during api call
                                        } catch (e: Exception) {
                                            messages.removeAt(messages.lastIndex)
                                            messages.add(ChatMessage(UUID.randomUUID().toString(), "Error: ${e.message}", false))
                                        }
                                    }
                                }
                            },
                                //send button is disabled if no text or file is selected
                                enabled = promptText.isNotBlank() || selectedFileUri != null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = if (promptText.isNotBlank() || selectedFileUri != null)
                                        PrimaryPurple
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Chat Messages List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color = if (isUser) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun copyUriToTempFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.${getMimeType(context, uri)}")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getMimeType(context: Context, uri: Uri): String {
    val type = context.contentResolver.getType(uri)
    return if (type == "application/pdf") "pdf" else "csv"
}

@Preview
@Composable
fun AiChatbotPreview() {
    BloomTheme {
        AiChatbotScreen(navController = rememberNavController())
    }
}