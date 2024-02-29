package org.wordpress.android.fluxc.persistence.blaze

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import org.wordpress.android.fluxc.model.blaze.BlazeCampaignModelNew
import org.wordpress.android.fluxc.model.blaze.BlazeCampaignsModelNew
import org.wordpress.android.fluxc.persistence.coverters.BlazeCampaignsDateConverter
import java.util.Date

@Dao
abstract class BlazeCampaignsDao {
    @Transaction
    open suspend fun getCampaignsAndPaginationForSite(siteId: Long): BlazeCampaignsModelNew {
        val campaigns = getCampaigns(siteId)
        val pagination = getCampaignsPagination(siteId)
        return BlazeCampaignsModelNew(
            campaigns = campaigns.map { it.toDomainModel() },
            skipped = pagination?.skipped ?: 0,
            totalItems = pagination?.totalItems ?: 25,
        )
    }

    @Query("SELECT * from BlazeCampaigns WHERE `siteId` = :siteId ORDER BY createdAt DESC")
    abstract fun getCampaigns(siteId: Long): List<BlazeCampaignEntity>

    @Query("SELECT * from BlazeCampaigns WHERE `siteId` = :siteId ORDER BY createdAt DESC")
    abstract fun observeCampaigns(siteId: Long): Flow<List<BlazeCampaignEntity>>

    @Query("SELECT * from BlazeCampaignsPagination WHERE `siteId` = :siteId")
    abstract fun getCampaignsPagination(siteId: Long): BlazeCampaignsPaginationEntity?

    @Query("SELECT * from BlazeCampaigns WHERE `siteId` = :siteId ORDER BY createdAt DESC LIMIT 1")
    abstract fun getMostRecentCampaignForSite(siteId: Long): BlazeCampaignEntity?

    @Query("SELECT * from BlazeCampaigns WHERE `siteId` = :siteId ORDER BY createdAt DESC LIMIT 1")
    abstract fun observeMostRecentCampaignForSite(siteId: Long): Flow<BlazeCampaignEntity?>

    @Transaction
    open suspend fun insertCampaignsAndPageInfoForSite(
        siteId: Long,
        domainModel: BlazeCampaignsModelNew
    ) {
        if (domainModel.skipped == 0) {
            clearBlazeCampaigns(siteId)
            clearBlazeCampaignsPagination(siteId)
        } // Always clear both tables when inserting first set of elements
        insertCampaignsForSite(siteId, domainModel)
        insertCampaignsPaginationForSite(siteId, domainModel)
    }

    private suspend fun insertCampaignsForSite(siteId: Long, domainModel: BlazeCampaignsModelNew) {
        insert(domainModel.campaigns.map { BlazeCampaignEntity.fromDomainModel(siteId, it) })
    }

    private suspend fun insertCampaignsPaginationForSite(
        siteId: Long,
        domainModel: BlazeCampaignsModelNew
    ) {
        insert(BlazeCampaignsPaginationEntity.fromDomainModel(siteId, domainModel))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(campaigns: List<BlazeCampaignEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(campaigns: BlazeCampaignsPaginationEntity)

    @Transaction
    open fun clear(siteId: Long) {
        clearBlazeCampaigns(siteId)
        clearBlazeCampaignsPagination(siteId)
    }

    @Query("DELETE FROM BlazeCampaigns where siteId = :siteId")
    abstract fun clearBlazeCampaigns(siteId: Long)

    @Query("DELETE FROM BlazeCampaignsPagination where siteId = :siteId")
    abstract fun clearBlazeCampaignsPagination(siteId: Long)

    @Entity(
        tableName = "BlazeCampaigns",
        indices = [Index(value = ["siteId"], unique = false)],
        primaryKeys = ["siteId", "campaignId"]
    )
    @TypeConverters(BlazeCampaignsDateConverter::class)
    data class BlazeCampaignEntity(
        val siteId: Long,
        val campaignId: Int,
        val title: String,
        val imageUrl: String?,
        val createdAt: Date,
        val endDate: Date?,
        val uiStatus: String,
        val impressions: Long,
        val clicks: Long,
        val targetUrn: String?,
        val totalBudget: Double,
        val spentBudget: Double
    ) {
        fun toDomainModel() = BlazeCampaignModelNew(
            campaignId = campaignId,
            title = title,
            imageUrl = imageUrl,
            createdAt = createdAt,
            endDate = endDate,
            uiStatus = uiStatus,
            impressions = impressions,
            clicks = clicks,
            targetUrn = targetUrn,
            totalBudget = totalBudget,
            spentBudget = spentBudget
        )

        companion object {
            fun fromDomainModel(
                siteId: Long,
                campaign: BlazeCampaignModelNew
            ) = BlazeCampaignEntity(
                siteId = siteId,
                campaignId = campaign.campaignId,
                title = campaign.title,
                imageUrl = campaign.imageUrl,
                createdAt = campaign.createdAt,
                endDate = campaign.endDate,
                uiStatus = campaign.uiStatus,
                impressions = campaign.impressions,
                clicks = campaign.clicks,
                targetUrn = campaign.targetUrn,
                totalBudget = campaign.totalBudget,
                spentBudget = campaign.spentBudget
            )
        }
    }

    @Entity(
        tableName = "BlazeCampaignsPagination",
        primaryKeys = ["siteId"]
    )
    data class BlazeCampaignsPaginationEntity(
        val siteId: Long,
        val skipped: Int,
        val totalItems: Int
    ) {
        companion object {
            fun fromDomainModel(
                siteId: Long,
                domainModel: BlazeCampaignsModelNew
            ) = BlazeCampaignsPaginationEntity(
                siteId = siteId,
                skipped = domainModel.skipped,
                totalItems = domainModel.totalItems,
            )
        }
    }
}
