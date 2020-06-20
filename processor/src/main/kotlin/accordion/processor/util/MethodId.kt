package accordion.processor.util

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

data class MethodId(val enclosingClass: ClassName, val methodName: String, val args: List<TypeName>)

fun getMethodId(element: ExecutableElement): MethodId {
    val enclosingElement = element.enclosingElement
    check(enclosingElement.isType() && enclosingElement is TypeElement) { "Can't find the enclosing class" }
    val name = element.simpleName.toString()
    val params = element.parameters.map { TypeName.get(it.asType()).withoutAnnotations() }
    return MethodId(ClassName.get(enclosingElement).withoutAnnotations(), name, params)
}

