package org.wordpress.android.ui.posts.editor

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.editor.gutenberg.DialogVisibility
import org.wordpress.android.editor.gutenberg.DialogVisibility.Hidden
import org.wordpress.android.editor.gutenberg.DialogVisibility.Showing
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.PostImmutableModel
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.ui.posts.EditPostRepository
import org.wordpress.android.ui.posts.EditPostRepository.UpdatePostResult
import org.wordpress.android.ui.posts.PostUtilsWrapper
import org.wordpress.android.ui.posts.SavePostToDbUseCase
import org.wordpress.android.ui.posts.editor.StorePostViewModel.ActivityFinishState.SAVED_LOCALLY
import org.wordpress.android.ui.posts.editor.StorePostViewModel.ActivityFinishState.SAVED_ONLINE
import org.wordpress.android.ui.posts.editor.StorePostViewModel.UpdateFromEditor
import org.wordpress.android.ui.posts.editor.StorePostViewModel.UpdateFromEditor.PostFields
import org.wordpress.android.ui.uploads.UploadServiceFacade
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.viewmodel.Event

@ExperimentalCoroutinesApi
class StorePostViewModelTest : BaseUnitTest() {
    @Mock
    lateinit var siteStore: SiteStore

    @Mock
    lateinit var postUtils: PostUtilsWrapper

    @Mock
    lateinit var uploadService: UploadServiceFacade

    @Mock
    lateinit var postRepository: EditPostRepository

    @Mock
    lateinit var savePostToDbUseCase: SavePostToDbUseCase

    @Mock
    lateinit var networkUtils: NetworkUtilsWrapper

    @Mock
    lateinit var dispatcher: Dispatcher

    @Mock
    lateinit var context: Context

    private lateinit var viewModel: StorePostViewModel
    private val title = "title"
    private val updatedTitle = "updatedTitle"
    private val content = "content"
    private val updatedContent = "updatedContent"
    private val postStatus = "DRAFT"
    private val postModel = PostModel()
    private val site = SiteModel()
    private val localSiteId = 1
    private val postId = 2

    @Before
    fun setUp() {
        viewModel = StorePostViewModel(
            testDispatcher(),
            siteStore,
            postUtils,
            uploadService,
            savePostToDbUseCase,
            networkUtils,
            dispatcher
        )
        postModel.setId(postId)
        postModel.setTitle(title)
        postModel.setContent(content)
        postModel.setStatus(postStatus)
        whenever(postRepository.getPost()).thenReturn(postModel)
        whenever(postRepository.localSiteId).thenReturn(localSiteId)
        whenever(postRepository.id).thenReturn(postId)
        whenever(siteStore.getSiteByLocalId(localSiteId)).thenReturn(site)
        whenever(postRepository.updateAsync(any(), any())).then {
            val action: (PostModel) -> Boolean = it.getArgument(0)
            val onCompleted: (PostImmutableModel, UpdatePostResult) -> Unit = it.getArgument(1)
            if (action(postModel)) {
                onCompleted(postModel, UpdatePostResult.Updated)
            }
            null
        }
    }

    @Test
    fun `delays save call`() = test {
        var event: Event<Unit>? = null
        viewModel.onSavePostTriggered.observeForever {
            event = it
        }
        assertThat(event).isNull()

        viewModel.savePostWithDelay()
        advanceUntilIdle()

        assertThat(event).isNotNull()
    }

    @Test
    fun `saves post to DB`() {
        viewModel.savePostToDb(postRepository, site)

        verify(savePostToDbUseCase).savePostToDb(postRepository, site)
    }

    @Test
    fun `does not update post object with no change`() {
        whenever(postRepository.hasPost()).thenReturn(true)
        var postUpdated = false

        viewModel.updatePostObjectWithUIAsync(
            postRepository,
            getUpdatedTitleAndContent = { PostFields(title, content) },
            onCompleted = { _, _ ->
                postUpdated = true
            })

        verify(postRepository).updateAsync(any(), any())

        assertThat(postUpdated).isFalse()
    }

    @Test
    fun `does not update post object when post is missing`() {
        whenever(postRepository.hasPost()).thenReturn(false)
        var postUpdated = false

        viewModel.updatePostObjectWithUIAsync(
            postRepository,
            getUpdatedTitleAndContent = { PostFields(title, content) },
            onCompleted = { _, _ ->
                postUpdated = true
            })

        verify(postRepository).updateAsync(any(), any())
        assertThat(postUpdated).isFalse()
    }

    @Test
    fun `returns update error when get content function returns null`() {
        whenever(postRepository.hasPost()).thenReturn(true)
        var postUpdated = false

        viewModel.updatePostObjectWithUIAsync(
            postRepository,
            getUpdatedTitleAndContent = {
                UpdateFromEditor.Failed(
                    RuntimeException("Not found")
                )
            },
            onCompleted = { _, _ ->
                postUpdated = true
            })

        assertThat(postUpdated).isFalse()
    }

    @Test
    fun `updates post title and date locally changed when title has changed`() {
        whenever(postRepository.hasPost()).thenReturn(true)

        var postUpdated = false

        viewModel.updatePostObjectWithUIAsync(
            postRepository,
            getUpdatedTitleAndContent = {
                PostFields(
                    updatedTitle,
                    content
                )
            },
            onCompleted = { _, _ ->
                postUpdated = true
            })

        assertThat(postUpdated).isTrue()
        assertThat(postModel.title).isEqualTo(updatedTitle)
        verify(postRepository).updatePublishDateIfShouldBePublishedImmediately(postModel)
    }

    @Test
    fun `updates post content and date locally changed when content has changed`() {
        whenever(postRepository.hasPost()).thenReturn(true)

        var postUpdated = false

        viewModel.updatePostObjectWithUIAsync(
            postRepository,
            getUpdatedTitleAndContent = {
                PostFields(
                    title,
                    updatedContent
                )
            },
            onCompleted = { _, _ ->
                postUpdated = true
            })

        assertThat(postUpdated).isTrue()
        assertThat(postModel.content).isEqualTo(updatedContent)
        verify(postRepository).updatePublishDateIfShouldBePublishedImmediately(postModel)
    }

    @Test
    fun `savePostOnline saves post do database and not online when network not available`() {
        whenever(networkUtils.isNetworkAvailable()).thenReturn(false)

        val result = viewModel.savePostOnline(
            true,
            context,
            postRepository,
            site
        )

        verify(savePostToDbUseCase).savePostToDb(postRepository, site)
        assertThat(result).isEqualTo(SAVED_LOCALLY)
        verifyNoInteractions(postUtils)
        verifyNoInteractions(uploadService)
    }

    @Test
    fun `savePostOnline saves post do database and online when network available`() {
        whenever(networkUtils.isNetworkAvailable()).thenReturn(true)

        val isFirstTimePublish = true
        val result = viewModel.savePostOnline(
            isFirstTimePublish,
            context,
            postRepository,
            site
        )

        verify(savePostToDbUseCase).savePostToDb(postRepository, site)
        assertThat(result).isEqualTo(SAVED_ONLINE)
        verify(postUtils).trackSavePostAnalytics(postModel, site)
        verify(uploadService).uploadPost(eq(context), eq(postId), eq(isFirstTimePublish), any())
    }

    @Test
    fun `updates progress dialog LiveData appropriately`() {
        val actual = mutableListOf<DialogVisibility>()
        val expected = mutableListOf<DialogVisibility>()
        viewModel.savingInProgressDialogVisibility.observeForever {
            actual.add(it)
        }

        // is Hidden upon initialization
        expected.add(Hidden)
        assertThat(actual).isEqualTo(expected)

        viewModel.showSavingProgressDialog()
        expected.add(Showing)
        assertThat(actual).isEqualTo(expected)

        val randomNonNullFinishState = SAVED_LOCALLY
        viewModel.finish(randomNonNullFinishState)
        expected.add(Hidden)
        assertThat(actual).isEqualTo(expected)
    }
}
