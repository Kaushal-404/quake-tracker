package com.example.quakeapplication.domain.model

sealed interface DataError {
    // The phone could not reach the server at all: airplane mode, no signal, timeout
    // "data object" means there is only ever one of these, since it carries no details
    data object NoConnection : DataError

    // The server answered, but with a failure, for example 500 or 503
    // This one is a class, not an object, because it carries the status code
    data class Server(val code: Int) : DataError

    // The server answered, but the JSON was not in the shape we expected
    data object UnexpectedData : DataError
}