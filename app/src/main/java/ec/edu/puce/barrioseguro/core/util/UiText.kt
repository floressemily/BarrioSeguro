package ec.edu.puce.barrioseguro.core.util

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
}