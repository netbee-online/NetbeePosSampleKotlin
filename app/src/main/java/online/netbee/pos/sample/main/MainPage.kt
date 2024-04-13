package online.netbee.pos.sample.main

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import online.netbee.pos.sample.PosProvider
import online.netbee.pos.sample.R
import online.netbee.pos.sample.posProviders
import online.netbee.pos.sample.security.KeyManager


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainPage(onSend: (String, PosProvider, String, String) -> Unit) {
    var amount by remember {
        mutableStateOf("2000")
    }

    var payload by remember {
        mutableStateOf("""id=1""")
    }

    var providerExpanded by remember {
        mutableStateOf(false)
    }

    var posProvider by remember {
        mutableStateOf(posProviders.first())
    }

    var netbeePublicKey by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

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
                    onSend(amount, posProvider, payload, netbeePublicKey)
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
            ExposedDropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                expanded = providerExpanded,
                onExpandedChange = {
                    providerExpanded = !providerExpanded
                }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    value = posProvider.displayName,
                    onValueChange = { },
                    label = {
                        Text(
                            text = stringResource(R.string.pos_provider),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = providerExpanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = {
                        providerExpanded = false
                    },
                ) {
                    posProviders.forEach { provider ->
                        DropdownMenuItem(
                            modifier = Modifier,
                            onClick = {
                                posProvider = provider
                                providerExpanded = false
                            },
                            text = {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                        )
                    }
                }
            }

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
