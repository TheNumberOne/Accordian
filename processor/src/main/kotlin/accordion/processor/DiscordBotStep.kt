package accordion.processor

import accordion.annotation.DiscordBot
import accordion.processor.util.getPackage
import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.SetMultimap
import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

class DiscordBotStep(
    private val exceptionConsumer: (DiagnosticException) -> Unit,
    private val filer: Filer
) : BasicAnnotationProcessor.ProcessingStep {
    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
        val elements: Set<Element> = elementsByAnnotation[DiscordBot::class.java]
        return elements.filterFailures(exceptionConsumer) { createBot(it) }
    }

    override fun annotations(): Set<Class<out Annotation>> {
        return setOf(DiscordBot::class.java)
    }

    private fun createBot(rootElement: Element) {
        val methodSpec = MethodSpec
            .methodBuilder("start")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(String::class.java, "token")
            .addStatement("\$T.create(token).login().block()", ClassName.bestGuess("discord4j.core.DiscordClient"))
            .build()

        val botName = calculateBotName(rootElement)

        val classSpec = TypeSpec
            .classBuilder(botName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(methodSpec)
            .build()

        val packageName = getPackage(rootElement)
        val file = JavaFile.builder(packageName, classSpec).build()
        filer.createSourceFile("$packageName.$botName", rootElement).openWriter().use {
            file.writeTo(it)
        }
    }

    private fun calculateBotName(rootElement: Element): String {
        return if (rootElement is TypeElement) {
            "${rootElement.simpleName}DiscordBot"
        } else {
            "DiscordBot"
        }
    }

    private fun getPackage(rootElement: Element): String {
        val p = rootElement.getPackage()
        if (p.isUnnamed) throw DiagnosticException("Please specify a package", rootElement)
        return p.qualifiedName.toString()
    }
}