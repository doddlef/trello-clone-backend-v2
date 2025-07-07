package org.kevin.trello_v2.framework

open class TrelloException: Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )
}

class BadArgumentException: TrelloException {
    constructor() : super("Bad argument provided.")
    constructor(message: String) : super(message)
    constructor(expected: String, given: String, ) : super("Expected: $expected, but got: $given")
}