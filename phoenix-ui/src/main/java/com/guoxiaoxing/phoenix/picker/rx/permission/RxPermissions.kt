package com.guoxiaoxing.phoenix.picker.rx.permission

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.text.TextUtils
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject
import java.util.*

class RxPermissions(activity: Activity) {

    var mRxPermissionsFragment: RxPermissionsFragment

    init {
        mRxPermissionsFragment = getRxPermissionsFragment(activity)
    }

    private fun getRxPermissionsFragment(activity: Activity): RxPermissionsFragment {
        return if(activity.fragmentManager.findFragmentByTag(TAG) == null){
            val newFragment = RxPermissionsFragment()
            val fragmentManager = activity.fragmentManager
            fragmentManager
                    .beginTransaction()
                    .add(newFragment, TAG)
                    .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
            return newFragment
        }else{
            activity.fragmentManager.findFragmentByTag(TAG) as RxPermissionsFragment
        }
    }

    fun setLogging(logging: Boolean) {
        mRxPermissionsFragment.setLogging(logging)
    }

    /**
     * Map emitted items from the source observable into `true` if permissions in parameters
     * are granted, or `false` if not.
     *
     *
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    fun <T> ensure(vararg permissions: String): ObservableTransformer<T, Boolean> {
        return ObservableTransformer { o ->
            request(o, *permissions)
                    // Transform Observable<Permission> to Observable<Boolean>
                    .buffer(permissions.size)
                    .flatMap(Function<List<Permission>, ObservableSource<Boolean>> { permissions ->
                        if (permissions.isEmpty()) {
                            // Occurs during orientation change, when the subject receives onComplete.
                            // In that case we don't want to propagate that empty list to the
                            // subscriber, only the onComplete.
                            return@Function Observable.empty<Boolean>()
                        }
                        // Return true if all permissions are granted.
                        for (p in permissions) {
                            if (!p.granted) {
                                return@Function Observable.just(false)
                            }
                        }
                        Observable.just(true)
                    })
        }
    }

    /**
     * Map emitted items from the source observable into [Permission] objects for each
     * permission in parameters.
     *
     *
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    fun <T> ensureEach(vararg permissions: String): ObservableTransformer<T, Permission> {
        return ObservableTransformer { o -> request(o, *permissions) }
    }

    /**
     * Request permissions immediately, **must be invoked during initialization phase
     * of your application**.
     */
    fun request(vararg permissions: String): Observable<Boolean> {
        return Observable.just(TRIGGER).compose(ensure<Any>(*permissions))
    }

    /**
     * Request permissions immediately, **must be invoked during initialization phase
     * of your application**.
     */
    fun requestEach(vararg permissions: String): Observable<Permission> {
        return Observable.just(TRIGGER).compose(ensureEach<Any>(*permissions))
    }

    private fun request(trigger: Observable<*>, vararg permissions: String): Observable<Permission> {
        if (permissions == null || permissions.size == 0) {
            throw IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission")
        }
        return oneOf(trigger, pending(*permissions))
                .flatMap { requestImplementation(*permissions) }
    }

    private fun pending(vararg permissions: String): Observable<Any> {
        for (p in permissions) {
            if (!mRxPermissionsFragment.containsByPermission(p)) {
                return Observable.empty()
            }
        }
        return Observable.just(TRIGGER)
    }

    private fun oneOf(trigger: Observable<*>?, pending: Observable<*>): Observable<*> {
        if (trigger == null) {
            return Observable.just(TRIGGER)
        }
        return Observable.merge(trigger, pending)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestImplementation(vararg permissions: String): Observable<Permission> {
        val list = ArrayList<Observable<Permission>>(permissions.size)
        val unrequestedPermissions = ArrayList<String>()

        // In case of multiple permissions, we getInstance an Observable for each of them.
        // At the end, the observables are combined to have a unique response.
        for (permission in permissions) {
            mRxPermissionsFragment.log("Requesting permission " + permission)
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                list.add(Observable.just(Permission(permission, true, false)))
                continue
            }

            if (isRevoked(permission)) {
                // Revoked by a policy, return a denied Permission object.
                list.add(Observable.just(Permission(permission, false, false)))
                continue
            }

            var subject: PublishSubject<Permission>? = mRxPermissionsFragment.getSubjectByPermission(permission)
            // Create a new subject if not exists
            if (subject == null) {
                unrequestedPermissions.add(permission)
                subject = PublishSubject.create<Permission>()
                mRxPermissionsFragment.setSubjectForPermission(permission, subject)
            }

            subject?.let { list.add(it) }
        }

        if (!unrequestedPermissions.isEmpty()) {
            val unrequestedPermissionsArray = unrequestedPermissions.toTypedArray()
            requestPermissionsFromFragment(unrequestedPermissionsArray)
        }
        return Observable.concat(Observable.fromIterable(list))
    }

    /**
     * Invokes Activity.shouldShowRequestPermissionRationale and wraps
     * the returned value in an observable.
     *
     *
     * In case of multiple permissions, only emits true if
     * Activity.shouldShowRequestPermissionRationale returned true for
     * all revoked permissions.
     *
     *
     * You shouldn't call this method if all permissions have been granted.
     *
     *
     * For SDK &lt; 23, the observable will always emit false.
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, vararg permissions: String): Observable<Boolean> {
        if (!isMarshmallow) {
            return Observable.just(false)
        }
        return Observable.just(shouldShowRequestPermissionRationaleImplementation(activity, *permissions))
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun shouldShowRequestPermissionRationaleImplementation(activity: Activity, vararg permissions: String): Boolean {
        for (p in permissions) {
            if (!isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false
            }
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    internal fun requestPermissionsFromFragment(permissions: Array<String>) {
        mRxPermissionsFragment.log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions))
        mRxPermissionsFragment.requestPermissions(permissions)
    }

    /**
     * Returns true if the permission is already granted.
     *
     *
     * Always true if SDK &lt; 23.
     */
    fun isGranted(permission: String): Boolean {
        return !isMarshmallow || mRxPermissionsFragment.isGranted(permission)
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     *
     *
     * Always false if SDK &lt; 23.
     */
    fun isRevoked(permission: String): Boolean {
        return isMarshmallow && mRxPermissionsFragment.isRevoked(permission)
    }

    internal val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    internal fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        mRxPermissionsFragment.onRequestPermissionsResult(permissions, grantResults, BooleanArray(permissions.size))
    }

    companion object {

        val TAG = "RxPermissions"
        val TRIGGER = Any()
    }
}
