package com.guoxiaoxing.phoenix.picture.edit.operation

import com.guoxiaoxing.phoenix.picture.edit.model.FuncDetailsMarker

/**
 * ## UI function details result marker
 *
 * Created by lxw
 */
interface OperationDetailListener {

    fun onReceiveDetails(operation: Operation, funcDetailsMarker: FuncDetailsMarker)
}