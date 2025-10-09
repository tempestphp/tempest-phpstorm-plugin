package com.github.tempest.framework.router.references

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.common.utils.StringUtils
import com.github.tempest.framework.php.patterns.AttributeFqnCondition
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouteParametersReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(PhpAttribute::class.java)
                        .with(AttributeFqnCondition(StandardPatterns.string().oneOf(TempestFrameworkClasses.ROUTES)))
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val element = element as? StringLiteralExpression ?: return emptyArray()
                    val attribute = element.parent.parent as? PhpAttribute ?: return emptyArray()
                    val method = attribute.owner as? Method ?: return emptyArray()
                    val parameters = method.parameters
                    if (parameters.isEmpty()) return emptyArray()

                    return StringUtils.findTextBetweenParenthesis(element.text)
                        .map { alias ->
                            val rangeInElement = TextRange(
                                alias.range.first + 1,
                                alias.range.last
                            )

                            val parameter = method.getParameter(alias.groupValues[1])
                            ParameterReference(element, rangeInElement, parameters, parameter)
                        }
                        .toTypedArray()
                }

            }
        )
    }
}