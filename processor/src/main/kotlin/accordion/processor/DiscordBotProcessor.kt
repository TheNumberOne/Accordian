package accordion.processor

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class DiscordBotProcessor : BasicAnnotationProcessor() {
    private var exceptions = mutableListOf<DiagnosticException>()
    private var previousExceptions: List<DiagnosticException> = emptyList()

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun initSteps(): Iterable<ProcessingStep> {
        return listOf(DiscordBotStep({ exceptions.add(it) }, processingEnv.filer))
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
}

