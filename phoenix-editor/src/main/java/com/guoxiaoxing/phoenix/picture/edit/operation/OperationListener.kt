package com.souche.android.sdk.media.editor.operation

import com.guoxiaoxing.phoenix.picture.edit.operation.Operation

interface OperationListener {

    fun onOperationSelected(operation: Operation)

    fun onFuncModeUnselected(operation: Operation)

}