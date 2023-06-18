package org.konkuk.placelist.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.konkuk.placelist.domain.enums.PlaceSituation
import org.konkuk.placelist.domain.enums.TodoPriority
import java.io.Serializable

@Entity(tableName = "todos",
    foreignKeys = [ForeignKey(entity = Place::class,
        parentColumns = ["place_id"],
        childColumns = ["place_id"],
        onDelete = CASCADE)],
)

data class Todo(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "place_id") var placeId: Int,
    var name: String,
    var isCompleted: Boolean = false,

    var priority: TodoPriority,
//    var subtasks: List<Subtask>,

    var repeatDays: Int,

    // About Places
    var situation: PlaceSituation
) : Serializable
