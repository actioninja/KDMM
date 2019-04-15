package ninja.actio.kdmm.dm.objecttree

import java.lang.NumberFormatException
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

//TODO: Set functions

class DMVarNumberDelegate<T>(val valueFunc: () -> T) {
    var cache: T? = null
    var valid = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if(!valid) {
            try {
                cache = valueFunc()
                valid = true
            } catch (e: NumberFormatException) {
                println("Bad number in var: $valueFunc")
            }
        }
        @Suppress("UNCHECKED_CAST") //Intentional; if this throws an error something is wrong with the parser
        return cache as T
    }
}

class DMVarStringDelegate(val valueFunc: () -> String) {
    var cache: String? = null
    var valid = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if(!valid) {
            cache = valueFunc()
            valid = true
        }
        return cache as String
    }
}