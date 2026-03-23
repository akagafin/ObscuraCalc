package com.obscuracalc.feature.converter

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.obscuracalc.core.converter.UnitCategory

@Composable
fun rememberConverterController(): ConverterController {
    val context = LocalContext.current.applicationContext
    return remember { ConverterController(context) }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ConverterScreen(
    controller: ConverterController,
    modifier: Modifier = Modifier,
) {
    val state = controller.state
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let(controller::importRates)
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Offline Converter", style = MaterialTheme.typography.headlineSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        controller.categories().forEach { category ->
                            AssistChip(
                                onClick = { controller.setCategory(category) },
                                label = { Text(category.name.replace('_', ' ')) },
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.inputValue,
                        onValueChange = controller::updateInput,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Value") },
                    )
                    UnitSelectorRow(
                        label = "From",
                        selected = state.fromUnit,
                        options = controller.availableUnits(),
                        onSelected = controller::updateFromUnit,
                    )
                    UnitSelectorRow(
                        label = "To",
                        selected = state.toUnit,
                        options = controller.availableUnits(),
                        onSelected = controller::updateToUnit,
                    )
                    Button(onClick = { controller.convert() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Convert")
                    }
                    if (state.outputValue.isNotBlank()) {
                        Text(
                            text = "Result: ${state.outputValue}",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
        }

        if (state.category == UnitCategory.CURRENCY) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = state.selectedBaseCurrency,
                            onValueChange = controller::updateBaseCurrency,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Base currency") },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.rateCodeInput,
                                onValueChange = controller::updateManualRateCode,
                                modifier = Modifier.weight(1f),
                                label = { Text("Code") },
                            )
                            OutlinedTextField(
                                value = state.rateValueInput,
                                onValueChange = controller::updateManualRateValue,
                                modifier = Modifier.weight(1f),
                                label = { Text("Rate") },
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = controller::saveManualRate,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save rate")
                            }
                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(
                                        arrayOf(
                                            "application/json",
                                            "text/csv",
                                            "text/comma-separated-values"
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Import JSON/CSV")
                            }
                        }
                        state.importMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Saved Rates", style = MaterialTheme.typography.titleMedium)
                        if (state.rates.isEmpty()) {
                            Text("No offline rates saved yet.")
                        } else {
                            state.rates.toSortedMap().forEach { (code, rate) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("$code: $rate")
                                    OutlinedButton(onClick = { controller.removeManualRate(code) }) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun UnitSelectorRow(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                AssistChip(
                    onClick = { onSelected(option) },
                    label = { Text(option.uppercase()) },
                )
            }
        }
        if (selected.isNotBlank()) {
            Text("Selected: ${selected.uppercase()}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
