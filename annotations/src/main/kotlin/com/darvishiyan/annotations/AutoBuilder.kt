package com.darvishiyan.annotations

@Target(AnnotationTarget.CLASS)
annotation class AutoBuilder(val flexible: Boolean) {
    companion object {
        const val flexible = "flexible"
    }
}
