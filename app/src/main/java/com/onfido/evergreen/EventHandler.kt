package com.onfido.evergreen

data class EventHandler(
    // TODO: type error
    val onError: EventListener<Any?>? = null,
    // TODO: type complete data
    val onComplete: EventListener<Any?>? = null
)

typealias EventListener<T> = (T) -> Unit
