package com.github.tempest.framework.router.references

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.php.patterns.AttributeFqnCondition
import com.github.tempest.framework.router.StringUtils
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl

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

                    return StringUtils.findRouterParameters(element.text)
                        .mapNotNull { alias ->
                            val nameGroup = alias.groups[1] ?: return@mapNotNull null
                            val rangeInElement = TextRange(
                                nameGroup.range.first,
                                nameGroup.range.last + 1
                            )

                            val parameter = method.getParameter(alias.groupValues[1])
                            ParameterReference(element, rangeInElement, parameters, parameter)
                        }
                        .toTypedArray()
                }

            }
        )
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(ParameterList::class.java)
                .withParent(FunctionReference::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val parameterList = element as? ParameterList ?: return emptyArray()
                    val function = parameterList.parent as? FunctionReferenceImpl ?: return emptyArray()
                    if (function.fqn != TempestFrameworkClasses.FUNCTION_URI) return emptyArray()
                    if (parameterList.parameters.isEmpty()) return emptyArray()

                    val firstParameter = function.parameters[0]
                    if (firstParameter == element) return emptyArray()

                    return parameterList
                        .node
                        .getChildren(IDENTIFIER_TOKENS)
                        .map {
                            RouteParameterReference(
                                element,
                                firstParameter,
                                it.text,
                                it.textRange.shiftLeft(element.startOffset),
                            )
                        }
                        .toTypedArray()
//                        .apply { println("found references for ${function.name} ${this.joinToString { it.toString() }}") }
                }
            }
        )
    }

    companion object {
        val IDENTIFIER_TOKENS = TokenSet.create(PhpTokenTypes.IDENTIFIER)
    }
}