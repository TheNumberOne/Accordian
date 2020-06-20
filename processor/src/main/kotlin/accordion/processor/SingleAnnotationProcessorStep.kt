package accordion.processor

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.SetMultimap
import javax.lang.model.element.Element
import kotlin.reflect.KClass

abstract class SingleAnnotationProcessorStep(
    annotation: KClass<out Annotation>,
    private val exceptionConsumer: (DiagnosticException) -> Unit
): BasicAnnotationProcessor.ProcessingStep {
    private val annotationClass = annotation.java

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
        val elements = elementsByAnnotation[annotationClass]
        return elements.filterFailures(exceptionConsumer, this::process)
    }

    abstract fun process(element: Element)

    override fun annotations(): Set<Class<out Annotation>> {
        return setOf(annotationClass)
    }

}