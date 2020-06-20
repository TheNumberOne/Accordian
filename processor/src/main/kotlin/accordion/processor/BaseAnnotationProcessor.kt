package accordion.processor

import com.google.auto.common.BasicAnnotationProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion

abstract class BaseAnnotationProcessor : BasicAnnotationProcessor() {
    private var exceptions = mutableListOf<DiagnosticException>()
    private var previousExceptions: List<DiagnosticException> = emptyList()

    protected fun addDiagnosticException(e: DiagnosticException) {
        exceptions.add(e)
    }

    override fun postRound(roundEnv: RoundEnvironment) {
        if (roundEnv.processingOver()) {
            previousExceptions.forEach {
                it.printTo(processingEnv.messager)
            }
        } else {
            previousExceptions = exceptions
            exceptions = mutableListOf()
        }
    }


    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}