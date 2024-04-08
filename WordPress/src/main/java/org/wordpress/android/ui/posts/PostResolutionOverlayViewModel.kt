package org.wordpress.android.ui.posts

import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wordpress.android.R
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.util.DateTimeUtilsWrapper
import org.wordpress.android.util.DateUtilsWrapper
import org.wordpress.android.viewmodel.Event
import javax.inject.Inject

class PostResolutionOverlayViewModel @Inject constructor(
    private val dateTimeUtilsWrapper: DateTimeUtilsWrapper,
    private val dateUtilsWrapper: DateUtilsWrapper
) : ViewModel() {
    private val _uiState = MutableLiveData<PostResolutionOverlayUiState>()
    val uiState: LiveData<PostResolutionOverlayUiState> = _uiState

    // todo: This needs to get the real data - not a string
    private val _triggerListeners =  MutableLiveData<Event<String>>()
    val triggerListeners:  MutableLiveData<Event<String>> = _triggerListeners

    // todo: annmarie set an event for the dismiss
//    private val _dismissDialog = SingleLiveEvent<Boolean>()
//    val dismissDialog = _dismissDialog as LiveData<Boolean>

    private var isStarted = false

    fun start(postModel: PostModel?) {
        if (isStarted) return

// todo: annmarie - add an event here that will dismiss the dialog if post is null
        val post = postModel?:return

        // todo: use the post to get the type
        val resolutionType = getPostResolutionConflictType()
        val uiState = when (resolutionType) {
            PostResolutionType.VERSION_SYNC -> getPostResolutionOverlayUiStateForVersionSync(post)
            PostResolutionType.AUTO_SAVE_SYNC -> getPostResolutionOverlayUiStateForAutosaveSync(post)
        }

        _uiState.postValue(uiState)
    }

    private fun getPostResolutionOverlayUiStateForVersionSync(post: PostModel): PostResolutionOverlayUiState {
        return PostResolutionOverlayUiState(
            titleResId = R.string.dialog_post_conflict_title,
            bodyResId = R.string.dialog_post_conflict_body,
            content = buildContentItemsForVersionSync(post),
            actionClick = ::onActionClick,
            cancelClick = ::onCancelClick,
            closeClick = ::onCloseClick,
            onSelected = ::onItemSelected
        )
    }

    private fun getPostResolutionOverlayUiStateForAutosaveSync(post: PostModel): PostResolutionOverlayUiState {
        return PostResolutionOverlayUiState(
            titleResId = R.string.dialog_post_conflict_title,
            bodyResId = R.string.dialog_post_conflict_body,
            content = buildContentItemsForVersionSync(post),
            actionClick = ::onActionClick,
            cancelClick = ::onCancelClick,
            closeClick = ::onCloseClick,
            onSelected = ::onItemSelected
        )
    }

    private fun buildContentItemsForVersionSync(post: PostModel): List<ContentItem> {
        // todo: annmarie - make this reusable and testable - use "Wrappers" not static methods
        val localLastModifiedString =
            if (TextUtils.isEmpty(post.dateLocallyChanged)) post.lastModified else post.dateLocallyChanged
        val remoteLastModifiedString = post.remoteLastModified
        val localLastModifiedAsLong = dateTimeUtilsWrapper.timestampFromIso8601Millis(localLastModifiedString)
        val remoteLastModifiedAsLong = dateTimeUtilsWrapper.timestampFromIso8601Millis(remoteLastModifiedString)

        val flags = (DateUtils.FORMAT_SHOW_TIME or
                DateUtils.FORMAT_SHOW_WEEKDAY or
                DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_ABBREV_RELATIVE)

        val localModifiedDateTime = dateUtilsWrapper.formatDateTime(localLastModifiedAsLong, flags )

        val remoteModifiedDateTime = dateUtilsWrapper.formatDateTime(remoteLastModifiedAsLong, flags )

        return listOf(
            ContentItem(headerResId = R.string.dialog_post_conflict_current_device,
                dateLine = UiString.UiStringText(localModifiedDateTime),
                isSelected = false,
                id = ContentItemType.LOCAL_DEVICE),
            ContentItem(headerResId = R.string.dialog_post_conflict_another_device,
                dateLine = UiString.UiStringText(remoteModifiedDateTime),
                isSelected = false,
                id = ContentItemType.OTHER_DEVICE)
        )
    }
    private fun getPostResolutionConflictType(): PostResolutionType {
        return PostResolutionType.VERSION_SYNC
    }

    // todo: annmarie - do I need another click action? I want to know the difference between the "x", the
    // dialog dismiss (swipe down) and the "cancel" so they can be tracked properly .. all lead to onDismiss
    fun onActionClick() {
        Log.i(javaClass.simpleName, "***=> onActionClick")
        // todo: add logging
        // todo: annmarie figure out how this is going to get executed via the handler
    }

    fun onCloseClick() {
        Log.i(javaClass.simpleName, "***=> onCloseClick")
        // todo: add logging
        onDialogDismissed()
       // todo:  _dismissDialog.value = true
    }

    fun onCancelClick() {
        Log.i(javaClass.simpleName, "***=> onCancelClick")
        // todo: add logging
        onDialogDismissed()
        // todo:  _dismissDialog.value = true
    }

    fun onDialogDismissed() {
        Log.i(javaClass.simpleName, "***=> onDialogDismissed")
        // todo: add logging
    }

    fun onItemSelected(selectedItem: ContentItem) {
        Log.i(javaClass.simpleName, "***=> onItemSelected $selectedItem")
        val selectedState = selectedItem.isSelected

        // Update the isSelected property of the selected item within the content list
        val updatedContent = _uiState.value?.content?.map { contentItem ->
            contentItem.copy(isSelected = selectedState &&  contentItem.id == selectedItem.id )
        } ?: return

        val currentUiState = _uiState.value ?: return // Return if UiState is null
        val updatedUiState = currentUiState.copy(
            selectedContentItem = selectedItem,
            content = updatedContent,
            actionEnabled = selectedState
        )
        Log.i(javaClass.simpleName, "***=> updatedState $updatedUiState")
        _uiState.postValue(updatedUiState)
    }
}

// todo: annmarie - you can move all of these into individual classes if you want
data class PostResolutionOverlayUiState(
    @StringRes val titleResId: Int,
    @StringRes val bodyResId: Int,
    val actionEnabled: Boolean = false,
    val content: List<ContentItem>,
    val selectedContentItem: ContentItem? = null,
    val onSelected: (ContentItem) -> Unit,
    // todo: annmarie - there are four actions that can be taken on the view
    // Tap X to close the dialog
    // Tap Cancel to not do anything and close the dialog
    // Swipe the dialog closed
    // Tap the action button
    // This goes back to how we want to capture tracking events
    // The existing dialogs have three actions: swipe outside to close, positive or negative action
    val closeClick: () -> Unit,
    val cancelClick: () -> Unit,
    val actionClick: () -> Unit
)

data class ContentItem(
    val id: ContentItemType,
    @DrawableRes val iconResId: Int = R.drawable.ic_pages_white_24dp,
    @StringRes val headerResId: Int,
    val dateLine: UiString,
    var isSelected: Boolean,
)

enum class ContentItemType {
    LOCAL_DEVICE,
    OTHER_DEVICE
}
// todo: use this for tracking - could create a separate tracking util
    enum class PostResolutionOverlayAction(
        val analyticsLabel: String
    ) {
        DISMISS(analyticsLabel = "dismiss"), // Swipe closed
        CANCEL(analyticsLabel = "cancel"), // Tapped the cancel action
        CLOSE(analyticsLabel = "close"), // Tapped the close X icon
        CONFIRM(analyticsLabel = "save") // Tapped the Confirm action button
    }

    enum class PostResolutionType {
        VERSION_SYNC,
        AUTO_SAVE_SYNC
        // todo: annmarie there are others - can I figure this out from the post? I probably can, so ignore this in the
    }

