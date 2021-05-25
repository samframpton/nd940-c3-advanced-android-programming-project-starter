package com.udacity

sealed class ButtonState {
    object Idle : ButtonState()
    object Loading : ButtonState()
}
