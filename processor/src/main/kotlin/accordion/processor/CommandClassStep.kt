//package accordion.processor
//
//import accordion.annotation.CommandClass
//import accordion.processor.model.CommandClassDescription
//import accordion.processor.model.MutableModel
//import accordion.processor.model.ParamType
//import com.squareup.javapoet.ClassName
//import com.squareup.javapoet.TypeName
//import javax.lang.model.element.Element
//import javax.lang.model.element.ElementKind
//import javax.lang.model.element.Modifier
//import javax.lang.model.element.TypeElement
//import javax.lang.model.type.DeclaredType
//import javax.lang.model.type.TypeKind
//import javax.lang.model.type.TypeMirror
//import javax.lang.model.util.ElementFilter
//
//class CommandClassStep(
//    private val mutableModel: MutableModel,
//    exceptionConsumer: (DiagnosticException) -> Unit
//) : SingleAnnotationProcessorStep(
//    CommandClass::class,
//    exceptionConsumer
//) {
//    override fun process(element: Element) {
//        if (element.kind != ElementKind.CLASS) {
//            throw DiagnosticException("Can only annotate classes with CommandClass", element)
//        }
//        check(element is TypeElement)
//        val constructors = ElementFilter.constructorsIn(element.enclosedElements)
//            .filter { Modifier.PRIVATE !in it.modifiers && Modifier.PROTECTED !in it.modifiers }
//        if (constructors.size != 1) {
//            throw DiagnosticException("Must provide exactly one constructor, which may be default and cannot be private or protected.")
//
//        }
//        val constructor = constructors[0]
//        val params = constructor.parameters.map {
//            val type = it.asType()
//            val name = TypeName.get(type)
//            val paramType = if (isDefaultConstructable(type)) {
//                ParamType.DefaultConstructable
//            } else {
//                ParamType.Required
//            }
//            CommandClassParam(name, paramType)
//        }
//        val commandClass = CommandClassDescription(ClassName.get(element), params)
//        mutableModel.commandClasses.add(commandClass)
//    }
//
//    private fun isDefaultConstructable(type: TypeMirror): Boolean {
//        if (type.kind != TypeKind.DECLARED) return false
//        check(type is DeclaredType)
//        val constructors = ElementFilter.constructorsIn(type.asElement().enclosedElements)
//            .filter { Modifier.PRIVATE !in it.modifiers && Modifier.PROTECTED !in it.modifiers }
//            .filter { it.parameters.isEmpty() }
//        return constructors.size == 1
//    }
//}