package com.orhan.data

import com.google.gson.annotations.Expose
import kotlinx.serialization.Serializable

@Serializable
data class Directive(
    @Expose val receivers : ArrayList<String>,
)
