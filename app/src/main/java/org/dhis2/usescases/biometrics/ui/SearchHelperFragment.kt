package org.dhis2.usescases.biometrics.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.dhis2.usescases.biometrics.ui.buttons.RegisterNewPatientButton
import org.dhis2.usescases.biometrics.ui.buttons.SearchWithAttributesButton
import org.dhis2.usescases.biometrics.ui.buttons.SearchWithBiometricsButton
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.listView.SearchHelperModule
import javax.inject.Inject

class SearchHelperFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as SearchTEActivity).searchComponent.plus(
            SearchHelperModule(),
        ).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SearchHelperContent(onAction = { action ->
                    viewModel.onSearchHelperActionSelected(action)
                })
            }
        }
    }
}

@Composable
fun SearchHelperContent(onAction: (action: SequentialSearchAction) -> Unit = { }) {
    Surface(color = Color.White) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SearchWithBiometricsButton( Modifier.fillMaxWidth(),
                    onClick = { onAction(SequentialSearchAction.SearchWithBiometrics) })

                Spacer(modifier = Modifier.height(16.dp))

                SearchWithAttributesButton( Modifier.fillMaxWidth(),
                    onClick = { onAction(SequentialSearchAction.SearchWithAttributes) })

                Spacer(modifier = Modifier.height(64.dp))

                RegisterNewPatientButton(
                    onClick = { onAction(SequentialSearchAction.RegisterNew) })

            }
        }
    }
}

@Preview
@Composable
fun PreviewSearchHelperContent() {
    SearchHelperContent()
}