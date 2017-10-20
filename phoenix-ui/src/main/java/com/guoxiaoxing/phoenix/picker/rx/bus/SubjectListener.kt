package com.guoxiaoxing.phoenix.picker.rx.bus

interface SubjectListener {

    fun add(observerListener: ObserverListener)
    fun remove(observerListener: ObserverListener)
}
