package com.guoxiaoxing.phoenix.picker.rx.bus

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*

class RxBus private constructor() {

    private val subscriptionsByEventType = HashMap<Class<*>, MutableList<Disposable>>()

    private val eventTypesBySubscriber = HashMap<Any, MutableList<Class<*>>>()

    private val subscriberMethodByEventType = HashMap<Class<*>, MutableList<SubscriberMethod>>()

    private val bus: Subject<Any>

    init {
        this.bus = PublishSubject.create<Any>().toSerialized()
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     * @param eventType 事件类型
     * *
     * @return return
     */
    fun <T> toObservable(eventType: Class<T>): Flowable<T> {
        return bus.toFlowable(BackpressureStrategy.BUFFER).ofType(eventType)
    }

    /**
     * 根据传递的code和 eventType 类型返回特定类型(eventType)的 被观察者
     * @param code      事件code
     * *
     * @param eventType 事件类型
     */
    private fun <T> toObservable(code: Int, eventType: Class<T>): Flowable<T> {
        return bus.toFlowable(BackpressureStrategy.BUFFER).ofType(Message::class.java)
                .filter { o -> o.code == code && eventType.isInstance(o.`object`) }.map { o -> o.`object` }.cast(eventType)
    }

    /**
     * 注册
     * @param subscriber 订阅者
     */
    fun register(subscriber: Any) {
        val subClass = subscriber.javaClass
        val methods = subClass.declaredMethods
        for (method in methods) {
            if (method.isAnnotationPresent(Subscribe::class.java)) {
                //获得参数类型
                val parameterType = method.parameterTypes
                //参数不为空 且参数个数为1
                if (parameterType != null && parameterType.size == 1) {

                    val eventType = parameterType[0]

                    addEventTypeToMap(subscriber, eventType)
                    val sub = method.getAnnotation(Subscribe::class.java)
                    val code = sub.code
                    val threadMode = sub.threadMode

                    val subscriberMethod = SubscriberMethod(subscriber, method, eventType, code, threadMode)
                    addSubscriberToMap(eventType, subscriberMethod)

                    addSubscriber(subscriberMethod)
                } else if (parameterType == null || parameterType.size == 0) {

                    val eventType = BusData::class.java

                    addEventTypeToMap(subscriber, eventType)
                    val sub = method.getAnnotation(Subscribe::class.java)
                    val code = sub.code
                    val threadMode = sub.threadMode

                    val subscriberMethod = SubscriberMethod(subscriber, method, eventType, code, threadMode)
                    addSubscriberToMap(eventType, subscriberMethod)

                    addSubscriber(subscriberMethod)
                }
            }
        }
    }

    /**
     * 将event的类型以订阅中subscriber为key保存到map里
     * @param subscriber 订阅者
     * *
     * @param eventType  event类型
     */
    private fun addEventTypeToMap(subscriber: Any, eventType: Class<*>) {
        var eventTypes: MutableList<Class<*>>? = eventTypesBySubscriber[subscriber]
        if (eventTypes == null) {
            eventTypes = ArrayList<Class<*>>()
            eventTypesBySubscriber.put(subscriber, eventTypes)
        }

        if (!eventTypes.contains(eventType)) {
            eventTypes.add(eventType)
        }
    }

    /**
     * 将注解方法信息以event类型为key保存到map中
     * @param eventType        event类型
     * *
     * @param subscriberMethod 注解方法信息
     */
    private fun addSubscriberToMap(eventType: Class<*>, subscriberMethod: SubscriberMethod) {
        var subscriberMethods: MutableList<SubscriberMethod>? = subscriberMethodByEventType[eventType]
        if (subscriberMethods == null) {
            subscriberMethods = ArrayList<SubscriberMethod>()
            subscriberMethodByEventType.put(eventType, subscriberMethods)
        }

        if (!subscriberMethods.contains(subscriberMethod)) {
            subscriberMethods.add(subscriberMethod)
        }
    }

    /**
     * 将订阅事件以event类型为key保存到map,用于取消订阅时用
     * @param eventType  event类型
     * *
     * @param disposable 订阅事件
     */
    private fun addSubscriptionToMap(eventType: Class<*>, disposable: Disposable) {
        var disposables: MutableList<Disposable>? = subscriptionsByEventType[eventType]
        if (disposables == null) {
            disposables = ArrayList<Disposable>()
            subscriptionsByEventType.put(eventType, disposables)
        }

        if (!disposables.contains(disposable)) {
            disposables.add(disposable)
        }
    }

    /**
     * 用RxJava添加订阅者
     * @param subscriberMethod d
     */
    private fun addSubscriber(subscriberMethod: SubscriberMethod) {
        val flowable: Flowable<*>
        if (subscriberMethod.code == -1) {
            flowable = toObservable(subscriberMethod.eventType)
        } else {
            flowable = toObservable(subscriberMethod.code, subscriberMethod.eventType)
        }
        val subscription = postToObservable(flowable, subscriberMethod)
                .subscribe { o -> callEvent(subscriberMethod, o) }
        addSubscriptionToMap(subscriberMethod.subscriber.javaClass, subscription)
    }

    /**
     * 用于处理订阅事件在那个线程中执行
     * @param observable       d
     * *
     * @param subscriberMethod d
     * *
     * @return Observable
     */
    private fun postToObservable(observable: Flowable<*>, subscriberMethod: SubscriberMethod): Flowable<*> {
        val scheduler: Scheduler
        when (subscriberMethod.threadMode) {
            ThreadMode.MAIN -> scheduler = AndroidSchedulers.mainThread()

            ThreadMode.NEW_THREAD -> scheduler = Schedulers.newThread()

            ThreadMode.CURRENT_THREAD -> scheduler = Schedulers.trampoline()
            else -> throw IllegalStateException("Unknown thread mode: " + subscriberMethod.threadMode)
        }
        return observable.observeOn(scheduler)
    }

    /**
     * 回调到订阅者的方法中
     * @param method code
     * *
     * @param object obj
     */
    private fun callEvent(method: SubscriberMethod, `object`: Any) {
        val eventClass = `object`.javaClass
        val methods = subscriberMethodByEventType[eventClass]
        if (methods != null && methods.size > 0) {
            for (subscriberMethod in methods) {
                val sub = subscriberMethod.method.getAnnotation(Subscribe::class.java)
                val c = sub.code
                if (c == method.code && method.subscriber == subscriberMethod.subscriber && method.method == subscriberMethod.method) {
                    subscriberMethod.invoke(`object`)
                }
            }
        }
    }

    /**
     * 是否注册
     * @param subscriber
     * *
     * @return
     */
    @Synchronized fun isRegistered(subscriber: Any): Boolean {
        return eventTypesBySubscriber.containsKey(subscriber)
    }

    /**
     * 取消注册
     * @param subscriber object
     */
    fun unregister(subscriber: Any) {
        val subscribedTypes = eventTypesBySubscriber[subscriber]
        if (subscribedTypes != null) {
            for (eventType in subscribedTypes) {
                unSubscribeByEventType(subscriber.javaClass)
                unSubscribeMethodByEventType(subscriber, eventType)
            }
            eventTypesBySubscriber.remove(subscriber)
        }
    }

    /**
     * subscriptions unsubscribe
     * @param eventType eventType
     */
    private fun unSubscribeByEventType(eventType: Class<*>) {
        val disposables = subscriptionsByEventType[eventType]
        if (disposables != null) {
            val iterator = disposables.iterator()
            while (iterator.hasNext()) {
                val disposable = iterator.next()
                if (disposable != null && !disposable.isDisposed) {
                    disposable.dispose()
                    iterator.remove()
                }
            }
        }
    }

    /**
     * 移除subscriber对应的subscriberMethods
     * @param subscriber subscriber
     * *
     * @param eventType  eventType
     */
    private fun unSubscribeMethodByEventType(subscriber: Any, eventType: Class<*>) {
        val subscriberMethods = subscriberMethodByEventType[eventType]
        if (subscriberMethods != null) {
            val iterator = subscriberMethods.iterator()
            while (iterator.hasNext()) {
                val subscriberMethod = iterator.next()
                if (subscriberMethod.subscriber == subscriber) {
                    iterator.remove()
                }
            }
        }
    }

    fun send(code: Int, o: Any) {
        bus.onNext(Message(code, o))
    }

    fun post(o: Any) {
        bus.onNext(o)
    }

    fun send(code: Int) {
        bus.onNext(Message(code, BusData()))
    }

    private inner class Message {
        var code: Int = 0
            set
        var `object`: Any? = null
            set

        constructor() {}

        constructor(code: Int, o: Any) {
            this.code = code
            this.`object` = o
        }
    }

    companion object {
        val default: RxBus by lazy { RxBus() }
    }
}
