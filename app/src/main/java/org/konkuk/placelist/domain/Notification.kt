package org.konkuk.placelist.domain

import org.konkuk.placelist.domain.enums.NotificationType

class Notification {
    lateinit var type: NotificationType
    lateinit var name: String
    lateinit var detail: String
}
