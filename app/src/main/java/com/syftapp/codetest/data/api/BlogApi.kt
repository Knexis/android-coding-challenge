package com.syftapp.codetest.data.api

import com.syftapp.codetest.data.model.domain.Comment
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import com.syftapp.codetest.data.model.mapper.CommentMapper
import com.syftapp.codetest.data.model.mapper.PostMapper
import com.syftapp.codetest.data.model.mapper.UserMapper
import com.syftapp.codetest.data.model.mapper.apiToDomain
import com.syftapp.codetest.data.repository.BlogDataProvider
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Handles API calls to server for retrieving blog post data
 */
class BlogApi(private val blogService: BlogService) : BlogDataProvider {

    /**
     * Handles getting a list of all users from api endpoint [blogService]
     * @return List of [User]s as a [Single]
     */
    override fun getUsers() = blogService.getUsers().map { it.apiToDomain(UserMapper) }

    /**
     * Handles getting a list of all comments from api endpoint [blogService]
     * @return List of [Comment]s as a [Single]
     */
    override fun getComments() = blogService.getComments().map { it.apiToDomain(CommentMapper) }

    /**
     * Handles getting a list of posts given [page] , [limit]
     * @param page specifies the page of post to tbe retrieved
     * @param limit specifies the number of posts we need on the said [page]
     * @param loadMore is not used here by is required by [BlogDataProvider]
     * @return List of [Post] as a Single
     */
    override fun getPosts(page: Int, limit: Int, loadMore: Boolean): Single<List<Post>> {
        return blogService.getPosts(page, limit)
            .map { it.apiToDomain(PostMapper) }
    }

}