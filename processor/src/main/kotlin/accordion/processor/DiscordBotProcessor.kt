package accordion.processor

import accordion.annotation.CommandClass
import accordion.annotation.DiscordBot
import accordion.processor.model.*
import accordion.processor.util.clazz
import accordion.processor.util.getPackage
import accordion.processor.util.typeName
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

private val annotations = setOf(CommandClass::class, DiscordBot::class)

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class DiscordBotProcessor : AbstractProcessor() {
    val model = MutableModel(mutableSetOf(), mutableSetOf())

    override fun process(rootElements: Set<TypeElement>, env: RoundEnvironment): Boolean {
        try {
            env.getElementsAnnotatedWith(CommandClass::class.java).forEach {
                processCommandClass(it, model)
            }
            env.getElementsAnnotatedWith(DiscordBot::class.java).forEach {
                processBot(it, model)
            }
            if (env.processingOver()) {
                model.build().forEach { (botName, classes) ->
                    val classSpec = buildClass(botName, classes)
                    JavaFile.builder(botName.packageName(), classSpec).build().writeTo(processingEnv.filer)
                }
            }
        } catch (e: DiagnosticException) {
            e.printTo(processingEnv.messager)
        }
        return true
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return annotations.map { it.qualifiedName!! }.toSet()
    }
}

fun processBot(element: Element, model: MutableModel) {
    val name = if (element is TypeElement) {
        "${element.simpleName}DiscordBot"
    } else {
        "DiscordBot"
    }

    val p = element.getPackage()
    if (p.isUnnamed) throw DiagnosticException("Please specify a package", element)
    val packageName = p.qualifiedName.toString()
    model.bots.add(ClassName.get(packageName, name))
}

fun processCommandClass(element: Element, model: MutableModel) {
    if (element.kind != ElementKind.CLASS) {
        throw DiagnosticException("Can only annotate classes with CommandClass", element)
    }
    check(element is TypeElement)
    val constructors = ElementFilter.constructorsIn(element.enclosedElements)
        .filter { Modifier.PRIVATE !in it.modifiers && Modifier.PROTECTED !in it.modifiers }
    if (constructors.size != 1) {
        throw DiagnosticException("Must provide exactly one constructor, which may be default and cannot be private or protected.")

    }
    val constructor = constructors[0]
    val params = constructor.parameters.map {
        TypeName.get(it.asType())
    }
    val commandClass = CommandClassDescription(ClassName.get(element), params)
    model.commandClasses.add(commandClass)
}

private fun buildClass(botClassName: ClassName, model: Set<CommandClassDescription>): TypeSpec {
    val allParams = model.flatMap { it.params }.toSet()

    return clazz(botClassName) {
        modifiers(public, final)

        val gatewayMono = field<Mono<GatewayDiscordClient>>("gatewayMono", private, final)

        val fields = allParams.map { param ->
            field(param, getVariableName(param), private, final)
        }

        val params = allParams.map {
            param(it, getVariableName(it))
        }.toTypedArray()

        constructor(private)(param<Mono<GatewayDiscordClient>>("gatewayMono"), *params) {
            val (innerGatewayMono) = params()
            "this.#N = #N"(gatewayMono, innerGatewayMono)
            params.forEach {
                "this.#N = #N"(it, it)
            }
        }

        val applicationMono = method(public)(typeName<Mono<Void>>())("applicationMono") {
            "return #N.then()"(gatewayMono)
        }

        val block = method(public)(void)("block") {
            "#N().block()"(applicationMono)
        }

        val run = method(public)(void)("run") {
            "#1T scheduler = #2T.newParallel(#3S, #2T.DEFAULT_POOL_SIZE, false)"(
                Scheduler::class,
                Schedulers::class,
                botClassName.simpleName()
            )
            "#N().subscribeOn(scheduler).subscribe()"(applicationMono)
        }

        val builderType = botClassName.nestedClass("Builder")

        clazz(builderType) {
            modifiers(public, static, final)

            val builderGatewayMono = field<Mono<GatewayDiscordClient>>("gatewayMono", private)

            val builderFields = allParams.map { param ->
                field(param, getVariableName(param), private, final) { "new #T()"(param) }
            }

            constructor(private) { }

            method(public)(botClassName)("build") {
                "return new #T(#T.requireNonNull(#N, #S)${", #T.requireNonNull(#N)".repeat(allParams.size)})"(
                    botClassName,
                    Objects::class,
                    builderGatewayMono,
                    "Please specify a token or a mono supplying the gateway.",
                    *builderFields.flatMap { listOf(Objects::class, it) }.toTypedArray()
                )
            }

            method(public)(builderType)("token")(param<String>("token")) {
                val (token) = params()
                "#N = #T.create(#N).login()"(builderGatewayMono, DiscordClient::class, token)
                "return this"()
            }

            method(public)(builderType)("gatewayMono")(param<Mono<GatewayDiscordClient>>("gatewayMono")) {
                val (innerGatewayMono) = params()
                "this.#N = #N"(builderGatewayMono, innerGatewayMono)
                "return this"()
            }

            allParams.mapIndexed { i, paramType ->
                method(public)(builderType)(getVariableName(paramType))(param(paramType, getVariableName(paramType))) {
                    val (innerParamName) = params()
                    "this.#N = #N"(builderFields[i], innerParamName)
                    "return this"()
                }
            }
        }

        val builder = method(public, static)(builderType)("builder") {
            "return new #T()"(builderType)
        }

        val create = method(public, static)(botClassName)("create")(param<String>("token")) {
            val (token) = params()
            "return #N().token(#N).build()"(builder, token)
        }

        method(public, static)(void)("runBlocking")(param<String>("token")) {
            val (token) = params()
            "#N(#N).#N()"(create, token, block)
        }

        method(public, static)(void)("run")(param<String>("token")) {
            val (token) = params()
            "#N(#N).#N()"(create, token, run)
        }
    }
}

fun getVariableName(typeName: TypeName): String {
    return when (typeName) {
        is ClassName -> typeName.simpleName().decapitalize()
        else -> error("Only accepts classes as params for now.")
    }
}
