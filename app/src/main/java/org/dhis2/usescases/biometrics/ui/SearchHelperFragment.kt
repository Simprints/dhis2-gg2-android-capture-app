package org.dhis2.usescases.biometrics.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.dhis2.R
import org.dhis2.commons.biometrics.gradientButtonColor
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
                SearchWithBiometricsButton(
                    onClick = { onAction(SequentialSearchAction.SearchWithBiometrics) })

                Spacer(modifier = Modifier.height(16.dp))

                SearchWithAttributesButton(
                    onClick = { onAction(SequentialSearchAction.SearchWithAttributes) })

                Spacer(modifier = Modifier.height(64.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New patient?")
                    TextButton(
                        onClick = { onAction(SequentialSearchAction.RegisterNew) }) {
                        Text(
                            "Register",
                            color = Color(0xFF0281cb),
                            style = TextStyle(
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun SearchWithBiometricsButton(
    onClick: () -> Unit = { },
) {
    val modifier = Modifier
        .wrapContentWidth()
        .fillMaxWidth()
        .height(50.dp)

    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = { onClick() },
    ) {
        Box(
            modifier = Modifier
                .background(gradientButtonColor)
                .then(modifier),
            contentAlignment = Alignment.Center,

            ) {
            Row() {
                Text(text = "Search with biometrics", color = Color.White)
            }
        }
    }
}

@Composable
fun SearchWithAttributesButton(
    onClick: () -> Unit = { },
) {
    OutlinedButton(modifier = Modifier.fillMaxWidth().height(50.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF0281cb)
        ),
        onClick = onClick) {
        Text(stringResource(R.string.search_with_attributes), color = Color(0xFF0281cb))
    }
}


@Preview
@Composable
fun PreviewSearchHelperContent() {
    SearchHelperContent()
}