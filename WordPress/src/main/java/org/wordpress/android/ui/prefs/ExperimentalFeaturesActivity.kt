package org.wordpress.android.ui.prefs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.wordpress.android.R
import org.wordpress.android.ui.compose.theme.AppThemeM3
import org.wordpress.android.ui.compose.unit.Margin
import org.wordpress.android.util.extensions.setContent

val experimentalFeatures = listOf(
    Feature(key = "experimental_block_editor", label = "Experimental block editor"),
    Feature(key = "experimental_block_editor_theme_styles", label = "Experimental block editor styles")
)

data class Feature(
    val enabled: Boolean = false,
    val key: String,
    val label: String
)

class FeatureViewModel : ViewModel() {
    private val _switchStates = MutableStateFlow<Map<String, Feature>>(emptyMap())
    val switchStates: StateFlow<Map<String, Feature>> = _switchStates.asStateFlow()

    init {
        val initialStates = experimentalFeatures.associate { item ->
            item.key to Feature(AppPrefs.getManualFeatureConfig(item.key), item.key, item.label)
        }
        _switchStates.value = initialStates
    }

    fun toggleFeature(key: String, label: String, enabled: Boolean) {
        _switchStates.update { currentStates ->
            currentStates.toMutableMap().apply {
                this[key] = Feature(enabled, key, label)
                AppPrefs.setManualFeatureConfig(enabled, key)
            }
        }
    }
}

@AndroidEntryPoint
class ExperimentalFeaturesActivity : AppCompatActivity() {
    private val viewModel: FeatureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppThemeM3 {
                val features by viewModel.switchStates.collectAsStateWithLifecycle()

                ExperimentalFeaturesScreen(
                    features = features,
                    onFeatureToggled = viewModel::toggleFeature,
                    onNavigateBack = onBackPressedDispatcher::onBackPressed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalFeaturesScreen(
    features: Map<String, Feature>,
    modifier: Modifier = Modifier,
    onFeatureToggled: (key: String, label: String, enabled: Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.experimental_features_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.back)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = modifier.then(Modifier.padding(innerPadding))) {
            Column {
                features.forEach { (key, feature) ->
                    FeatureToggle(
                        key = key,
                        label = feature.label,
                        enabled = feature.enabled,
                        onChange = onFeatureToggled,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureToggle(
    modifier: Modifier = Modifier,
    key: String,
    label: String,
    enabled: Boolean,
    onChange: (String, String, Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(enabled) { onChange(key, label, !enabled) }
            .padding(horizontal = Margin.ExtraLarge.value, vertical = Margin.MediumLarge.value)
    ) {
        Spacer(modifier = Modifier.width(Margin.ExtraLarge.value))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = { newValue ->
                onChange(key, label, newValue)
            },
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExperimentalFeaturesScreenPreview() {
    AppThemeM3 {
        val featuresStatusAlternated = remember {
            experimentalFeatures.mapIndexed { index, feature ->
                feature.key to feature.copy(enabled = index % 2 == 0)
            }.toMap()
        }

        CompositionLocalProvider(LocalContext provides LocalContext.current) {
            ExperimentalFeaturesScreen(features = featuresStatusAlternated, onFeatureToggled = { _, _, _ -> }, onNavigateBack = {})
        }
    }
}
