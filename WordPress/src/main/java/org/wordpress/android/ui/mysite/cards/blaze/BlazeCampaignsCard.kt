package org.wordpress.android.ui.mysite.cards.blaze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.wordpress.android.R
import org.wordpress.android.ui.compose.components.card.DashboardCard
import org.wordpress.android.ui.compose.styles.DashboardCardTypography
import org.wordpress.android.ui.compose.utils.uiStringText
import org.wordpress.android.ui.mysite.MySiteCardAndItem.Card.DashboardCards.DashboardCard.BlazeCard.BlazeCampaignsCardModel
import org.wordpress.android.ui.mysite.MySiteCardAndItem.Card.DashboardCards.DashboardCard.BlazeCard.BlazeCampaignsCardModel.BlazeCampaignsCardItem.BlazeCampaignStats
import org.wordpress.android.ui.utils.UiString

@Composable
@Suppress("FunctionName")
fun BlazeCampaignsCard(
    blazeCampaignCardModel: BlazeCampaignsCardModel,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier, content = {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = uiStringText(uiString = blazeCampaignCardModel.title),
                style = DashboardCardTypography.smallTitle,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )
            val status = blazeCampaignCardModel.campaign.status
            if (status != null) {
                BlazeStatusLabel(
                    status = status,
                )
            }
            CampaignTitleThumbnail(
                campaignTitle = blazeCampaignCardModel.campaign.title,
                featuredImageUrl = blazeCampaignCardModel.campaign.featuredImageUrl,
                modifier = Modifier
                    .fillMaxWidth()
            )
            if (blazeCampaignCardModel.campaign.stats != null) {
                CampaignStats(
                    campaignStats = blazeCampaignCardModel.campaign.stats,
                )
            }
            Text(
                text = uiStringText(uiString = blazeCampaignCardModel.footer.label),
                style = DashboardCardTypography.footerCTA,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    })
}

@Composable
fun CampaignTitleThumbnail(campaignTitle: UiString, featuredImageUrl: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = uiStringText(uiString = campaignTitle),
            style = DashboardCardTypography.subTitle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(
                1f,
                fill = false
            )
        )
        if (featuredImageUrl != null) {
            Spacer(Modifier.width(16.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(featuredImageUrl)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.featured_image_desc),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun CampaignStats(
    campaignStats: BlazeCampaignStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CampaignStat(
            modifier = Modifier
                .weight(1f),
            title = "Impressions",
            value = campaignStats.impressions
        )
        CampaignStat(
            modifier = Modifier
                .weight(1f),
            title = "Clicks",
            value = campaignStats.clicks
        )
    }
}

@Composable
private fun CampaignStat(title: String, value: UiString, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = DashboardCardTypography.detailText,
            textAlign = TextAlign.Start
        )
        Text(
            text = uiStringText(uiString = value),
            style = DashboardCardTypography.largeText,
            textAlign = TextAlign.Start
        )
    }
}
