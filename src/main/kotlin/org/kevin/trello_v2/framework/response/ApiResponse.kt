package org.kevin.trello_v2.framework.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse private constructor(
    val code: Int,
    val message: String?,
    val data: Map<String, Any?>?
): Serializable {

    data class Builder(
        val code: ResponseCode,
    ) {
        var message: String? = null
        var data: MutableMap<String, Any?>? = null

        fun message(message: String?) = apply { this.message = message }
        fun add(data: Pair<String, Any?>) = apply {
            if (this.data == null) {
                this.data = mutableMapOf()
            }
            this.data?.set(data.first, data.second)
        }
        fun build() = ApiResponse(code.code, message, data)
    }

    companion object {
        const val serialVersionUID: Long = 1L

        fun success() = Builder(ResponseCode.SUCCESS)
        fun error() = Builder(ResponseCode.ERROR)
    }
}
