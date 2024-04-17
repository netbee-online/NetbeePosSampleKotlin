package online.netbee.pos.sample.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import online.netbee.pos.sample.PosProvider
import online.netbee.pos.sample.R
import online.netbee.pos.sample.posProviders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosProvider(
    posProvider: PosProvider,
    onProviderChange: (PosProvider) -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
) {
    ExposedDropdownMenuBox(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        expanded = expanded,
        onExpandedChange = {
            onExpandChange(!expanded)
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
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandChange(false)
            },
        ) {
            posProviders.forEach { provider ->
                DropdownMenuItem(
                    modifier = Modifier,
                    onClick = {
                        onProviderChange(provider)
                        onExpandChange(false)
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
}