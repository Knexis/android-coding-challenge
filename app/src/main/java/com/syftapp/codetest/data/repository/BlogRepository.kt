package com.syftapp.codetest.data.repository

import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.CommentDao
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.UserDao
import com.syftapp.codetest.data.model.domain.Comment
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.koin.core.KoinComponent

/**
 * Repository for handling the retrieval and saving of [User]s, [Comment]s and [Post]s
 * remotely [blogApi] and locally [userDao], [commentDao], [postDao]
 */
class BlogRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val blogApi: BlogApi
) : KoinComponent, BlogDataProvider {

    /**
     * Handles:
     * Fetching [User]s locally [userDao]
     * Fetching [User]s remotely[blogApi]
     * Saving   [User]s retrieved from [blogApi] into the database using[userDao]
     */
    override fun getUsers(): Single<List<User>> {
        return fetchData(
            local = { userDao.getAll() },
            remote = { blogApi.getUsers() },
            insert = { value -> userDao.insertAll(*value.toTypedArray()) }
        )
    }

    /**
     * Handles:
     * Fetching [Comment]s locally [commentDao]
     * Fetching [Comment]s remotely[blogApi]
     * Saving   [Comment]s retrieved from [blogApi] into the database using[commentDao]
     */
    override fun getComments(): Single<List<Comment>> {
        return fetchData(
            local = { commentDao.getAll() },
            remote = { blogApi.getComments() },
            insert = { value -> commentDao.insertAll(*value.toTypedArray()) }
        )
    }

    /**
     * Handles:
     * Fetching [Post]s locally [postDao]
     * Fetching [Post]s remotely[blogApi]
     * Saving   [Post]s retrieved from [blogApi] into the database using[postDao]
     */
    override fun getPosts(page: Int, limit: Int, loadMore: Boolean): Single<List<Post>> {
        return fetchData(
            local = { postDao.getAll() },
            remote = { blogApi.getPosts(page, limit) },
            insert = { value -> postDao.insertAll(*value.toTypedArray()) },
            loadMore = loadMore
        )
    }

    /**
     * Handles getting a single post from database given [postId]
     * @param postId id of the post
     * @return [Post] as a [Maybe]
     */
    fun getPost(postId: Int): Maybe<Post> {
        return postDao.get(postId)
    }

    /**
     * Handles fetch data locally or remotely and saves new data from remote into database (locally)
     * @param local local data source
     * @param remote remote data source
     * @param insert remote data source
     * @param loadMore is false by default, set this to true to load data from [remote]
     * @return [List] of data as [Single]
     */
    private fun <T> fetchData(
        local: () -> Single<List<T>>,
        remote: () -> Single<List<T>>,
        insert: (insertValue: List<T>) -> Completable,
        loadMore: Boolean = false
    ): Single<List<T>> {

        return local.invoke()
            .flatMap {
                //If there are no posts in the db or this was triggered by a loadmore
                // action go ahead and load the data from remote data source
                if(it.isEmpty() || loadMore){
                    remote.invoke()
                        .map { value ->
                            insert.invoke(value).subscribe();
                            value
                        }
                }else{
                    Single.just(it)
                }
            }
    }
}
