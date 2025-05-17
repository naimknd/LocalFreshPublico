package com.example.localfresh.utils

/**
 * Usado como wrapper para datos expuestos vía LiveData que representan un evento
 */
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    /**
     * Retorna el contenido y evita usos repetidos
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Retorna el contenido, incluso si ya fue manejado
     */
    fun peekContent(): T = content
}