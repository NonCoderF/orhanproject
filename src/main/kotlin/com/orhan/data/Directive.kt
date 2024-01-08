package com.orhan.data

import com.google.gson.annotations.Expose
import kotlinx.serialization.Serializable

@Serializable
data class Directive(
    @Expose val ticker : String,
    @Expose val receivers : ArrayList<String>,
)
