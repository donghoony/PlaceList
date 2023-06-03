package org.konkuk.placelist.place

import org.konkuk.placelist.domain.Todo

interface AddTodoListener {
    fun update(todo: Todo)
    fun getTodosPlaceId(): Int
}