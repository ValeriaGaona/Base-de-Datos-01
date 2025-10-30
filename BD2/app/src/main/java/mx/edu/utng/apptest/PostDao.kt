package mx.edu.utng.apptest

import androidx.room.*
@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id DESC")
    suspend fun getAll(): List<PostEntity>
    @Insert
    suspend fun insert(post: PostEntity)
    @Delete
    suspend fun delete(post: PostEntity)

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getById(id: Int): PostEntity

    @Update
    suspend fun update(post: PostEntity): Int
}