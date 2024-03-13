package org.wordpress.android.util.analytics

import org.junit.Assert
import org.junit.Test
import org.wordpress.android.analytics.AnalyticsTracker
import java.util.Locale

class AnalyticsTrackerNosaraTest {
    @Test
    fun testEventWithStandardNames() {
        AnalyticsTracker.Stat.values().filter { !specialNames.keys.contains(it) }.forEach {
            val eventName = it.eventName
            val expectedName = it.name.lowercase(Locale.US)
            Assert.assertEquals(eventName, expectedName)
        }
    }

    @Test
    fun testEventWithSpecialNames() {
        AnalyticsTracker.Stat.values().filter { specialNames.keys.contains(it) }.forEach {
            val eventName = it.eventName
            val expectedName = specialNames[it]
            Assert.assertEquals(eventName, expectedName)
        }
    }

    @Suppress("MaxLineLength")
    private val specialNames = mapOf(
        AnalyticsTracker.Stat.READER_ARTICLE_COMMENT_REPLIED_TO to "reader_article_commented_on",
        AnalyticsTracker.Stat.READER_BLOG_FOLLOWED to "reader_site_followed",
        AnalyticsTracker.Stat.READER_BLOG_UNFOLLOWED to "reader_site_unfollowed",
        AnalyticsTracker.Stat.READER_INFINITE_SCROLL to "reader_infinite_scroll_performed",
        AnalyticsTracker.Stat.READER_TAG_FOLLOWED to "reader_reader_tag_followed",
        AnalyticsTracker.Stat.READER_TAG_UNFOLLOWED to "reader_reader_tag_unfollowed",
        AnalyticsTracker.Stat.READER_SEARCH_RESULT_TAPPED to "reader_searchcard_clicked",
        AnalyticsTracker.Stat.READER_GLOBAL_RELATED_POST_CLICKED to "reader_related_post_from_other_site_clicked",
        AnalyticsTracker.Stat.READER_LOCAL_RELATED_POST_CLICKED to "reader_related_post_from_same_site_clicked",
        AnalyticsTracker.Stat.READER_POST_SAVED_FROM_OTHER_POST_LIST to "reader_post_saved",
        AnalyticsTracker.Stat.READER_POST_SAVED_FROM_SAVED_POST_LIST to "reader_post_saved",
        AnalyticsTracker.Stat.READER_POST_SAVED_FROM_DETAILS to "reader_post_saved",
        AnalyticsTracker.Stat.READER_POST_UNSAVED_FROM_OTHER_POST_LIST to "reader_post_unsaved",
        AnalyticsTracker.Stat.READER_POST_UNSAVED_FROM_SAVED_POST_LIST to "reader_post_unsaved",
        AnalyticsTracker.Stat.READER_POST_UNSAVED_FROM_DETAILS to "reader_post_unsaved",
        AnalyticsTracker.Stat.READER_SAVED_POST_OPENED_FROM_SAVED_POST_LIST to "reader_saved_post_opened",
        AnalyticsTracker.Stat.READER_SAVED_POST_OPENED_FROM_OTHER_POST_LIST to "reader_saved_post_opened",
        AnalyticsTracker.Stat.STATS_PERIOD_DAYS_ACCESSED to "stats_period_accessed",
        AnalyticsTracker.Stat.STATS_PERIOD_WEEKS_ACCESSED to "stats_period_accessed",
        AnalyticsTracker.Stat.STATS_PERIOD_MONTHS_ACCESSED to "stats_period_accessed",
        AnalyticsTracker.Stat.STATS_PERIOD_YEARS_ACCESSED to "stats_period_accessed",
        AnalyticsTracker.Stat.STATS_TAPPED_BAR_CHART to "stats_bar_chart_tapped",
        AnalyticsTracker.Stat.EDITOR_CREATED_POST to "editor_post_created",
        AnalyticsTracker.Stat.EDITOR_ADDED_PHOTO_VIA_DEVICE_LIBRARY to "editor_photo_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_VIDEO_VIA_DEVICE_LIBRARY to "editor_video_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_PHOTO_VIA_MEDIA_EDITOR to "editor_photo_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_PHOTO_NEW to "editor_photo_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_VIDEO_NEW to "editor_video_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_PHOTO_VIA_WP_MEDIA_LIBRARY to "editor_photo_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_VIDEO_VIA_WP_MEDIA_LIBRARY to "editor_video_added",
        AnalyticsTracker.Stat.EDITOR_ADDED_PHOTO_VIA_STOCK_MEDIA_LIBRARY to "editor_photo_added",
        AnalyticsTracker.Stat.MEDIA_PICKER_OPEN_CAPTURE_MEDIA to "media_picker_capture_media_opened",
        AnalyticsTracker.Stat.MEDIA_PICKER_OPEN_DEVICE_LIBRARY to "media_picker_device_library_opened",
        AnalyticsTracker.Stat.MEDIA_PICKER_OPEN_WP_MEDIA to "media_picker_wordpress_library_opened",
        AnalyticsTracker.Stat.EDITOR_UPDATED_POST to "editor_post_updated",
        AnalyticsTracker.Stat.EDITOR_SCHEDULED_POST to "editor_post_scheduled",
        AnalyticsTracker.Stat.EDITOR_PUBLISHED_POST to "editor_post_published",
        AnalyticsTracker.Stat.EDITOR_SAVED_DRAFT to "editor_draft_saved",
        AnalyticsTracker.Stat.EDITOR_EDITED_IMAGE to "editor_image_edited",
        AnalyticsTracker.Stat.EDITOR_TAPPED_BLOCKQUOTE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_BOLD to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_ELLIPSIS_COLLAPSE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_ELLIPSIS_EXPAND to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING_1 to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING_2 to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING_3 to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING_4 to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING_5 to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HEADING_6 to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HTML to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_HORIZONTAL_RULE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_IMAGE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_ITALIC to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_LINK_ADDED to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_LIST to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_LIST_ORDERED to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_LIST_UNORDERED to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_NEXT_PAGE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_PARAGRAPH to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_PREFORMAT to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_READ_MORE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_STRIKETHROUGH to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_UNDERLINE to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_ALIGN_LEFT to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_ALIGN_CENTER to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_ALIGN_RIGHT to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_REDO to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_TAPPED_UNDO to "editor_button_tapped",
        AnalyticsTracker.Stat.EDITOR_GUTENBERG_ENABLED to "gutenberg_enabled",
        AnalyticsTracker.Stat.EDITOR_GUTENBERG_DISABLED to "gutenberg_disabled",
        AnalyticsTracker.Stat.REVISIONS_DETAIL_VIEWED_FROM_LIST to "revisions_detail_viewed",
        AnalyticsTracker.Stat.REVISIONS_DETAIL_VIEWED_FROM_SWIPE to "revisions_detail_viewed",
        AnalyticsTracker.Stat.REVISIONS_DETAIL_VIEWED_FROM_CHEVRON to "revisions_detail_viewed",
        AnalyticsTracker.Stat.ME_ACCESSED to "me_tab_accessed",
        AnalyticsTracker.Stat.MY_SITE_ACCESSED to "my_site_tab_accessed",
        AnalyticsTracker.Stat.NOTIFICATIONS_OPENED_NOTIFICATION_DETAILS to "notifications_notification_details_opened",
        AnalyticsTracker.Stat.NOTIFICATION_REPLIED_TO to "notifications_replied_to",
        AnalyticsTracker.Stat.NOTIFICATION_QUICK_ACTIONS_REPLIED_TO to "notifications_replied_to",
        AnalyticsTracker.Stat.NOTIFICATION_APPROVED to "notifications_approved",
        AnalyticsTracker.Stat.NOTIFICATION_QUICK_ACTIONS_APPROVED to "notifications_approved",
        AnalyticsTracker.Stat.NOTIFICATION_UNAPPROVED to "notifications_unapproved",
        AnalyticsTracker.Stat.NOTIFICATION_LIKED to "notifications_comment_liked",
        AnalyticsTracker.Stat.NOTIFICATION_QUICK_ACTIONS_LIKED to "notifications_comment_liked",
        AnalyticsTracker.Stat.NOTIFICATION_QUICK_ACTIONS_QUICKACTION_TOUCHED to "quick_action_touched",
        AnalyticsTracker.Stat.NOTIFICATION_UNLIKED to "notifications_comment_unliked",
        AnalyticsTracker.Stat.NOTIFICATION_TRASHED to "notifications_trashed",
        AnalyticsTracker.Stat.NOTIFICATION_FLAGGED_AS_SPAM to "notifications_flagged_as_spam",
        AnalyticsTracker.Stat.NOTIFICATION_SWIPE_PAGE_CHANGED to "notifications_swipe_page_changed",
        AnalyticsTracker.Stat.NOTIFICATION_PENDING_DRAFTS_TAPPED to "notifications_pending_drafts_tapped",
        AnalyticsTracker.Stat.NOTIFICATION_PENDING_DRAFTS_IGNORED to "notifications_pending_drafts_ignored",
        AnalyticsTracker.Stat.NOTIFICATION_PENDING_DRAFTS_DISMISSED to "notifications_pending_drafts_dismissed",
        AnalyticsTracker.Stat.NOTIFICATION_PENDING_DRAFTS_SETTINGS_ENABLED to "notifications_pending_drafts_settings_enabled",
        AnalyticsTracker.Stat.NOTIFICATION_PENDING_DRAFTS_SETTINGS_DISABLED to "notifications_pending_drafts_settings_disabled",
        AnalyticsTracker.Stat.NOTIFICATION_UPLOAD_MEDIA_SUCCESS_WRITE_POST to "notifications_upload_media_success_write_post",
        AnalyticsTracker.Stat.NOTIFICATION_UPLOAD_POST_ERROR_RETRY to "notifications_upload_post_error_retry",
        AnalyticsTracker.Stat.NOTIFICATION_UPLOAD_MEDIA_ERROR_RETRY to "notifications_upload_media_error_retry",
        AnalyticsTracker.Stat.NOTIFICATION_RECEIVED_PROCESSING_START to "notifications_received_processing_start",
        AnalyticsTracker.Stat.NOTIFICATION_RECEIVED_PROCESSING_END to "notifications_received_processing_end",
        AnalyticsTracker.Stat.OPENED_POSTS to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_PAGES to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_PAGE_PARENT to "page_parent_opened",
        AnalyticsTracker.Stat.OPENED_COMMENTS to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_VIEW_SITE to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_VIEW_SITE_FROM_HEADER to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_VIEW_ADMIN to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_MEDIA_LIBRARY to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_BLOG_SETTINGS to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_ACCOUNT_SETTINGS to "account_settings_opened",
        AnalyticsTracker.Stat.OPENED_APP_SETTINGS to "app_settings_opened",
        AnalyticsTracker.Stat.OPENED_MY_PROFILE to "my_profile_opened",
        AnalyticsTracker.Stat.OPENED_PEOPLE_MANAGEMENT to "people_management_list_opened",
        AnalyticsTracker.Stat.OPENED_PERSON to "people_management_details_opened",
        AnalyticsTracker.Stat.OPENED_PLUGIN_DIRECTORY to "plugin_directory_opened",
        AnalyticsTracker.Stat.OPENED_PLANS to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_PLANS_COMPARISON to "plans_compare",
        AnalyticsTracker.Stat.OPENED_SHARING_MANAGEMENT to "site_menu_opened",
        AnalyticsTracker.Stat.OPENED_SHARING_BUTTON_MANAGEMENT to "sharing_buttons_opened",
        AnalyticsTracker.Stat.ACTIVITY_LOG_FILTER_BAR_DATE_RANGE_BUTTON_TAPPED to "activitylog_filterbar_range_button_tapped",
        AnalyticsTracker.Stat.ACTIVITY_LOG_FILTER_BAR_ACTIVITY_TYPE_BUTTON_TAPPED to "activitylog_filterbar_type_button_tapped",
        AnalyticsTracker.Stat.ACTIVITY_LOG_FILTER_BAR_DATE_RANGE_SELECTED to "activitylog_filterbar_select_range",
        AnalyticsTracker.Stat.ACTIVITY_LOG_FILTER_BAR_ACTIVITY_TYPE_SELECTED to "activitylog_filterbar_select_type",
        AnalyticsTracker.Stat.ACTIVITY_LOG_FILTER_BAR_DATE_RANGE_RESET to "activitylog_filterbar_reset_range",
        AnalyticsTracker.Stat.ACTIVITY_LOG_FILTER_BAR_ACTIVITY_TYPE_RESET to "activitylog_filterbar_reset_type",
        AnalyticsTracker.Stat.JETPACK_BACKUP_FILTER_BAR_DATE_RANGE_BUTTON_TAPPED to "jetpack_backup_filterbar_range_button_tapped",
        AnalyticsTracker.Stat.JETPACK_BACKUP_FILTER_BAR_DATE_RANGE_SELECTED to "jetpack_backup_filterbar_select_range",
        AnalyticsTracker.Stat.JETPACK_BACKUP_FILTER_BAR_DATE_RANGE_RESET to "jetpack_backup_filterbar_reset_range",
        AnalyticsTracker.Stat.JETPACK_SCAN_IGNORE_THREAT_DIALOG_OPEN to "jetpack_scan_ignorethreat_dialogopen",
        AnalyticsTracker.Stat.JETPACK_SCAN_FIX_THREAT_DIALOG_OPEN to "jetpack_scan_fixthreat_dialogopen",
        AnalyticsTracker.Stat.JETPACK_SCAN_ALL_THREATS_OPEN to "jetpack_scan_allthreats_open",
        AnalyticsTracker.Stat.JETPACK_SCAN_ALL_THREATS_FIX_TAPPED to "jetpack_scan_allthreats_fix_tapped",
        AnalyticsTracker.Stat.OPENED_PLUGIN_LIST to "plugin_list_opened",
        AnalyticsTracker.Stat.OPENED_PLUGIN_DETAIL to "plugin_detail_opened",
        AnalyticsTracker.Stat.CREATE_ACCOUNT_INITIATED to "account_create_initiated",
        AnalyticsTracker.Stat.CREATE_ACCOUNT_EMAIL_EXISTS to "account_create_email_exists",
        AnalyticsTracker.Stat.CREATE_ACCOUNT_USERNAME_EXISTS to "account_create_username_exists",
        AnalyticsTracker.Stat.CREATE_ACCOUNT_FAILED to "account_create_failed",
        AnalyticsTracker.Stat.CREATED_ACCOUNT to "account_created",
        AnalyticsTracker.Stat.SHARED_ITEM_READER to "item_shared_reader",
        AnalyticsTracker.Stat.ADDED_SELF_HOSTED_SITE to "self_hosted_blog_added",
        AnalyticsTracker.Stat.INSTALL_JETPACK_CANCELLED to "install_jetpack_canceled",
        AnalyticsTracker.Stat.PUSH_NOTIFICATION_TAPPED to "push_notification_alert_tapped",
        AnalyticsTracker.Stat.LOGIN_FAILED to "login_failed_to_login",
        AnalyticsTracker.Stat.PAGES_SET_PARENT_CHANGES_SAVED to "site_pages_set_parent_changes_saved",
        AnalyticsTracker.Stat.PAGES_ADD_PAGE to "site_pages_add_page",
        AnalyticsTracker.Stat.PAGES_TAB_PRESSED to "site_pages_tabs_pressed",
        AnalyticsTracker.Stat.PAGES_OPTIONS_PRESSED to "site_pages_options_pressed",
        AnalyticsTracker.Stat.PAGES_SEARCH_ACCESSED to "site_pages_search_accessed",
        AnalyticsTracker.Stat.PAGES_EDIT_HOMEPAGE_INFO_PRESSED to "site_pages_edit_homepage_info_pressed",
        AnalyticsTracker.Stat.PAGES_EDIT_HOMEPAGE_ITEM_PRESSED to "site_pages_edit_homepage_item_pressed",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_GRAVATAR_GALLERY_PICKED to "signup_email_epilogue_gallery_picked",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_GRAVATAR_SHOT_NEW to "signup_email_epilogue_shot_new",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_UNCHANGED to "signup_epilogue_unchanged",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_UPDATE_DISPLAY_NAME_FAILED to "signup_epilogue_update_display_name_failed",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_UPDATE_DISPLAY_NAME_SUCCEEDED to "signup_epilogue_update_display_name_succeeded",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_UPDATE_USERNAME_FAILED to "signup_epilogue_update_username_failed",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_UPDATE_USERNAME_SUCCEEDED to "signup_epilogue_update_username_succeeded",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_USERNAME_SUGGESTIONS_FAILED to "signup_epilogue_username_suggestions_failed",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_USERNAME_TAPPED to "signup_epilogue_username_tapped",
        AnalyticsTracker.Stat.SIGNUP_EMAIL_EPILOGUE_VIEWED to "signup_epilogue_viewed",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_UNCHANGED to "signup_epilogue_unchanged",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_UPDATE_DISPLAY_NAME_FAILED to "signup_epilogue_update_display_name_failed",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_UPDATE_DISPLAY_NAME_SUCCEEDED to "signup_epilogue_update_display_name_succeeded",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_UPDATE_USERNAME_FAILED to "signup_epilogue_update_username_failed",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_UPDATE_USERNAME_SUCCEEDED to "signup_epilogue_update_username_succeeded",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_USERNAME_SUGGESTIONS_FAILED to "signup_epilogue_username_suggestions_failed",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_USERNAME_TAPPED to "signup_epilogue_username_tapped",
        AnalyticsTracker.Stat.SIGNUP_SOCIAL_EPILOGUE_VIEWED to "signup_epilogue_viewed",
        AnalyticsTracker.Stat.MEDIA_LIBRARY_ADDED_PHOTO to "media_library_photo_added",
        AnalyticsTracker.Stat.MEDIA_LIBRARY_ADDED_VIDEO to "media_library_video_added",
        AnalyticsTracker.Stat.PERSON_REMOVED to "people_management_person_removed",
        AnalyticsTracker.Stat.PERSON_UPDATED to "people_management_person_updated",
        AnalyticsTracker.Stat.THEMES_ACCESSED_THEMES_BROWSER to "themes_theme_browser_accessed",
        AnalyticsTracker.Stat.THEMES_ACCESSED_SEARCH to "themes_search_accessed",
        AnalyticsTracker.Stat.THEMES_CHANGED_THEME to "themes_theme_changed",
        AnalyticsTracker.Stat.THEMES_PREVIEWED_SITE to "themes_theme_for_site_previewed",
        AnalyticsTracker.Stat.SITE_SETTINGS_ACCESSED_MORE_SETTINGS to "site_settings_more_settings_accessed",
        AnalyticsTracker.Stat.SITE_SETTINGS_JETPACK_SECURITY_SETTINGS_VIEWED to "jetpack_settings_viewed",
        AnalyticsTracker.Stat.SITE_SETTINGS_JETPACK_ALLOWLISTED_IPS_VIEWED to "jetpack_allowlisted_ips_viewed",
        AnalyticsTracker.Stat.SITE_SETTINGS_JETPACK_ALLOWLISTED_IPS_CHANGED to "jetpack_allowlisted_ips_changed",
        AnalyticsTracker.Stat.TRAIN_TRACKS_RENDER to "traintracks_render",
        AnalyticsTracker.Stat.TRAIN_TRACKS_INTERACT to "traintracks_interact",
        AnalyticsTracker.Stat.MEDIA_UPLOAD_STARTED to "media_service_upload_started",
        AnalyticsTracker.Stat.MEDIA_UPLOAD_ERROR to "media_service_upload_response_error",
        AnalyticsTracker.Stat.MEDIA_UPLOAD_SUCCESS to "media_service_upload_response_ok",
        AnalyticsTracker.Stat.MEDIA_UPLOAD_CANCELED to "media_service_upload_canceled",
        AnalyticsTracker.Stat.QUICK_START_TASK_DIALOG_NEGATIVE_TAPPED to "quick_start_task_dialog_button_tapped",
        AnalyticsTracker.Stat.QUICK_START_TASK_DIALOG_POSITIVE_TAPPED to "quick_start_task_dialog_button_tapped",
        AnalyticsTracker.Stat.QUICK_START_REMOVE_DIALOG_NEGATIVE_TAPPED to "quick_start_remove_dialog_button_tapped",
        AnalyticsTracker.Stat.QUICK_START_REMOVE_DIALOG_POSITIVE_TAPPED to "quick_start_remove_dialog_button_tapped",
        AnalyticsTracker.Stat.QUICK_START_TYPE_CUSTOMIZE_VIEWED to "quick_start_list_viewed",
        AnalyticsTracker.Stat.QUICK_START_TYPE_GROW_VIEWED to "quick_start_list_viewed",
        AnalyticsTracker.Stat.QUICK_START_TYPE_GET_TO_KNOW_APP_VIEWED to "quick_start_list_viewed",
        AnalyticsTracker.Stat.QUICK_START_TYPE_CUSTOMIZE_DISMISSED to "quick_start_type_dismissed",
        AnalyticsTracker.Stat.QUICK_START_TYPE_GROW_DISMISSED to "quick_start_type_dismissed",
        AnalyticsTracker.Stat.QUICK_START_TYPE_GET_TO_KNOW_APP_DISMISSED to "quick_start_type_dismissed",
        AnalyticsTracker.Stat.QUICK_START_LIST_CREATE_SITE_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_UPDATE_SITE_TITLE_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_VIEW_SITE_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_ADD_SOCIAL_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_PUBLISH_POST_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_FOLLOW_SITE_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_UPLOAD_ICON_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_CHECK_STATS_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_REVIEW_PAGES_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_CHECK_NOTIFICATIONS_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_UPLOAD_MEDIA_SKIPPED to "quick_start_list_item_skipped",
        AnalyticsTracker.Stat.QUICK_START_LIST_CREATE_SITE_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_UPDATE_SITE_TITLE_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_VIEW_SITE_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_ADD_SOCIAL_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_PUBLISH_POST_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_FOLLOW_SITE_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_UPLOAD_ICON_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_CHECK_STATS_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_REVIEW_PAGES_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_CHECK_NOTIFICATIONS_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_LIST_UPLOAD_MEDIA_TAPPED to "quick_start_list_item_tapped",
        AnalyticsTracker.Stat.QUICK_START_CREATE_SITE_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_UPDATE_SITE_TITLE_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_VIEW_SITE_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_SHARE_SITE_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_PUBLISH_POST_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_FOLLOW_SITE_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_UPLOAD_ICON_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_CHECK_STATS_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_REVIEW_PAGES_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_CHECK_NOTIFICATIONS_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_UPLOAD_MEDIA_TASK_COMPLETED to "quick_start_task_completed",
        AnalyticsTracker.Stat.QUICK_START_REQUEST_VIEWED to "quick_start_request_dialog_viewed",
        AnalyticsTracker.Stat.QUICK_START_REQUEST_DIALOG_NEGATIVE_TAPPED to "quick_start_request_dialog_button_tapped",
        AnalyticsTracker.Stat.QUICK_START_REQUEST_DIALOG_POSITIVE_TAPPED to "quick_start_request_dialog_button_tapped",
        AnalyticsTracker.Stat.APP_REVIEWS_DECLINED_TO_RATE_APP to "app_reviews_declined_to_rate_apt",
        AnalyticsTracker.Stat.APP_REVIEWS_EVENT_INCREMENTED_BY_UPLOADING_MEDIA to "app_reviews_significant_event_incremented",
        AnalyticsTracker.Stat.APP_REVIEWS_EVENT_INCREMENTED_BY_CHECKING_NOTIFICATION to "app_reviews_significant_event_incremented",
        AnalyticsTracker.Stat.APP_REVIEWS_EVENT_INCREMENTED_BY_PUBLISHING_POST_OR_PAGE to "app_reviews_significant_event_incremented",
        AnalyticsTracker.Stat.APP_REVIEWS_EVENT_INCREMENTED_BY_OPENING_READER_POST to "app_reviews_significant_event_incremented",
        AnalyticsTracker.Stat.DOMAINS_SEARCH_SELECT_DOMAIN_TAPPED to "domains_dashboard_select_domain_tapped",
        AnalyticsTracker.Stat.QUICK_LINK_RIBBON_PAGES_TAPPED to "quick_action_ribbon_tapped",
        AnalyticsTracker.Stat.QUICK_LINK_RIBBON_POSTS_TAPPED to "quick_action_ribbon_tapped",
        AnalyticsTracker.Stat.QUICK_LINK_RIBBON_MEDIA_TAPPED to "quick_action_ribbon_tapped",
        AnalyticsTracker.Stat.QUICK_LINK_RIBBON_STATS_TAPPED to "quick_action_ribbon_tapped",
        AnalyticsTracker.Stat.QUICK_LINK_RIBBON_MORE_TAPPED to "quick_action_ribbon_tapped",
        AnalyticsTracker.Stat.OPENED_QUICK_LINK_RIBBON_MORE to "site_menu_opened",
        AnalyticsTracker.Stat.WELCOME_NO_SITES_INTERSTITIAL_CREATE_NEW_SITE_TAPPED to "welcome_no_sites_interstitial_button_tapped",
        AnalyticsTracker.Stat.WELCOME_NO_SITES_INTERSTITIAL_ADD_SELF_HOSTED_SITE_TAPPED to "welcome_no_sites_interstitial_button_tapped",
        AnalyticsTracker.Stat.FEATURE_ANNOUNCEMENT_SHOWN_ON_APP_UPGRADE to "feature_announcement_shown",
        AnalyticsTracker.Stat.FEATURE_ANNOUNCEMENT_SHOWN_FROM_APP_SETTINGS to "feature_announcement_shown",
        AnalyticsTracker.Stat.FEATURE_ANNOUNCEMENT_FIND_OUT_MORE_TAPPED to "feature_announcement_button_tapped",
        AnalyticsTracker.Stat.FEATURE_ANNOUNCEMENT_CLOSE_DIALOG_BUTTON_TAPPED to "feature_announcement_button_tapped",
        AnalyticsTracker.Stat.EDITOR_GUTENBERG_UNSUPPORTED_BLOCK_WEBVIEW_SHOWN to "gutenberg_unsupported_block_webview_shown",
        AnalyticsTracker.Stat.EDITOR_GUTENBERG_UNSUPPORTED_BLOCK_WEBVIEW_CLOSED to "gutenberg_unsupported_block_webview_closed",
        AnalyticsTracker.Stat.READER_POST_MARKED_AS_SEEN to "reader_mark_as_seen",
        AnalyticsTracker.Stat.READER_POST_MARKED_AS_UNSEEN to "reader_mark_as_unseen",
        AnalyticsTracker.Stat.COMMENT_QUICK_ACTION_APPROVED to "comment_approved",
        AnalyticsTracker.Stat.COMMENT_QUICK_ACTION_LIKED to "comment_liked",
        AnalyticsTracker.Stat.COMMENT_QUICK_ACTION_REPLIED_TO to "comment_replied_to",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_ANSWER_PROMPT_CLICKED to "blogging_prompts_my_site_card_answer_prompt_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_SHARE_CLICKED to "blogging_prompts_my_site_card_share_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_VIEW_ANSWERS_CLICKED to "blogging_prompts_my_site_card_view_answers_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_CLICKED to "blogging_prompts_my_site_card_menu_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_VIEW_MORE_PROMPTS_CLICKED to "blogging_prompts_my_site_card_menu_view_more_prompts_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_SKIP_THIS_PROMPT_CLICKED to "blogging_prompts_my_site_card_menu_skip_this_prompt_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_REMOVE_FROM_DASHBOARD_CLICKED to "blogging_prompts_my_site_card_menu_remove_from_dashboard_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_SKIP_THIS_PROMPT_UNDO_CLICKED to "blogging_prompts_my_site_card_menu_skip_this_prompt_undo_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_REMOVE_FROM_DASHBOARD_UNDO_CLICKED to "blogging_prompts_my_site_card_menu_remove_from_dashboard_undo_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_MY_SITE_CARD_MENU_LEARN_MORE_CLICKED to "blogging_prompts_my_site_card_menu_learn_more_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_INTRODUCTION_SCREEN_VIEWED to "blogging_prompts_introduction_modal_viewed",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_INTRODUCTION_SCREEN_DISMISSED to "blogging_prompts_introduction_modal_dismissed",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_INTRODUCTION_TRY_IT_NOW_CLICKED to "blogging_prompts_introduction_modal_try_it_now_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_INTRODUCTION_REMIND_ME_CLICKED to "blogging_prompts_introduction_modal_remind_me_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_INTRODUCTION_GOT_IT_CLICKED to "blogging_prompts_introduction_modal_got_it_tapped",
        AnalyticsTracker.Stat.BLOGGING_PROMPTS_LIST_SCREEN_VIEWED to "blogging_prompts_prompts_list_viewed",
        AnalyticsTracker.Stat.JETPACK_REMOVE_FEATURE_OVERLAY_DISPLAYED to "remove_feature_overlay_displayed",
        AnalyticsTracker.Stat.JETPACK_REMOVE_FEATURE_OVERLAY_LINK_TAPPED to "remove_feature_overlay_link_tapped",
        AnalyticsTracker.Stat.JETPACK_REMOVE_FEATURE_OVERLAY_BUTTON_GET_JETPACK_APP_TAPPED to "remove_feature_overlay_button_tapped",
        AnalyticsTracker.Stat.JETPACK_REMOVE_FEATURE_OVERLAY_DISMISSED to "remove_feature_overlay_dismissed",
        AnalyticsTracker.Stat.JETPACK_REMOVE_FEATURE_OVERLAY_LEARN_MORE_TAPPED to "remove_feature_overlay_link_tapped",
        AnalyticsTracker.Stat.JETPACK_REMOVE_SITE_CREATION_OVERLAY_DISPLAYED to "remove_site_creation_overlay_displayed",
        AnalyticsTracker.Stat.JETPACK_REMOVE_SITE_CREATION_OVERLAY_BUTTON_GET_JETPACK_APP_TAPPED to "remove_site_creation_overlay_button_tapped",
        AnalyticsTracker.Stat.JETPACK_REMOVE_SITE_CREATION_OVERLAY_DISMISSED to "remove_site_creation_overlay_dismissed",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_CARD_VIEWED to "jp_install_full_plugin_card_viewed",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_CARD_TAPPED to "jp_install_full_plugin_card_tapped",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_CARD_DISMISSED to "jp_install_full_plugin_card_dismissed",
        AnalyticsTracker.Stat.JETPACK_FULL_PLUGIN_INSTALL_ONBOARDING_SCREEN_SHOWN to "jp_install_full_plugin_onboarding_modal_viewed",
        AnalyticsTracker.Stat.JETPACK_FULL_PLUGIN_INSTALL_ONBOARDING_SCREEN_DISMISSED to "jp_install_full_plugin_onboarding_modal_dismissed",
        AnalyticsTracker.Stat.JETPACK_FULL_PLUGIN_INSTALL_ONBOARDING_INSTALL_TAPPED to "jp_install_full_plugin_onboarding_modal_install_tapped",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_FLOW_VIEWED to "jp_install_full_plugin_flow_viewed",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_FLOW_CANCEL_TAPPED to "jp_install_full_plugin_flow_cancel_tapped",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_FLOW_INSTALL_TAPPED to "jp_install_full_plugin_flow_install_tapped",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_FLOW_RETRY_TAPPED to "jp_install_full_plugin_flow_retry_tapped",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_FLOW_SUCCESS to "jp_install_full_plugin_flow_success",
        AnalyticsTracker.Stat.JETPACK_INSTALL_FULL_PLUGIN_FLOW_DONE_TAPPED to "jp_install_full_plugin_flow_done_tapped",
        AnalyticsTracker.Stat.BLAZE_FEATURE_OVERLAY_DISPLAYED to "blaze_overlay_displayed",
        AnalyticsTracker.Stat.BLAZE_FEATURE_OVERLAY_PROMOTE_CLICKED to "blaze_overlay_button_tapped",
        AnalyticsTracker.Stat.BLAZE_FEATURE_OVERLAY_DISMISSED to "blaze_overlay_dismissed",
        AnalyticsTracker.Stat.BLAZE_CAMPAIGN_LISTING_PAGE_SHOWN to "blaze_campaign_list_opened",
        AnalyticsTracker.Stat.BLAZE_CAMPAIGN_DETAIL_PAGE_OPENED to "blaze_campaign_details_opened",
        AnalyticsTracker.Stat.WP_JETPACK_INDIVIDUAL_PLUGIN_OVERLAY_SHOWN to "wp_individual_site_overlay_viewed",
        AnalyticsTracker.Stat.WP_JETPACK_INDIVIDUAL_PLUGIN_OVERLAY_DISMISSED to "wp_individual_site_overlay_dismissed",
        AnalyticsTracker.Stat.WP_JETPACK_INDIVIDUAL_PLUGIN_OVERLAY_PRIMARY_TAPPED to "wp_individual_site_overlay_primary_tapped",
        AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_SHOWN to "free_to_paid_plan_dashboard_card_shown",
        AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_TAPPED to "free_to_paid_plan_dashboard_card_tapped",
        AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_MORE_MENU_TAPPED to "free_to_paid_plan_dashboard_card_menu_tapped",
        AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_HIDDEN to "free_to_paid_plan_dashboard_card_hidden",
    )
}
