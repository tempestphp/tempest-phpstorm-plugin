package com.github.tempest.framework.php.patterns

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PropertyPatternCondition
import com.jetbrains.php.lang.psi.elements.PhpAttribute

class AttributeFqnCondition<T : PhpAttribute>(namePattern: ElementPattern<String>) :
    PropertyPatternCondition<T?, String?>("withFqn", namePattern) {
    override fun getPropertyValue(o: Any): String? {
        return if (o is PhpAttribute) o.fqn else null
    }
}