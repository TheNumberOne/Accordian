package accordion.processor.model

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName

class MutableModel(
    val commandClasses: MutableSet<CommandClassDescription>,
    val bots: MutableSet<ClassName>
) {
    fun build(): List<Model> {
        return bots.map { className ->
            val packageName = className.packageName()
            Model(
                className,
                commandClasses.filter {
                    val p = it.name.packageName()
                    p.startsWith(packageName) && (p.length == packageName.length || p[packageName.length] == '.')
                }.toSet()
            )
        }
    }
}

data class Model(val clazz: ClassName, val commandClasses: Set<CommandClassDescription>) {
    fun getAllParams(): Set<TypeName> = commandClasses.flatMap { it.params }.toSet()
}

data class CommandClassDescription(
    val name: ClassName,
    val params: List<TypeName>
)
