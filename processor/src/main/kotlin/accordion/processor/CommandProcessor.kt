package accordion.processor

import accordian.internal.DiscordCommand
import accordion.annotation.Command
import accordion.annotation.CommandClass
import accordion.processor.util.MethodId
import accordion.processor.util.getMethodId
import accordion.processor.util.typeName
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import discord4j.core.event.domain.message.MessageCreateEvent
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import javax.annotation.processing.Filer
import javax.annotation.processing.Generated
import javax.annotation.processing.Processor
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class CommandProcessor : BaseAnnotationProcessor() {
    override fun initSteps(): Iterable<ProcessingStep> {
        return listOf(CommandProcessorStep(processingEnv.filer, this::addDiagnosticException))
    }
}

class CommandProcessorStep(private val filer: Filer, exceptionConsumer: (DiagnosticException) -> Unit) :
    SingleAnnotationProcessorStep(Command::class, exceptionConsumer) {

    override fun process(element: Element) {
        if (element.kind != ElementKind.METHOD || element !is ExecutableElement) {
            throw DiagnosticException("Can only annotate methods with @Command", element)
        }
        val methodId = getMethodId(element)
        val commandName = methodId.methodName
        if (methodId.args.isNotEmpty()) {
            throw DiagnosticException("Can't currently process methods with parameters.")
        }
        val returnType = TypeName.get(element.returnType)
        if (returnType != typeName<String>()) {
            throw DiagnosticException("Can't currently process methods with a return type that is not string.")
        }

        val className = commandClassNameOf(methodId)
        JavaFile.builder(
            className.packageName(),
            TypeSpec.classBuilder(className).apply {
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                addSuperinterface(DiscordCommand::class.java)
                addAnnotation(AnnotationSpec.builder(Generated::class.java).apply {
                    addMember("value", "\$S", CommandProcessor::class.qualifiedName)
                    addMember("date", "\$S", Instant.now())
                }.build())
                addAnnotation(CommandClass::class.java)
                addMethod(MethodSpec.methodBuilder("names").apply {
                    addModifiers(Modifier.PUBLIC)
                    returns(typeName<Flux<String>>())
                    addStatement("return \$T.just(\$S)", Flux::class.java, commandName)
                }.build())
                val enclosingClass = FieldSpec
                    .builder(methodId.enclosingClass, "enclosingClass", Modifier.PRIVATE, Modifier.FINAL)
                    .build()
                addField(enclosingClass)
                addMethod(MethodSpec.constructorBuilder().apply {
                    val innerEnclosingClass = ParameterSpec
                        .builder(methodId.enclosingClass, "enclosingClass")
                        .build()
                    addParameter(innerEnclosingClass)
                    addStatement("this.\$N = \$N", enclosingClass, innerEnclosingClass)
                }.build())
                addMethod(MethodSpec.methodBuilder("process").apply {
                    addModifiers(Modifier.PUBLIC)
                    val messageCreateEvent = ParameterSpec
                        .builder(MessageCreateEvent::class.java, "messageCreateEvent")
                        .build()
                    addParameter(messageCreateEvent)
                    addParameter(String::class.java, "message")
                    returns(typeName<Mono<Void>>())
                    addStatement("\$T result = \$N.\$N()", String::class.java, enclosingClass, methodId.methodName)
                    beginControlFlow("if (result == null)")
                    addStatement("return \$T.empty()", Mono::class.java)
                    nextControlFlow("else")
                    addStatement(
                        "return \$N.getMessage().getChannel().flatMap(channel -> channel.createMessage(result)).then()",
                        messageCreateEvent
                    )
                    endControlFlow()
                }.build())
            }.build()
        ).build().writeTo(filer)
    }

    private fun commandClassNameOf(methodId: MethodId) =
        ClassName.get(
            methodId.enclosingClass.packageName(),
            listOf(
                "Accordion",
                methodId.enclosingClass.simpleNames().joinToString("_"),
                methodId.methodName,
                methodId.hashCode()
            ).joinToString("_")
        )
}