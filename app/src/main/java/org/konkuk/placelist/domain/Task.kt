package org.konkuk.placelist.domain

import java.util.Date

open class Task {
    lateinit var name: String
    var isCompleted: Boolean = false
    lateinit var createdDate: Date
    lateinit var completedDate : Date
}
