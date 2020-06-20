package accordion.processor.util

import com.squareup.javapoet.TypeName
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

inline fun <reified T> typeName(): TypeName =
    TypeName.get(typeLiteral<T>().type)

open class TypeLiteral<T> {
    val type: Type
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
}

inline fun <reified T> typeLiteral(): TypeLiteral<T> = object : TypeLiteral<T>() {}