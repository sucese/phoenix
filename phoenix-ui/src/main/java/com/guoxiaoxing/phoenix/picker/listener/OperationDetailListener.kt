package com.guoxiaoxing.phoenix.picture.edit.operation

import com.guoxiaoxing.phoenix.picker.model.FuncDetailsMarker

/**
 * ## UI function details result marker
 *
 * Created by lxw
 */
interface OperationDetailListener {

    fun onReceiveDetails(operation: com.guoxiaoxing.phoenix.picture.edit.operation.Operation, funcDetailsMarker: FuncDetailsMarker)
}