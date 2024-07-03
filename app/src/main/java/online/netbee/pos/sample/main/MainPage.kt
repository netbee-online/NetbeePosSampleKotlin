package online.netbee.pos.sample.main

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.UUID
import online.netbee.pos.sample.security.KeyManager


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainPage(onSend: (String, String, String, String) -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var amount by remember {
        mutableStateOf("2000")
    }

    var payload by remember {
        mutableStateOf("""id=1""")
    }

//    var providerExpanded by remember {
//        mutableStateOf(false)
//    }
//
//    var posProvider by remember {
//        mutableStateOf(posProviders.first())
//    }

    var netbeePublicKey by remember {
        val key = sharedPreferences.getString("netbee_public_key", "") ?: ""
        mutableStateOf(key)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "آزمایش نت بی پوز") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(text = "ارسال")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null
                    )
                },
                onClick = {
                    sharedPreferences.edit().apply {
                        putString("netbee_public_key", netbeePublicKey)
                    }.apply()

                    val stanId = UUID.randomUUID().toString()
                    onSend(amount, stanId, payload, netbeePublicKey)
                }
            )
        }
    ) { paddingValues ->
        val clipboardManager = LocalClipboardManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = amount,
                onValueChange = { amount = it },
                label = {
                    Text(text = "مبلغ")
                }
            )

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = payload,
                    onValueChange = { payload = it },
                    label = {
                        Text(text = "payload")
                    }
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = netbeePublicKey,
                    onValueChange = { netbeePublicKey = it },
                    label = {
                        Text(text = "کلید عمومی نت بی")
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            netbeePublicKey = clipboardManager.getText().toString()
                        }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPaste,
                                contentDescription = null
                            )
                        }
                    }
                )

                Text(modifier = Modifier, text = "کلید عمومی شما")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = KeyManager.fakePublicKey,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(KeyManager.fakePublicKey))
                            Toast.makeText(context, "کپی شد", Toast.LENGTH_LONG).show()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
