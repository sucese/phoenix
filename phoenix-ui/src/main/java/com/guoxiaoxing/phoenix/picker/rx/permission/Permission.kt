package com.guoxiaoxing.phoenix.picker.rx.permission

class Permission @JvmOverloads internal constructor(val name: String, val granted: Boolean, val shouldShowRequestPermissionRationale: Boolean = false) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as Permission?

        if (granted != that!!.granted) return false
        if (shouldShowRequestPermissionRationale != that.shouldShowRequestPermissionRationale)
            return false
        return name == that.name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + if (granted) 1 else 0
        result = 31 * result + if (shouldShowRequestPermissionRationale) 1 else 0
        return result
    }

    override fun toString(): String {
        return "Permission{" +
                "name='" + name + '\'' +
                ", granted=" + granted +
                ", shouldShowRequestPermissionRationale=" + shouldShowRequestPermissionRationale +
                '}'
    }
}
