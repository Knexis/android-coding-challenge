package com.syftapp.codetest.posts

import com.syftapp.codetest.data.model.domain.Post
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent

/**
 * Presenter for Posts
 */
class PostsPresenter(private val getPostsUseCase: GetPostsUseCase) : KoinComponent {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var view: PostsView

    private var page: Int = 1
    private var limit: Int = 20
    private var previousCount = 0

    fun bind(view: PostsView) {
        this.view = view
        compositeDisposable.add(loadPosts())
    }

    fun unbind() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    /**
     * Handles showing details of [Post]
     * @param post [Post] whose details are to be shown
     */
    fun showDetails(post: Post) {
        view.render(PostScreenState.PostSelected(post))
    }

    /**
     * Handles loading list of [Post]s given [page] and [limit]
     * @param loadMore is false by default. Set this to true to load [Post]s from remote data source
     */
    fun loadPosts(loadMore: Boolean = false) =

    getPostsUseCase.execute(page, limit, loadMore)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { view.render(PostScreenState.Loading) }
        .doAfterTerminate {

            //Check if it's the first page and previousCount == 0 , it means we have reached the last page of the posts.
            //Pass this value on PostScreenState.FinishedLoading
            view.render(PostScreenState.FinishedLoading(page > 1 && previousCount == 0))

        }
        .subscribe(
            {
                previousCount = it.size
                view.render(PostScreenState.DataAvailable(it))
                updatePageNumber(it, loadMore)
            },
            {
                view.render(PostScreenState.Error(it))
            }
        )

    /**
     * Handles updating [page] to the next [page] number using:
     * 1. Size of the [Post]s
     * 2. If [loadPosts] was triggered by a [loadMore] = true
     * @param posts the list of new [Post]s from the repository
     * @param loadMore tells us if [loadPosts] was triggered by a load more action or not
     * and if [posts] is from local (false) or remote data source (true).
     */
    private fun updatePageNumber(posts: List<Post>, loadMore: Boolean) {
        if(posts.isNotEmpty()){
            if(loadMore)
                //auto increment as usual if the post are from a remote data source
                page++
            else
                //determine the next page from the size of the posts from the local data source
                page = (posts.size/limit) + 1
        }
    }
}