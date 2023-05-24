package org.konkuk.placelist.domain

import org.konkuk.placelist.domain.enums.PlaceSituation
import org.konkuk.placelist.domain.enums.TodoPriority

class Todo : Task() {
    lateinit var priority: TodoPriority
    lateinit var subtasks: List<Subtask>

    // About Places
    lateinit var situation: PlaceSituation

}
