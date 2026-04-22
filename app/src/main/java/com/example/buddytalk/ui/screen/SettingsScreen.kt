package com.example.buddytalk.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.buddytalk.data.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: UserViewModel, onBack: () -> Unit) {
    val userState by viewModel.user.collectAsState()
    val darkGray = Color(0xFF2D3436)
    val lightBg = Color(0xFFF1F2F6)
    val primaryBlue = Color(0xFF0984E3)

    var nameTextFieldState by remember { mutableStateOf(TextFieldValue("")) }
    var isInitialized by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateAvatar(it.toString())
        }
    }

    LaunchedEffect(userState) {
        userState?.let {
            if (!isInitialized) {
                nameTextFieldState = TextFieldValue(it.userName)
                isInitialized = true
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Cài đặt", 
                        color = darkGray, 
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = darkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(100.dp)
                    .clickable { launcher.launch("image/*") }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(lightBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (userState?.avatarUrl != null) {
                        AsyncImage(
                            model = userState?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null, 
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray
                        )
                    }
                }
                
                Surface(
                    shape = CircleShape,
                    color = primaryBlue,
                    modifier = Modifier.size(32.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null, 
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Text(
                text = "Chạm để đổi ảnh",
                color = primaryBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Thông tin cá nhân",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = nameTextFieldState,
                    onValueChange = { nameTextFieldState = it },
                    label = { Text("Tên người dùng", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null, 
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    textStyle = TextStyle(color = darkGray, fontSize = 16.sp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = lightBg,
                        unfocusedContainerColor = lightBg,
                        disabledContainerColor = lightBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = primaryBlue,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateUserName(nameTextFieldState.text)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Lưu thay đổi", 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}