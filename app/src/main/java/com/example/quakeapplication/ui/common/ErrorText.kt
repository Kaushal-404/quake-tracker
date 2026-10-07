package com.example.quakeapplication.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.quakeapplication.R

import com.example.quakeapplication.domain.model.DataError

// Turns a domain error into a user-facing sentence
// "when" over a sealed type: adding a new error kind makes this fail to compile until it is handled
@Composable
fun DataError.message(): String = when (this) {
    DataError.NoConnection -> stringResource(R.string.error_no_connection)
    is DataError.Server -> stringResource(R.string.error_server, code)
    DataError.UnexpectedData -> stringResource(R.string.error_unexpected_data)
}
