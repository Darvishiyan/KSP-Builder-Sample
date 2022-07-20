package com.darvishiyan.kspsample

import com.darvishiyan.annotations.AutoBuilder
import com.darvishiyan.annotations.BuilderProperty

@AutoBuilder(flexible = true)
data class Person(
    val name: String,
    @BuilderProperty val age: Int?,
    @BuilderProperty val email: String?,
    @BuilderProperty val contact: Pair<String, String>?,
)