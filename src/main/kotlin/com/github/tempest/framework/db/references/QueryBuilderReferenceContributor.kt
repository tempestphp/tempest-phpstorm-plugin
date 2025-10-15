package com.github.tempest.framework.db.references

import com.github.tempest.framework.db.TempestQBDictionary
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.util.findParentOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class QueryBuilderReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(MethodReference::class.java)
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val element = element as? StringLiteralExpression ?: return emptyArray()
                    val methodReference = element.findParentOfType<MethodReference>() ?: return emptyArray()
                    if (
                        methodReference.name !in TempestQBDictionary.WHERE_METHODS
                        && methodReference.name !in TempestQBDictionary.SELECT_METHODS
                    ) return emptyArray()

                    return arrayOf(FieldReference(element))
                }
            }
        )
    }
}