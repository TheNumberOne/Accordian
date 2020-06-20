package accordion.processor.util

import com.google.auto.common.MoreElements
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement

fun Element.getPackage(): PackageElement {
    @Suppress("UnstableApiUsage")
    return MoreElements.getPackage(this)
}

fun Element.isType(): Boolean {
    @Suppress("UnstableApiUsage")
    return MoreElements.isType(this)
}
