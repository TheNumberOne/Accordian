package accordion.processor

import javax.annotation.processing.Messager
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class DiagnosticException(
    private val kind: Diagnostic.Kind,
    message: String = "",
    private val element: Element? = null,
    private val annotationMirror: AnnotationMirror? = null,
    private val annotationValue: AnnotationValue? = null
) : Exception(message) {
    init {
        if (annotationValue != null) {
            require(annotationMirror != null) { "Specify the annotation mirror if specifying the annotation value." }
        }
        if (annotationMirror != null) {
            require(element != null) { "Specify the annotated element if you specify the annotation mirror." }
        }
    }

    constructor(
        message: String = "",
        element: Element? = null,
        annotationMirror: AnnotationMirror? = null,
        annotationValue: AnnotationValue? = null
    ) : this(
        Diagnostic.Kind.ERROR,
        message,
        element,
        annotationMirror,
        annotationValue
    )

    fun printTo(messager: Messager) {
        element ?: run {
            messager.printMessage(kind, message)
            return
        }
        annotationMirror ?: run {
            messager.printMessage(kind, message, element)
            return
        }
        annotationValue ?: run {
            messager.printMessage(kind, message, element, annotationMirror)
            return
        }
        messager.printMessage(kind, message, element, annotationMirror, annotationValue)
    }
}

/**
 * @return true if a Diagnostic exception was thrown
 */
inline fun catchDiagnostics(consumer: (DiagnosticException) -> Unit, f: () -> Unit): Boolean {
    return try {
        f()
        false
    } catch (e: DiagnosticException) {
        consumer(e)
        true
    }
}

/**
 * @return The items that threw a diagnostic exception.
 */
inline fun <T> Set<T>.filterFailures(consumer: (DiagnosticException) -> Unit, f: (T) -> Unit): Set<T> {
    return filter { item -> catchDiagnostics(consumer) { f(item) } }.toSet()
}