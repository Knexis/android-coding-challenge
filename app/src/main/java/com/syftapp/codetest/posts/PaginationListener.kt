package com.syftapp.codetest.posts

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView scroll listener that listens to scroll of items in the [RecyclerView] till it's on the
 * last item using [isLoading] and [isLoading] and then triggers [loadMoreItems]
 */
abstract class PaginationListener(private val layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) {
                loadMoreItems()
            }
        }
    }

    /**
     * Is triggered when user scrolls to the bottom of the list [isLoading] and [isLastPage] are both false
     */
    protected abstract fun loadMoreItems()

    /**
     * Set this to true if the last item of the server as been returned
     */
    abstract val isLastPage: Boolean

    /**
     * Set this to true if [loadMoreItems] has been triggered and data has not returned
     */
    abstract val isLoading: Boolean

}