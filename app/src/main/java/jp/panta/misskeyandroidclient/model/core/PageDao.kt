package jp.panta.misskeyandroidclient.model.core

import androidx.room.*
import jp.panta.misskeyandroidclient.model.Page

@Dao
interface PageDao {
    @Query("select * from page")
    fun findAll(): List<Page>?

    @Query("select * from page where id = :id limit 1")
    fun findById(id: Long): Page?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(page: Page)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(pages: List<Page>)

    @Query("delete from page where id=:id")
    fun delete(id: Long)

    @Query("delete from page")
    fun deleteAll()

    @Delete
    fun deleteAll(list: List<Page>)

    @Query("delete from page where accountId = :accountId")
    fun clearByAccount(accountId: String)

}