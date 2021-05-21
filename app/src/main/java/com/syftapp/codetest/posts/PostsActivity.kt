package com.syftapp.codetest.posts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.syftapp.codetest.Navigation
import com.syftapp.codetest.R
import com.syftapp.codetest.data.model.domain.Post
import kotlinx.android.synthetic.main.activity_posts.*
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent

/**
 * Handles the display of [Post] and also handles showing details of a [Post] on tap
 */
class PostsActivity : AppCompatActivity(), PostsView, KoinComponent {

    private val presenter: PostsPresenter by inject()
    private lateinit var navigation: Navigation

    private lateinit var adapter: PostsAdapter

    private var isLoadingData: Boolean = false
    private var isLastDataPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        navigation = Navigation(this)

        listOfPosts.layoutManager = LinearLayoutManager(this)
        val separator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        listOfPosts.addItemDecoration(separator)

        addPaginationListener()

        presenter.bind(this)

    }

    private fun addPaginationListener(){

        listOfPosts.layoutManager?.let {

            listOfPosts.addOnScrollListener(object :
                PaginationListener(layoutManager = it as LinearLayoutManager) {
                override fun loadMoreItems() {
                    presenter.loadPosts(loadMore = true)
                }

                override val isLastPage: Boolean
                    get() = isLastDataPage

                override val isLoading: Boolean
                    get() = isLoadingData

            })

        }
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun render(state: PostScreenState) {
        when (state) {
            is PostScreenState.Loading -> onLoadingData()
            is PostScreenState.DataAvailable -> showPosts(state.posts)
            is PostScreenState.Error -> showError(getString(R.string.load_posts_error_message))
            is PostScreenState.FinishedLoading -> finishedLoading(state.lastPage)
            is PostScreenState.PostSelected -> navigation.navigateToPostDetail(state.post.id)
        }
    }

    /**
     * Handles updating the [isLoadingData] to true to signify to the [listOfPosts] to stop listening to scrolls
     * Shows loading screen with [showLoading]
     */
    private fun onLoadingData(){
        isLoadingData = true
        showLoading()
    }

    /**
     * Handles hiding [listOfPosts] and showing [loading] when new data is being loaded
     * [listOfPosts] is only hidden when it's empty
     */
    private fun showLoading() {
        error.visibility = View.GONE
        if (::adapter.isInitialized && adapter.isEmpty())
            listOfPosts.visibility = View.GONE
        loading.visibility = View.VISIBLE
    }

    /**
     * Handles hiding progress barwith [hideLoading] and updating the [isLoadingData] to false to signify
     * to the [listOfPosts] to start listening to scrolls
     */
    private fun finishedLoading(lastPage: Boolean){
        isLastDataPage = lastPage
        hideLoading()
    }

    /**
     * Handles hiding progress bar [loading]
     */
    private fun hideLoading() {
        loading.visibility = View.GONE
    }

    /**
     * Handles initializing and display of [posts] and updating the [isLoadingData] to false to signify
     * to the [listOfPosts] to start listening to scrolls
     * @param posts list of [Post]s to be displayed on [listOfPosts]
     */
    private fun showPosts(posts: List<Post>) {
        if (posts.isEmpty())
            return

        if(::adapter.isInitialized){
            adapter.updateList(posts)
        }else{
            adapter = PostsAdapter(posts.toMutableList(), presenter)
            listOfPosts.adapter = adapter
        }

        listOfPosts.visibility = View.VISIBLE
        isLoadingData = false
    }

    /**
     * Handles showing error message to the user and also updating the [isLoadingData] to false to signify
     * to the [listOfPosts] to start listening to scrolls
     * @param message error to be displayed with [error]
     */
    private fun showError(message: String) {
        error.visibility = View.VISIBLE
        error.setText(message)
        isLoadingData = false
    }
}
