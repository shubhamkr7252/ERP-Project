package com.app.erp.gloabal_functions

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

object UserInputInteraction {
    private fun AppCompatActivity.blockInput() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun AppCompatActivity.unblockInput() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    internal fun AppCompatActivity.blockInputForTask(task: () -> Unit) {
        blockInput()
        task.invoke()
        unblockInput()
    }
}