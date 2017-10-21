package com.souche.android.sdk.media.editor.operation

interface OperationListener {

    fun onOperationSelected(operation: com.guoxiaoxing.phoenix.picture.edit.operation.Operation)

    fun onFuncModeUnselected(operation: com.guoxiaoxing.phoenix.picture.edit.operation.Operation)

}