package accordion.processor.util

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

@DslMarker
annotation class PoetDsl

@PoetDsl
open class CommonDsl {
    val public = Modifier.PUBLIC
    val final = Modifier.FINAL
    val static = Modifier.STATIC
    val private = Modifier.PRIVATE

    val void = TypeName.VOID

    inline fun <reified T> param(name: String, vararg modifiers: Modifier): ParameterSpec {
        return ParameterSpec.builder(typeName<T>(), name, *modifiers).build()
    }

    fun param(type: TypeName, name: String, vararg modifiers: Modifier): ParameterSpec {
        return ParameterSpec.builder(type, name, *modifiers).build()
    }

    fun m(vararg modifiers: Modifier): Array<out Modifier> = modifiers
}

@PoetDsl
class TypeSpecDsl(val builder: TypeSpec.Builder) : CommonDsl() {
    fun build(): TypeSpec = builder.build()

    val method: MethodSpec.Builder get() = MethodSpec.methodBuilder("generatedMethod")
    val constructor: MethodSpec.Builder get() = MethodSpec.constructorBuilder()

    operator fun MethodSpec.Builder.invoke(vararg modifiers: Modifier): MethodSpec.Builder {
        return addModifiers(*modifiers)
    }

    operator fun MethodSpec.Builder.invoke(vararg modifiers: Modifier, dsl: MethodSpecDsl.() -> Unit): MethodSpec {
        return this(*modifiers)(dsl)
    }

    operator fun MethodSpec.Builder.invoke(returnType: TypeName): MethodSpec.Builder {
        return returns(returnType)
    }

    operator fun MethodSpec.Builder.invoke(name: String): MethodSpec.Builder {
        // MethodSpec.Builder overwrites the name when setting the name.
        val retType = build().returnType
        return setName(name).returns(retType)
    }

    operator fun MethodSpec.Builder.invoke(name: String, dsl: MethodSpecDsl.() -> Unit): MethodSpec {
        return this(name)(dsl)
    }

    operator fun MethodSpec.Builder.invoke(dsl: MethodSpecDsl.() -> Unit): MethodSpec {
        return MethodSpecDsl(this).apply(dsl).build().also {
            builder.addMethod(it)
        }
    }

    operator fun MethodSpec.Builder.invoke(vararg params: ParameterSpec): MethodSpec.Builder {
        return addParameters(params.asIterable())
    }

    operator fun MethodSpec.Builder.invoke(vararg params: ParameterSpec, dsl: MethodSpecDsl.() -> Unit): MethodSpec {
        return this(*params)(dsl)
    }

    fun modifiers(vararg modifiers: Modifier) {
        builder.addModifiers(*modifiers)
    }

    inline fun <reified T> field(
        name: String,
        vararg modifiers: Modifier,
        dsl: FieldSpecDsl.() -> Unit = {}
    ): FieldSpec {
        return FieldSpecDsl(FieldSpec.builder(typeName<T>(), name, *modifiers)).apply(dsl).build().also {
            builder.addField(it)
        }
    }

    fun field(type: TypeName, name: String, vararg modifiers: Modifier, dsl: FieldSpecDsl.() -> Unit = {}): FieldSpec {
        return FieldSpecDsl(FieldSpec.builder(type, name, *modifiers)).apply(dsl).build().also {
            builder.addField(it)
        }
    }

    fun method(name: String, methodBuilder: MethodSpecDsl.() -> Unit): MethodSpec {
        return MethodSpecDsl(MethodSpec.methodBuilder(name)).apply(methodBuilder).build().also {
            builder.addMethod(it)
        }
    }

    fun method(
        modifiers: Iterable<Modifier>,
        returnType: TypeName,
        name: String,
        methodBuilder: MethodSpecDsl.() -> Unit
    ): MethodSpec {
        return method(name) {
            modifiers(modifiers)
            returns(returnType)
            methodBuilder()
        }
    }

    operator fun Iterable<Modifier>.invoke(
        returnType: TypeName,
        methodName: String,
        vararg parameters: ParameterSpec,
        methodBuilder: MethodSpecDsl.() -> Unit
    ): MethodSpec {
        return method(this, returnType, methodName) {
            parameters(parameters.asIterable())
            methodBuilder()
        }
    }

    fun clazz(name: ClassName, dsl: TypeSpecDsl.() -> Unit) {
        clazzImpl(name, dsl).also {
            builder.addType(it)
        }
    }

    fun clazz(name: String, dsl: TypeSpecDsl.() -> Unit) {
        clazzImpl(name, dsl).also {
            builder.addType(it)
        }
    }
}

@PoetDsl
class MethodSpecDsl(private var builder: MethodSpec.Builder) : CommonDsl() {
    fun build(): MethodSpec = builder.build()

    fun modifiers(vararg modifiers: Modifier) {
        builder = builder.addModifiers(*modifiers)
    }

    fun modifiers(modifiers: Iterable<Modifier>) {
        builder = builder.addModifiers(modifiers)
    }

    fun parameter(type: TypeName, name: String, vararg modifiers: Modifier): ParameterSpec {
        return ParameterSpec.builder(type, name, *modifiers).build().also {
            builder = builder.addParameter(it)
        }
    }

    fun parameter(type: KClass<*>, name: String, vararg modifiers: Modifier): ParameterSpec {
        return ParameterSpec.builder(type.java, name, *modifiers).build().also {
            builder = builder.addParameter(it)
        }
    }

    fun params(): List<ParameterSpec> = builder.parameters

    fun parameters(params: Iterable<ParameterSpec>) {
        builder = builder.addParameters(params)
    }

    operator fun String.invoke(vararg params: Any) {
        builder = builder.addStatement(replace('#', '$'), *params.map {
            if (it is KClass<*>) it.java else it
        }.toTypedArray())
    }

    fun returns(type: TypeName) {
        builder = builder.returns(type)
    }
}

@PoetDsl
class FieldSpecDsl(val builder: FieldSpec.Builder) : CommonDsl() {
    fun build(): FieldSpec = builder.build()

    operator fun String.invoke(vararg params: Any) {
        builder.initializer(replace('#', '$'), *params.map {
            if (it is KClass<*>) it.java else it
        }.toTypedArray())
    }
}

private fun clazzImpl(name: String, builder: TypeSpecDsl.() -> Unit): TypeSpec {
    return TypeSpecDsl(TypeSpec.classBuilder(name)).apply(builder).build()
}

private fun clazzImpl(name: ClassName, builder: TypeSpecDsl.() -> Unit): TypeSpec {
    return TypeSpecDsl(TypeSpec.classBuilder(name)).apply(builder).build()
}

fun clazz(name: String, builder: TypeSpecDsl.() -> Unit): TypeSpec {
    return clazzImpl(name, builder)
}

fun clazz(name: ClassName, builder: TypeSpecDsl.() -> Unit): TypeSpec {
    return clazzImpl(name, builder)
}