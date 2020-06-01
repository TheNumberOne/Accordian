package accordion.processor.util

import com.google.auto.common.MoreElements
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement

fun Element.getPackage(): PackageElement {
    @Suppress("UnstableApiUsage")
    return MoreElements.getPackage(this)
}