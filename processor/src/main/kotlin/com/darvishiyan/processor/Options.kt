package com.darvishiyan.processor

class Options {
    companion object {
        const val myConfig1 = "myConfig1"
        const val myConfig2 = "myConfig2"
        const val myConfig3 = "myConfig3"

        @Suppress("UNCHECKED_CAST")
        fun <T> getValue(options: Map<String, String>, option: String): T? = options[option] as? T
        fun isActive(options: Map<String, String>, option: String) = options[option] == "true"
        fun getText(options: Map<String, String>, option: String) = options[option]
    }
}