package org.konkuk.placelist.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.konkuk.placelist.domain.Todo

@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(todo: Todo) : Long

    @Delete
    fun delete(todo: Todo)

    @Update
    fun update(todo: Todo)

    @Query("select t.* from todos t inner join places p on t.place_id = p.place_id where t.place_id = :placeId")
    fun findByPlaceId(placeId: Long): List<Todo>

}
