package org.wordpress.android.ui.reader

data class FollowCommentsUiState(
    val type: FollowCommentsUiStateType,
    val showFollowButton: Boolean,
    val isFollowing: Boolean,
    val animate: Boolean,
    val onFollowButtonClick: ((Boolean) -> Unit)?,
    val isReceivingNotifications: Boolean
)

enum class FollowCommentsUiStateType {
    DISABLED,
    LOADING,
    GONE,
    VISIBLE_WITH_STATE
}
