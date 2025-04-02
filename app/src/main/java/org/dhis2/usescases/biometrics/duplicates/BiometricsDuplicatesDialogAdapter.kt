package org.dhis2.usescases.biometrics.duplicates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import com.google.android.material.card.MaterialCardView
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemSearchTrackedEntityBinding
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.dhis2.usescases.searchTrackEntity.adapters.SearchAdapterDiffCallback
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

class BiometricsDuplicatesDialogAdapter(
    private val cardMapper: TEICardMapper,
    private val colorUtils: ColorUtils,
    private val onClickListener: (SearchTeiModel) -> Unit
) : PagingDataAdapter<SearchTeiModel, BiometricsDuplicatesDialogHolder>(SearchAdapterDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BiometricsDuplicatesDialogHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemSearchTrackedEntityBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false)
        return BiometricsDuplicatesDialogHolder(binding, colorUtils)
    }

    override fun onBindViewHolder(holder: BiometricsDuplicatesDialogHolder, position: Int) {
        getItem(position)?.let {
            val materialCardView =
                holder.itemView.findViewById<MaterialCardView>(R.id.cardView)
            materialCardView.visibility = View.GONE
            val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
            composeView.setContent {
                val card = cardMapper.map(
                    searchTEIModel = it,
                    onSyncIconClick = null,
                    onCardClick = {
                        onClickListener(getItem(position)!!)
                    },
                    onImageClick = {},
                )
                Column(
                    modifier = Modifier
                        .padding(
                            start = Spacing.Spacing8,
                            end = Spacing.Spacing8,
                            bottom = Spacing.Spacing4,
                        ),
                ) {
                    if (position == 0) {
                        Spacer(modifier = Modifier.size(Spacing.Spacing8))
                    }
                    ListCard(
                        listAvatar = card.avatar,
                        title = ListCardTitleModel(text = card.title),
                        lastUpdated = card.lastUpdated,
                        additionalInfoList = card.additionalInfo,
                        actionButton = card.actionButton,
                        expandLabelText = card.expandLabelText,
                        shrinkLabelText = card.shrinkLabelText,
                        onCardClick = card.onCardCLick,
                    )
                }
            }
            holder.bind(it, {
                getItem(holder.absoluteAdapterPosition)?.toggleAttributeList()
                notifyItemChanged(holder.absoluteAdapterPosition)
            }) { _: String? ->
                //path?.let { onImageClick(path) }
            }
        }
    }
}
