package org.wordpress.android.ui.posts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.text.StringEscapeUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.analytics.AnalyticsTracker.Stat;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.action.TaxonomyAction;
import org.wordpress.android.fluxc.generated.SiteActionBuilder;
import org.wordpress.android.fluxc.generated.TaxonomyActionBuilder;
import org.wordpress.android.fluxc.model.MediaModel;
import org.wordpress.android.fluxc.model.PostFormatModel;
import org.wordpress.android.fluxc.model.PostImmutableModel;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.TermModel;
import org.wordpress.android.fluxc.model.post.PostStatus;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.MediaStore.OnMediaUploaded;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.fluxc.store.SiteStore.OnPostFormatsChanged;
import org.wordpress.android.fluxc.store.TaxonomyStore;
import org.wordpress.android.fluxc.store.TaxonomyStore.OnTaxonomyChanged;
import org.wordpress.android.models.Person;
import org.wordpress.android.ui.ActivityLauncher;
import org.wordpress.android.ui.RequestCodes;
import org.wordpress.android.ui.photopicker.MediaPickerLauncher;
import org.wordpress.android.ui.posts.EditPostRepository.UpdatePostResult;
import org.wordpress.android.ui.posts.FeaturedImageHelper.FeaturedImageData;
import org.wordpress.android.ui.posts.FeaturedImageHelper.FeaturedImageState;
import org.wordpress.android.ui.posts.FeaturedImageHelper.TrackableEvent;
import org.wordpress.android.ui.posts.PostSettingsListDialogFragment.DialogType;
import org.wordpress.android.ui.posts.PublishSettingsViewModel.PublishUiModel;
import org.wordpress.android.ui.posts.prepublishing.visibility.usecases.UpdatePostStatusUseCase;
import org.wordpress.android.ui.prefs.SiteSettingsInterface;
import org.wordpress.android.ui.prefs.SiteSettingsInterface.SiteSettingsListener;
import org.wordpress.android.ui.utils.UiHelpers;
import org.wordpress.android.usecase.social.JetpackSocialFlow;
import org.wordpress.android.util.AccessibilityUtils;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;
import org.wordpress.android.util.DateTimeUtils;
import org.wordpress.android.util.NetworkUtilsWrapper;
import org.wordpress.android.util.StringUtils;
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper;
import org.wordpress.android.util.image.ImageManager;
import org.wordpress.android.util.image.ImageManager.RequestListener;
import org.wordpress.android.util.image.ImageType;
import org.wordpress.android.widgets.WPSnackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static org.wordpress.android.ui.pages.PagesActivityKt.EXTRA_PAGE_PARENT_ID_KEY;
import static org.wordpress.android.ui.posts.EditPostActivity.EXTRA_POST_LOCAL_ID;
import static org.wordpress.android.ui.posts.SelectCategoriesActivity.KEY_SELECTED_CATEGORY_IDS;

public class EditPostSettingsFragment extends Fragment {
    private static final String POST_FORMAT_STANDARD_KEY = "standard";

    private static final int ACTIVITY_REQUEST_CODE_SELECT_CATEGORIES = 5;
    private static final int ACTIVITY_REQUEST_CODE_SELECT_TAGS = 6;

    private static final int CHOOSE_FEATURED_IMAGE_MENU_ID = 100;
    private static final int REMOVE_FEATURED_IMAGE_MENU_ID = 101;
    private static final int REMOVE_FEATURED_IMAGE_UPLOAD_MENU_ID = 102;
    private static final int RETRY_FEATURED_IMAGE_UPLOAD_MENU_ID = 103;

    private SiteSettingsInterface mSiteSettings;

    private LinearLayout mCategoriesTagsContainer;
    private LinearLayout mExcerptContainer;
    private LinearLayout mFormatContainer;
    private View mFormatBottomSeparator;
    private LinearLayout mPageAttributesContainer;
    private LinearLayout mMarkAsStickyContainer;
    private LinearLayout mPublishDateContainer;
    private TextView mExcerptTextView;
    private TextView mParentTextView;
    private TextView mSlugTextView;
    private TextView mCategoriesTextView;
    private TextView mTagsTextView;
    private TextView mStatusTextView;
    private TextView mPostFormatTextView;
    private TextView mPasswordTextView;
    private View mPostAuthorDivider;
    private LinearLayout mPostAuthorContainer;
    private TextView mAuthorTextView;
    private TextView mPublishDateTextView;
    private TextView mPublishDateTitleTextView;
    private TextView mCategoriesTagsHeaderTextView;
    private TextView mFeaturedImageHeaderTextView;
    private TextView mMoreOptionsHeaderTextView;
    private TextView mPublishHeaderTextView;
    private ImageView mFeaturedImageView;
    private ImageView mLocalFeaturedImageView;
    private Button mFeaturedImageButton;
    private SwitchCompat mStickySwitch;
    private ViewGroup mFeaturedImageRetryOverlay;
    private ViewGroup mFeaturedImageProgressOverlay;
    private ViewGroup mJetpackSocialContainer;
    private EditPostSettingsJetpackSocialContainerView mJetpackSocialContainerView;

    private ArrayList<String> mDefaultPostFormatKeys;
    private ArrayList<String> mDefaultPostFormatNames;
    private ArrayList<String> mPostFormatKeys;
    private ArrayList<String> mPostFormatNames;

    private ActivityResultLauncher<Intent> mEditShareMessageActivityResultLauncher;

    @Inject SiteStore mSiteStore;
    @Inject AccountStore mAccountStore;
    @Inject TaxonomyStore mTaxonomyStore;
    @Inject Dispatcher mDispatcher;
    @Inject ImageManager mImageManager;
    @Inject FeaturedImageHelper mFeaturedImageHelper;
    @Inject UiHelpers mUiHelpers;
    @Inject PostSettingsUtils mPostSettingsUtils;
    @Inject AnalyticsTrackerWrapper mAnalyticsTrackerWrapper;
    @Inject UpdatePostStatusUseCase mUpdatePostStatusUseCase;
    @Inject MediaPickerLauncher mMediaPickerLauncher;
    @Inject UpdateFeaturedImageUseCase mUpdateFeaturedImageUseCase;
    @Inject NetworkUtilsWrapper mNetworkUtilsWrapper;

    @Inject ViewModelProvider.Factory mViewModelFactory;
    private EditPostPublishSettingsViewModel mPublishedViewModel;
    private EditorJetpackSocialViewModel mJetpackSocialViewModel;

    private final OnCheckedChangeListener mOnStickySwitchChangeListener =
            (buttonView, isChecked) -> onStickySwitchChanged(isChecked);


    public interface EditPostActivityHook {
        EditPostRepository getEditPostRepository();

        SiteModel getSite();
    }

    public static EditPostSettingsFragment newInstance() {
        return new EditPostSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) requireActivity().getApplicationContext()).component().inject(this);
        mDispatcher.register(this);

        // Early load the default lists for post format keys and names.
        // Will use it later without needing to have access to the Resources.
        mDefaultPostFormatKeys =
                new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.post_format_keys)));
        mDefaultPostFormatNames = new ArrayList<>(Arrays.asList(getResources()
                .getStringArray(R.array.post_format_display_names)));
        mPublishedViewModel = new ViewModelProvider(requireActivity(), mViewModelFactory)
                .get(EditPostPublishSettingsViewModel.class);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updatePostFormatKeysAndNames();
        fetchSiteSettingsAndUpdateDefaultPostFormatIfNecessary();

        // Update post formats and categories, in case anything changed.
        SiteModel siteModel = getSite();
        mDispatcher.dispatch(SiteActionBuilder.newFetchPostFormatsAction(siteModel));
        if (!getEditPostRepository().isPage()) {
            mDispatcher.dispatch(TaxonomyActionBuilder.newFetchCategoriesAction(siteModel));
        }

        refreshViews();
    }

    private void fetchSiteSettingsAndUpdateDefaultPostFormatIfNecessary() {
        // A format is already set for the post, no need to fetch the default post format
        if (!TextUtils.isEmpty(getEditPostRepository().getPostFormat())) {
            return;
        }
        // we need to fetch site settings in order to get the latest default post format
        mSiteSettings = SiteSettingsInterface.getInterface(
                getActivity(), getSite(), new SiteSettingsListener() {
                    @Override
                    public void onSaveError(Exception error) {
                        // no-op
                    }

                    @Override
                    public void onFetchError(Exception error) {
                        // no-op
                    }

                    @Override
                    public void onSettingsUpdated() {
                        // mEditPostActivityHook will be null if the fragment is detached
                        if (getEditPostActivityHook() != null) {
                            updatePostFormat(
                                    mSiteSettings.getDefaultPostFormat());
                        }
                    }

                    @Override
                    public void onSettingsSaved() {
                        // no-op
                    }

                    @Override
                    public void onCredentialsValidated(Exception error) {
                        // no-op
                    }
                });
        if (mSiteSettings != null) {
            // init will fetch remote settings for us
            mSiteSettings.init(true);
        }
    }

    @Override public void onResume() {
        super.onResume();
        mJetpackSocialViewModel.onResume(JetpackSocialFlow.POST_SETTINGS);
    }

    @Override
    public void onDestroy() {
        if (mSiteSettings != null) {
            mSiteSettings.clear();
        }
        mDispatcher.unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.edit_post_settings_fragment, container, false);

        if (rootView == null) {
            return null;
        }

        mExcerptTextView = rootView.findViewById(R.id.post_excerpt);
        mParentTextView = rootView.findViewById(R.id.post_parent);
        mSlugTextView = rootView.findViewById(R.id.post_slug);
        mCategoriesTextView = rootView.findViewById(R.id.post_categories);
        mTagsTextView = rootView.findViewById(R.id.post_tags);
        mStatusTextView = rootView.findViewById(R.id.post_status);
        mPostFormatTextView = rootView.findViewById(R.id.post_format);
        mPasswordTextView = rootView.findViewById(R.id.post_password);
        mPostAuthorDivider = rootView.findViewById(R.id.post_author_divider);
        mPostAuthorContainer = rootView.findViewById(R.id.post_author_container);
        mAuthorTextView = rootView.findViewById(R.id.post_author);
        mPublishDateTextView = rootView.findViewById(R.id.publish_date);
        mPublishDateTitleTextView = rootView.findViewById(R.id.publish_date_title);
        mCategoriesTagsHeaderTextView = rootView.findViewById(R.id.post_settings_categories_and_tags_header);
        mMoreOptionsHeaderTextView = rootView.findViewById(R.id.post_settings_more_options_header);
        mFeaturedImageHeaderTextView = rootView.findViewById(R.id.post_settings_featured_image_header);
        mPublishHeaderTextView = rootView.findViewById(R.id.post_settings_publish);
        mPublishDateContainer = rootView.findViewById(R.id.publish_date_container);
        mStickySwitch = rootView.findViewById(R.id.post_settings_sticky_switch);
        mJetpackSocialContainer = rootView.findViewById(R.id.post_settings_jetpack_social_container);
        mJetpackSocialContainerView = rootView.findViewById(R.id.edit_post_settings_jetpack_social_container_view);

        mFeaturedImageView = rootView.findViewById(R.id.post_featured_image);
        mLocalFeaturedImageView = rootView.findViewById(R.id.post_featured_image_local);
        mFeaturedImageButton = rootView.findViewById(R.id.post_add_featured_image_button);
        mFeaturedImageRetryOverlay = rootView.findViewById(R.id.post_featured_image_retry_overlay);
        mFeaturedImageProgressOverlay = rootView.findViewById(R.id.post_featured_image_progress_overlay);

        OnClickListener showContextMenuListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.showContextMenu();
            }
        };

        mFeaturedImageView.setOnClickListener(showContextMenuListener);
        mLocalFeaturedImageView.setOnClickListener(showContextMenuListener);
        mFeaturedImageRetryOverlay.setOnClickListener(showContextMenuListener);
        mFeaturedImageProgressOverlay.setOnClickListener(showContextMenuListener);

        registerForContextMenu(mFeaturedImageView);
        registerForContextMenu(mLocalFeaturedImageView);
        registerForContextMenu(mFeaturedImageRetryOverlay);
        registerForContextMenu(mFeaturedImageProgressOverlay);

        mFeaturedImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchFeaturedMediaPicker();
            }
        });

        mExcerptContainer = rootView.findViewById(R.id.post_excerpt_container);
        mExcerptContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPostExcerptDialog();
            }
        });

        LinearLayout parentContainer = rootView.findViewById(R.id.post_parent_container);
        parentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPageParentActivity();
            }
        });

        final LinearLayout slugContainer = rootView.findViewById(R.id.post_slug_container);
        slugContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSlugDialog();
            }
        });

        mCategoriesTagsContainer = rootView.findViewById(R.id.post_categories_and_tags_card);

        LinearLayout categoriesContainer = rootView.findViewById(R.id.post_categories_container);
        categoriesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategoriesActivity();
            }
        });

        LinearLayout tagsContainer = rootView.findViewById(R.id.post_tags_container);
        tagsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTagsActivity();
            }
        });

        final LinearLayout statusContainer = rootView.findViewById(R.id.post_status_container);
        statusContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStatusDialog();
            }
        });

        mFormatContainer = rootView.findViewById(R.id.post_format_container);
        mFormatContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPostFormatDialog();
            }
        });

        mFormatBottomSeparator = rootView.findViewById(R.id.post_format_bottom_separator);

        final LinearLayout passwordContainer = rootView.findViewById(R.id.post_password_container);
        passwordContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPostPasswordDialog();
            }
        });

        mPublishDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentActivity activity = getActivity();
                if (activity instanceof EditPostSettingsCallback) {
                    ((EditPostSettingsCallback) activity).onEditPostPublishedSettingsClick();
                }
            }
        });

        mPostAuthorContainer.setOnClickListener(view -> showAuthorDialog());

        mStickySwitch.setOnCheckedChangeListener(mOnStickySwitchChangeListener);

        mMarkAsStickyContainer = rootView.findViewById(R.id.post_settings_mark_as_sticky_container);
        mPageAttributesContainer = rootView.findViewById(R.id.post_settings_page_attributes_container);


        mPublishedViewModel.getOnUiModel().observe(getViewLifecycleOwner(), new Observer<PublishUiModel>() {
            @Override public void onChanged(PublishUiModel uiModel) {
                updatePublishDateTextView(uiModel.getPublishDateLabel(),
                        Objects.requireNonNull(getEditPostRepository().getPost()));
            }
        });
        mPublishedViewModel.getOnPostStatusChanged().observe(getViewLifecycleOwner(), new Observer<PostStatus>() {
            @Override public void onChanged(PostStatus postStatus) {
                updatePostStatus(postStatus);
            }
        });

        if (getEditPostRepository() != null) {
            hideSpecificViews(getEditPostRepository().isPage());
        }
        setupSettingHintsForAccessibility();
        applyAccessibilityHeadingToSettings();

        return rootView;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupJetpackSocialViewModel();
    }

    private void setupJetpackSocialViewModel() {
        mJetpackSocialViewModel = new ViewModelProvider(
                requireActivity(),
                mViewModelFactory
        ).get(EditorJetpackSocialViewModel.class);

        observeJetpackSocialContainerVisibility();
        observeJetpackSocialUiState();
    }

    private void observeJetpackSocialContainerVisibility() {
        mJetpackSocialViewModel.getJetpackSocialContainerVisibility().observe(getViewLifecycleOwner(), visibility -> {
            if (visibility.getShowInPostSettings()) {
                mJetpackSocialContainer.setVisibility(View.VISIBLE);
            } else {
                mJetpackSocialContainer.setVisibility(View.GONE);
            }
        });
    }

    private void observeJetpackSocialUiState() {
        mJetpackSocialViewModel.getJetpackSocialUiState().observe(getViewLifecycleOwner(), uiState -> {
            mJetpackSocialContainerView.setJetpackSocialUiState(uiState);
        });
    }

    @Override
    public void onCreateContextMenu(
            @NonNull ContextMenu menu,
            @NonNull View v,
            @Nullable ContextMenu.ContextMenuInfo menuInfo
    ) {
        if (mFeaturedImageRetryOverlay.getVisibility() == View.VISIBLE) {
            menu.add(0, RETRY_FEATURED_IMAGE_UPLOAD_MENU_ID, 0,
                    getString(R.string.post_settings_retry_featured_image));
            menu.add(0, REMOVE_FEATURED_IMAGE_UPLOAD_MENU_ID, 0,
                    getString(R.string.post_settings_remove_featured_image));
        } else {
            menu.add(0, CHOOSE_FEATURED_IMAGE_MENU_ID, 0, getString(R.string.post_settings_choose_featured_image));
            menu.add(0, REMOVE_FEATURED_IMAGE_MENU_ID, 0, getString(R.string.post_settings_remove_featured_image));
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        SiteModel site = getSite();
        PostImmutableModel post = getEditPostRepository().getPost();
        if (site == null || post == null) {
            AppLog.w(T.POSTS, "Unexpected state: Post or Site is null.");
            return false;
        }
        switch (item.getItemId()) {
            case CHOOSE_FEATURED_IMAGE_MENU_ID:
                mFeaturedImageHelper.cancelFeaturedImageUpload(site, post, false);
                launchFeaturedMediaPicker();
                return true;
            case REMOVE_FEATURED_IMAGE_UPLOAD_MENU_ID:
            case REMOVE_FEATURED_IMAGE_MENU_ID:
                mFeaturedImageHelper.cancelFeaturedImageUpload(site, post, false);
                clearFeaturedImage();

                mFeaturedImageHelper.trackFeaturedImageEvent(TrackableEvent.IMAGE_REMOVE_CLICKED, post.getId());

                return true;
            case RETRY_FEATURED_IMAGE_UPLOAD_MENU_ID:
                retryFeaturedImageUpload(site, post);
                return true;
            default:
                return false;
        }
    }

    private void setupSettingHintsForAccessibility() {
        AccessibilityUtils.disableHintAnnouncement(mPublishDateTextView);
        AccessibilityUtils.disableHintAnnouncement(mCategoriesTextView);
        AccessibilityUtils.disableHintAnnouncement(mTagsTextView);
        AccessibilityUtils.disableHintAnnouncement(mPasswordTextView);
        AccessibilityUtils.disableHintAnnouncement(mSlugTextView);
        AccessibilityUtils.disableHintAnnouncement(mExcerptTextView);
        AccessibilityUtils.disableHintAnnouncement(mParentTextView);
    }

    private void applyAccessibilityHeadingToSettings() {
        AccessibilityUtils.enableAccessibilityHeading(mCategoriesTagsHeaderTextView);
        AccessibilityUtils.enableAccessibilityHeading(mFeaturedImageHeaderTextView);
        AccessibilityUtils.enableAccessibilityHeading(mMoreOptionsHeaderTextView);
        AccessibilityUtils.enableAccessibilityHeading(mPublishHeaderTextView);
    }

    private void retryFeaturedImageUpload(@NonNull SiteModel site, @NonNull PostImmutableModel post) {
        MediaModel mediaModel = mFeaturedImageHelper.retryFeaturedImageUpload(site, post);
        if (mediaModel == null) {
            clearFeaturedImage();
        }
    }

    public void refreshViews() {
        if (!isAdded() || getEditPostRepository() == null) {
            return;
        }
        hideSpecificViews(getEditPostRepository().isPage());
        mExcerptTextView.setText(getEditPostRepository().getExcerpt());
        mParentTextView.setText(getEditPostRepository().getParentTitle(getSite()));
        mSlugTextView.setText(getEditPostRepository().getSlug());
        mPasswordTextView.setText(getEditPostRepository().getPassword());
        PostImmutableModel postModel = getEditPostRepository().getPost();
        updatePostFormatTextView(postModel);
        updateTagsTextView(postModel);
        updateStatusTextView();
        updatePublishDateTextView(postModel);
        updateAuthorTextView(postModel.getAuthorDisplayName());
        mPublishedViewModel.start(getEditPostRepository());
        updateCategoriesTextView(postModel);
        updateFeaturedImageView(postModel);
        updateStickySwitch(postModel);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Bundle extras;

            switch (requestCode) {
                case ACTIVITY_REQUEST_CODE_SELECT_CATEGORIES:
                    extras = data.getExtras();
                    if (extras != null && extras.containsKey(KEY_SELECTED_CATEGORY_IDS)) {
                        @SuppressWarnings("unchecked")
                        List<Long> categoryList = (ArrayList<Long>) extras.getSerializable(KEY_SELECTED_CATEGORY_IDS);
                        PostAnalyticsUtilsKt.trackPostSettings(
                                mAnalyticsTrackerWrapper, Stat.EDITOR_POST_CATEGORIES_ADDED);
                        updateCategories(categoryList);
                    }
                    break;
                case ACTIVITY_REQUEST_CODE_SELECT_TAGS:
                    extras = data.getExtras();
                    if (resultCode == RESULT_OK && extras != null) {
                        String selectedTags = extras.getString(PostSettingsTagsActivity.KEY_SELECTED_TAGS);
                        PostAnalyticsUtilsKt.trackPostSettings(mAnalyticsTrackerWrapper, Stat.EDITOR_POST_TAGS_CHANGED);
                        updateTags(selectedTags);
                    }
                    break;
                case RequestCodes.PAGE_PARENT:
                    extras = data.getExtras();
                    if (resultCode == Activity.RESULT_OK && extras != null) {
                        long parentId = extras.getLong(EXTRA_PAGE_PARENT_ID_KEY, -1);
                        if (parentId != -1L) {
                            updateParent(parentId);
                        }
                    }
            }
        }
    }

    private void showPostExcerptDialog() {
        if (!isAdded()) {
            return;
        }
        PostSettingsInputDialogFragment dialog = PostSettingsInputDialogFragment.newInstance(
                getEditPostRepository().getExcerpt(), getString(R.string.post_settings_excerpt),
                getString(R.string.post_settings_excerpt_dialog_hint), false, true);
        dialog.setPostSettingsInputDialogListener(
                new PostSettingsInputDialogFragment.PostSettingsInputDialogListener() {
                    @Override
                    public void onInputUpdated(String input) {
                        input = input.trim();
                        mAnalyticsTrackerWrapper.track(Stat.EDITOR_POST_EXCERPT_CHANGED);
                        updateExcerpt(input);
                    }
                });
        dialog.show(getChildFragmentManager(), null);
    }

    private void showPageParentActivity() {
        EditPostRepository repository = getEditPostRepository();
        SiteModel site = getSite();

        if (!isAdded() || repository == null || site == null) {
            return;
        }

        if (!mNetworkUtilsWrapper.isNetworkAvailable()) {
            showNoNetworkSnackbar();
            return;
        }

        long remoteId = repository.getRemotePostId();
        ActivityLauncher.viewPageParentForResult(this, site, remoteId);
    }

    private void showSlugDialog() {
        if (!isAdded()) {
            return;
        }
        PostSettingsInputDialogFragment dialog = PostSettingsInputDialogFragment.newInstance(
                getEditPostRepository().getSlug(), getString(R.string.post_settings_slug),
                getString(R.string.post_settings_slug_dialog_hint), true, false);
        dialog.setPostSettingsInputDialogListener(
                new PostSettingsInputDialogFragment.PostSettingsInputDialogListener() {
                    @Override
                    public void onInputUpdated(String input) {
                        mAnalyticsTrackerWrapper.track(Stat.EDITOR_POST_SLUG_CHANGED);
                        updateSlug(input);
                    }
                });
        dialog.show(getChildFragmentManager(), null);
    }

    private void showCategoriesActivity() {
        if (!isAdded()) {
            return;
        }
        Intent categoriesIntent = new Intent(requireActivity(), SelectCategoriesActivity.class);
        categoriesIntent.putExtra(WordPress.SITE, getSite());
        categoriesIntent.putExtra(EXTRA_POST_LOCAL_ID, getEditPostRepository().getId());
        startActivityForResult(categoriesIntent, ACTIVITY_REQUEST_CODE_SELECT_CATEGORIES);
    }

    private void showTagsActivity() {
        if (!isAdded()) {
            return;
        }
        // Fetch/refresh the tags in preparation for the PostSettingsTagsActivity
        SiteModel siteModel = getSite();
        mDispatcher.dispatch(TaxonomyActionBuilder.newFetchTagsAction(siteModel));

        Intent tagsIntent = new Intent(requireActivity(), PostSettingsTagsActivity.class);
        tagsIntent.putExtra(WordPress.SITE, siteModel);
        String tags = TextUtils.join(",", getEditPostRepository().getTagNameList());
        tagsIntent.putExtra(PostSettingsTagsActivity.KEY_TAGS, tags);
        startActivityForResult(tagsIntent, ACTIVITY_REQUEST_CODE_SELECT_TAGS);
    }

    private void onStickySwitchChanged(boolean checked) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            editPostRepository.updateAsync(postModel -> {
                postModel.setSticky(checked);
                return true;
            }, null);
        }
    }

    /*
     * called by the activity when the user taps OK on a PostSettingsDialogFragment
     */
    public void onPostSettingsFragmentPositiveButtonClicked(@NonNull PostSettingsListDialogFragment fragment) {
        int index;
        PostStatus status = null;
        switch (fragment.getDialogType()) {
            case HOMEPAGE_STATUS:
                index = fragment.getCheckedIndex();
                status = getHomepageStatusAtIndex(index);
                break;
            case POST_STATUS:
                index = fragment.getCheckedIndex();
                status = getPostStatusAtIndex(index);
                break;
            case AUTHOR:
                index = fragment.getCheckedIndex();
                List<Person> authors = mPublishedViewModel.getAuthors().getValue();
                if (authors == null) {
                    return;
                }
                Person author = authors.get(index);
                updateAuthor(author);
                break;
            case POST_FORMAT:
                String formatName = fragment.getSelectedItem();
                updatePostFormat(getPostFormatKeyFromName(formatName));
                mAnalyticsTrackerWrapper.track(Stat.EDITOR_POST_FORMAT_CHANGED);
                break;
        }

        if (status != null) {
            updatePostStatus(status);
            PostAnalyticsUtilsKt.trackPostSettings(mAnalyticsTrackerWrapper, Stat.EDITOR_POST_VISIBILITY_CHANGED);
        }
    }

    private void showStatusDialog() {
        if (!isAdded()) {
            return;
        }

        boolean isSiteHomepage = isSiteHomepage();
        int index = isSiteHomepage ? getCurrentHomepageStatusIndex() : getCurrentPostStatusIndex();
        FragmentManager fm = getChildFragmentManager();

        DialogType statusType = isSiteHomepage ? DialogType.HOMEPAGE_STATUS : DialogType.POST_STATUS;
        PostSettingsListDialogFragment fragment =
                PostSettingsListDialogFragment.newInstance(statusType, index);
        fragment.show(fm, PostSettingsListDialogFragment.TAG);
    }

    private void showAuthorDialog() {
        if (!isAdded()) {
            return;
        }

        if (!mNetworkUtilsWrapper.isNetworkAvailable()) {
            showNoNetworkSnackbar();
            return;
        }

        FragmentManager fm = getChildFragmentManager();

        PostSettingsListDialogFragment fragment = PostSettingsListDialogFragment.newAuthorListInstance(getAuthorId());
        fragment.show(fm, PostSettingsListDialogFragment.TAG);
    }

    private boolean isSiteHomepage() {
        EditPostRepository postRepository = getEditPostRepository();
        boolean isPage = postRepository.isPage();
        boolean isPublishedPage = postRepository.getStatus() == PostStatus.PUBLISHED
                                  || postRepository.getStatus() == PostStatus.PRIVATE;
        boolean isHomepage = postRepository.getRemotePostId() == getSite().getPageOnFront();
        return isPage && isPublishedPage && isHomepage;
    }

    private void showPostFormatDialog() {
        if (!isAdded()) {
            return;
        }

        int checkedIndex = 0;
        String postFormat = getEditPostRepository().getPostFormat();
        if (!TextUtils.isEmpty(postFormat)) {
            for (int i = 0; i < mPostFormatKeys.size(); i++) {
                if (postFormat.equals(mPostFormatKeys.get(i))) {
                    checkedIndex = i;
                    break;
                }
            }
        }

        FragmentManager fm = getChildFragmentManager();
        PostSettingsListDialogFragment fragment =
                PostSettingsListDialogFragment.newInstance(DialogType.POST_FORMAT, checkedIndex);
        fragment.show(fm, PostSettingsListDialogFragment.TAG);
    }

    private void showPostPasswordDialog() {
        if (!isAdded()) {
            return;
        }
        PostSettingsInputDialogFragment dialog = PostSettingsInputDialogFragment.newInstance(
                getEditPostRepository().getPassword(), getString(R.string.password),
                getString(R.string.post_settings_password_dialog_hint), false, false);
        dialog.setPostSettingsInputDialogListener(
                new PostSettingsInputDialogFragment.PostSettingsInputDialogListener() {
                    @Override
                    public void onInputUpdated(String input) {
                        PostAnalyticsUtilsKt
                                .trackPostSettings(mAnalyticsTrackerWrapper, Stat.EDITOR_POST_PASSWORD_CHANGED);
                        updatePassword(input);
                    }
                });
        dialog.show(getChildFragmentManager(), null);
    }

    // Helpers

    private EditPostRepository getEditPostRepository() {
        if (getEditPostActivityHook() == null) {
            // This can only happen during a callback while activity is re-created for some reason (config changes etc)
            return null;
        }
        return getEditPostActivityHook().getEditPostRepository();
    }

    private SiteModel getSite() {
        if (getEditPostActivityHook() == null) {
            // This can only happen during a callback while activity is re-created for some reason (config changes etc)
            return null;
        }
        return getEditPostActivityHook().getSite();
    }

    private EditPostActivityHook getEditPostActivityHook() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }

        if (activity instanceof EditPostActivityHook) {
            return (EditPostActivityHook) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement EditPostActivityHook");
        }
    }

    private void updateSaveButton() {
        if (isAdded()) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void updateParent(long parentId) {
        EditPostRepository editPostRepository = getEditPostRepository();
        SiteModel site = getSite();
        if (editPostRepository != null && site != null) {
            editPostRepository.updateAsync(postModel -> {
                boolean hasChanged = postModel.getParentId() != parentId;
                postModel.setParentId(parentId);
                return hasChanged;
            }, (postModel, result) -> {
                if (result == UpdatePostResult.Updated.INSTANCE) {
                    mParentTextView.setText(editPostRepository.getParentTitle(site));
                }
                return null;
            });
        }
    }

    private void updateExcerpt(String excerpt) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            editPostRepository.updateAsync(postModel -> {
                postModel.setExcerpt(excerpt);
                return true;
            }, (postModel, result) -> {
                if (result == UpdatePostResult.Updated.INSTANCE) {
                    mExcerptTextView.setText(excerpt);
                }
                return null;
            });
        }
    }

    private void updateSlug(String slug) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            editPostRepository.updateAsync(postModel -> {
                postModel.setSlug(slug);
                return true;
            }, (postModel, result) -> {
                if (result == UpdatePostResult.Updated.INSTANCE) {
                    mSlugTextView.setText(slug);
                }
                return null;
            });
        }
    }

    private void updatePassword(String password) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository == null) return;

        String trimmedPassword = password.trim();
        Boolean isNewPasswordBlank = trimmedPassword.isEmpty();
        String previousPassword = editPostRepository.getPassword();
        Boolean isPreviousPasswordBlank = previousPassword.trim().isEmpty();

        // Nothing to save
        if (isNewPasswordBlank && isPreviousPasswordBlank) return;

        // Save untrimmed password if not blank, else save empty string
        String newPassword = isNewPasswordBlank ? trimmedPassword : password;

        editPostRepository.updateAsync(postModel -> {
            postModel.setPassword(newPassword);
            return true;
        }, (postModel, result) -> {
            if (result == UpdatePostResult.Updated.INSTANCE) {
                mPasswordTextView.setText(newPassword);
            }
            return null;
        });
    }

    private void updateCategories(List<Long> categoryList) {
        if (categoryList == null) {
            return;
        }
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            editPostRepository.updateAsync(postModel -> {
                postModel.setCategoryIdList(categoryList);
                return true;
            }, (postModel, result) -> {
                if (result == UpdatePostResult.Updated.INSTANCE) {
                    updateCategoriesTextView(postModel);
                }
                return null;
            });
        }
    }

    void updatePostStatus(PostStatus postStatus) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            mUpdatePostStatusUseCase.updatePostStatus(postStatus, editPostRepository,
                    postImmutableModel -> {
                        updatePostStatusRelatedViews(postImmutableModel);
                        updateSaveButton();
                        mJetpackSocialViewModel.onPostStatusChanged();
                        return null;
                    });
        }
    }

    void updateAuthor(Person author) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            editPostRepository.updateAsync(postModel -> {
                postModel.setAuthorId(author.getPersonID());
                postModel.setAuthorDisplayName(author.getDisplayName());
                return true;
            }, (postModel, result) -> {
                if (result == UpdatePostResult.Updated.INSTANCE) {
                    updateAuthorTextView(postModel.getAuthorDisplayName());
                }
                return null;
            });
        }
    }

    private void updatePostFormat(String postFormat) {
        EditPostRepository editPostRepository = getEditPostRepository();
        if (editPostRepository != null) {
            editPostRepository.updateAsync(postModel -> {
                postModel.setPostFormat(postFormat);
                return true;
            }, (postModel, result) -> {
                if (result == UpdatePostResult.Updated.INSTANCE) {
                    updatePostFormatTextView(postModel);
                }
                return null;
            });
        }
    }

    public void updatePostStatusRelatedViews(PostImmutableModel postModel) {
        updateStatusTextView();
        updatePublishDateTextView(postModel);
        mPublishedViewModel.onPostStatusChanged(postModel);
    }

    private void updateStatusTextView() {
        if (!isAdded()) {
            return;
        }
        String[] statuses = getResources().getStringArray(R.array.post_settings_statuses);
        int index = getCurrentPostStatusIndex();
        // We should never get an OutOfBoundsException here, but if we do,
        // we should let it crash so we can fix the underlying issue
        mStatusTextView.setText(statuses[index]);
    }

    private void updateTags(String selectedTags) {
        EditPostRepository postRepository = getEditPostRepository();
        if (postRepository == null) {
            return;
        }
        postRepository.updateAsync(postModel -> {
            if (!TextUtils.isEmpty(selectedTags)) {
                String tags = selectedTags.replace("\n", " ");
                postModel.setTagNameList(Arrays.asList(TextUtils.split(tags, ",")));
            } else {
                postModel.setTagNameList(new ArrayList<>());
            }
            return true;
        }, (postModel, result) -> {
            if (result == UpdatePostResult.Updated.INSTANCE) {
                updateTagsTextView(postModel);
            }
            return null;
        });
    }

    private void updateTagsTextView(PostImmutableModel postModel) {
        String tags = TextUtils.join(",", postModel.getTagNameList());
        // If `tags` is empty, the hint "Not Set" will be shown instead
        tags = StringEscapeUtils.unescapeHtml4(tags);
        mTagsTextView.setText(tags);
    }

    private void updateStickySwitch(PostImmutableModel postModel) {
        if (!isAdded() || postModel == null || mStickySwitch == null) {
            return;
        }

        // We need to remove the listener first, otherwise the listener will be triggered
        mStickySwitch.setOnCheckedChangeListener(null);
        mStickySwitch.setChecked(postModel.getSticky());
        mStickySwitch.setOnCheckedChangeListener(mOnStickySwitchChangeListener);
    }

    private void updatePostFormatTextView(PostImmutableModel postModel) {
        // Post format can be updated due to a site settings fetch and the textView might not have been initialized yet
        if (mPostFormatTextView == null) {
            return;
        }
        String postFormat = getPostFormatNameFromKey(postModel.getPostFormat());
        mPostFormatTextView.setText(postFormat);
    }

    private void updatePublishDateTextView(PostImmutableModel postModel) {
        if (!isAdded()) {
            return;
        }
        if (postModel != null) {
            String labelToUse = mPostSettingsUtils.getPublishDateLabel(postModel);
            updatePublishDateTextView(labelToUse, postModel);
        }
    }

    private void updatePublishDateTextView(String label, PostImmutableModel postImmutableModel) {
        mPublishDateTextView.setText(label);

        boolean isPrivatePost = postImmutableModel.getStatus().equals(PostStatus.PRIVATE.toString());

        mPublishDateTextView.setEnabled(!isPrivatePost);
        mPublishDateTitleTextView.setEnabled(!isPrivatePost);
        mPublishDateContainer.setEnabled(!isPrivatePost);
    }

    private void updateAuthorTextView(String authorDisplayName) {
        if (getSite() != null && getSite().getHasCapabilityListUsers()) {
            mPostAuthorDivider.setVisibility(View.VISIBLE);
            mPostAuthorContainer.setVisibility(View.VISIBLE);

            if (authorDisplayName == null) {
                // If the authorDisplayName is null, that means this is a new unpublished post.
                // Set author to the current user name.
                EditPostRepository editPostRepository = getEditPostRepository();
                if (editPostRepository == null) {
                    return;
                }
                PostImmutableModel postModel = editPostRepository.getPost();
                if (postModel != null && postModel.getAuthorDisplayName() == null) {
                    updateAuthorTextView(mAccountStore.getAccount().getDisplayName());
                }
            } else {
                mAuthorTextView.setText(authorDisplayName);
            }
        }
    }

    private void updateCategoriesTextView(PostImmutableModel post) {
        if (post == null || getSite() == null) {
            // Since this method can get called after a callback, we have to make sure we have the post and site
            return;
        }
        List<TermModel> categories = mTaxonomyStore.getCategoriesForPost(post, getSite());
        StringBuilder sb = new StringBuilder();
        Iterator<TermModel> it = categories.iterator();
        if (it.hasNext()) {
            sb.append(it.next().getName());
            while (it.hasNext()) {
                sb.append(", ");
                sb.append(it.next().getName());
            }
        }
        // If `sb` is empty, the hint "Not Set" will be shown instead
        mCategoriesTextView.setText(StringEscapeUtils.unescapeHtml4(sb.toString()));
    }

    // Post Status Helpers

    private PostStatus getPostStatusAtIndex(int index) {
        switch (index) {
            case 0:
                return PostStatus.PUBLISHED;
            case 1:
                return PostStatus.DRAFT;
            case 2:
                return PostStatus.PENDING;
            case 3:
                return PostStatus.PRIVATE;
            default:
                return PostStatus.UNKNOWN;
        }
    }

    private int getCurrentPostStatusIndex() {
        switch (getEditPostRepository().getStatus()) {
            case DRAFT:
                return 1;
            case PENDING:
                return 2;
            case PRIVATE:
                return 3;
            case TRASHED:
            case UNKNOWN:
            case PUBLISHED:
            case SCHEDULED:
                return 0;
        }
        return 0;
    }

    private PostStatus getHomepageStatusAtIndex(int index) {
        switch (index) {
            case 0:
                return PostStatus.PUBLISHED;
            case 1:
                return PostStatus.PRIVATE;
            default:
                return PostStatus.UNKNOWN;
        }
    }

    private int getCurrentHomepageStatusIndex() {
        switch (getEditPostRepository().getStatus()) {
            case PRIVATE:
                return 1;
            case DRAFT:
            case PENDING:
            case TRASHED:
            case UNKNOWN:
            case PUBLISHED:
            case SCHEDULED:
                return 0;
        }
        return 0;
    }

    private long getAuthorId() {
        PostImmutableModel postModel = getEditPostRepository().getPost();
        if (postModel == null) {
            return -1;
        }
        long postAuthorId = postModel.getAuthorId();
        if (postAuthorId == 0) {
            // If the author id is 0, that means this is the post creating screen.
            // Selected author should be the current user.
            return mAccountStore.getAccount().getUserId();
        } else {
            return postAuthorId;
        }
    }

    // Post Format Helpers

    private void updatePostFormatKeysAndNames() {
        final SiteModel site = getSite();
        if (site == null) {
            // Since this method can get called after a callback, we have to make sure we have the site
            return;
        }

        // Initialize the lists from the defaults
        mPostFormatKeys = new ArrayList<>(mDefaultPostFormatKeys);
        mPostFormatNames = new ArrayList<>(mDefaultPostFormatNames);

        // If we have specific values for this site, use them
        List<PostFormatModel> postFormatModels = mSiteStore.getPostFormats(site);
        for (PostFormatModel postFormatModel : postFormatModels) {
            if (!mPostFormatKeys.contains(postFormatModel.getSlug())) {
                mPostFormatKeys.add(postFormatModel.getSlug());
                mPostFormatNames.add(postFormatModel.getDisplayName());
            }
        }
    }

    private String getPostFormatKeyFromName(String postFormatName) {
        for (int i = 0; i < mPostFormatNames.size(); i++) {
            if (postFormatName.equalsIgnoreCase(mPostFormatNames.get(i))) {
                return mPostFormatKeys.get(i);
            }
        }
        return POST_FORMAT_STANDARD_KEY;
    }

    private String getPostFormatNameFromKey(String postFormatKey) {
        if (TextUtils.isEmpty(postFormatKey)) {
            postFormatKey = POST_FORMAT_STANDARD_KEY;
        }

        for (int i = 0; i < mPostFormatKeys.size(); i++) {
            if (postFormatKey.equalsIgnoreCase(mPostFormatKeys.get(i))) {
                return mPostFormatNames.get(i);
            }
        }
        // Since this is only used as a display name, if we can't find the key, we should just
        // return the capitalized key as the name which should be better than returning `null`
        return StringUtils.capitalize(postFormatKey);
    }

    // Featured Image Helpers

    public void updateFeaturedImage(long featuredImageId, boolean imagePicked) {
        if (isAdded() && imagePicked) {
            int postId = getEditPostRepository().getId();
            mFeaturedImageHelper.trackFeaturedImageEvent(
                    TrackableEvent.IMAGE_PICKED_POST_SETTINGS,
                    postId
            );
        }

        EditPostRepository postRepository = getEditPostRepository();
        if (postRepository == null) {
            return;
        }

        mUpdateFeaturedImageUseCase.updateFeaturedImage(featuredImageId, postRepository,
                postModel -> {
                    updateFeaturedImageView(postModel);
                    return null;
                });
    }

    private void clearFeaturedImage() {
        updateFeaturedImage(0, false);

        if (getActivity() instanceof EditPostSettingsCallback) {
            ((EditPostSettingsCallback) getActivity()).clearFeaturedImage();
        }
    }

    private void updateFeaturedImageView(PostImmutableModel postModel) {
        Context context = getContext();
        SiteModel site = getSite();
        if (!isAdded() || postModel == null || site == null || context == null) {
            return;
        }
        final FeaturedImageData currentFeaturedImageState =
                mFeaturedImageHelper.createCurrentFeaturedImageState(site, postModel);

        FeaturedImageState uiState = currentFeaturedImageState.getUiState();
        updateFeaturedImageViews(currentFeaturedImageState.getUiState());
        if (currentFeaturedImageState.getMediaUri() != null) {
            if (uiState == FeaturedImageState.REMOTE_IMAGE_LOADING) {
                /*
                 *  Fetch the remote image, but keep showing the local image (when present) until "onResourceReady"
                 *  is invoked.  We use this hack to prevent showing an empty view when the local image is replaced
                 *  with a remote image.
                 */
                mImageManager.loadWithResultListener(mFeaturedImageView, ImageType.IMAGE,
                        currentFeaturedImageState.getMediaUri(), ScaleType.FIT_CENTER,
                        null, new RequestListener<Drawable>() {
                            @Override public void onLoadFailed(@Nullable Exception e, @Nullable Object model) {
                            }

                            @Override public void onResourceReady(@NonNull Drawable resource, @Nullable Object model) {
                                if (currentFeaturedImageState.getUiState() == FeaturedImageState.REMOTE_IMAGE_LOADING) {
                                    updateFeaturedImageViews(FeaturedImageState.REMOTE_IMAGE_SET);
                                }
                            }
                        });
            } else {
                mImageManager.load(mLocalFeaturedImageView, ImageType.IMAGE, currentFeaturedImageState.getMediaUri(),
                        ScaleType.FIT_CENTER);
            }
        }
    }

    private void launchFeaturedMediaPicker() {
        if (isAdded()) {
            int postId = getEditPostRepository().getId();
            mFeaturedImageHelper.trackFeaturedImageEvent(TrackableEvent.IMAGE_SET_CLICKED, postId);

            mMediaPickerLauncher
                    .showFeaturedImagePicker(requireActivity(), getSite(),
                            postId);
        }
    }

    // Publish Date Helpers

    private Calendar getCurrentPublishDateAsCalendar() {
        Calendar calendar = Calendar.getInstance();
        String dateCreated = getEditPostRepository().getDateCreated();
        // Set the currently selected time if available
        if (!TextUtils.isEmpty(dateCreated)) {
            calendar.setTime(DateTimeUtils.dateFromIso8601(dateCreated));
        }
        return calendar;
    }

    // FluxC events

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTaxonomyChanged(OnTaxonomyChanged event) {
        if (event.isError()) {
            AppLog.e(T.POSTS, "An error occurred while updating taxonomy with type: " + event.error.type);
            return;
        }
        if (event.causeOfChange == TaxonomyAction.FETCH_CATEGORIES) {
            updateCategoriesTextView(getEditPostRepository().getPost());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPostFormatsChanged(OnPostFormatsChanged event) {
        if (event.isError()) {
            AppLog.e(T.POSTS, "An error occurred while updating the post formats with type: " + event.error.type);
            return;
        }
        AppLog.v(T.POSTS, "Post formats successfully fetched!");
        updatePostFormatKeysAndNames();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaUploaded(OnMediaUploaded event) {
        if (event.media != null && event.media.getMarkedLocallyAsFeatured()) {
            refreshViews();
        }
    }

    private void updateFeaturedImageViews(FeaturedImageState state) {
        mUiHelpers.updateVisibility(mFeaturedImageView, state.getImageViewVisible());
        mUiHelpers.updateVisibility(mLocalFeaturedImageView, state.getLocalImageViewVisible());
        mUiHelpers.updateVisibility(mFeaturedImageButton, state.getButtonVisible());
        mUiHelpers.updateVisibility(mFeaturedImageRetryOverlay, state.getRetryOverlayVisible());
        mUiHelpers.updateVisibility(mFeaturedImageProgressOverlay, state.getProgressOverlayVisible());
        if (!state.getLocalImageViewVisible()) {
            mImageManager.cancelRequestAndClearImageView(mLocalFeaturedImageView);
        }
    }

    private void hideSpecificViews(Boolean isPage) {
        if (isPage) {
            mCategoriesTagsContainer.setVisibility(View.GONE);
            mFormatContainer.setVisibility(View.GONE);
            mFormatBottomSeparator.setVisibility(View.GONE);
            mMarkAsStickyContainer.setVisibility(View.GONE);
            mExcerptContainer.setVisibility(View.GONE);
        } else {
            mPageAttributesContainer.setVisibility(View.GONE);
        }
    }

    private void showNoNetworkSnackbar() {
        String message = getString(R.string.no_network_message);
        WPSnackbar.make(
                requireView().findViewById(R.id.settings_fragment_root),
                message,
                Snackbar.LENGTH_LONG
        ).show();
    }

    interface EditPostSettingsCallback {
        void onEditPostPublishedSettingsClick();

        void clearFeaturedImage();
    }
}
