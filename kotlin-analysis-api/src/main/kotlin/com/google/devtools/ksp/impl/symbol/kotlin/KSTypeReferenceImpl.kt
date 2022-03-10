package com.google.devtools.ksp.impl.symbol.kotlin

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import org.jetbrains.kotlin.analysis.api.annotations.annotations
import org.jetbrains.kotlin.analysis.api.types.KtType

class KSTypeReferenceImpl(private val ktType: KtType) : KSTypeReference {
    override val element: KSReferenceElement?
        get() = TODO("Not yet implemented")

    override fun resolve(): KSType {
        return KSTypeImpl(ktType)
    }

    override val annotations: Sequence<KSAnnotation> by lazy {
        ktType.annotations.map { KSAnnotationImpl(it) }.asSequence()
    }

    override val origin: Origin = Origin.KOTLIN

    override val location: Location
        get() = TODO("Not yet implemented")

    override val parent: KSNode?
        get() = TODO("Not yet implemented")

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitTypeReference(this, data)
    }

    override val modifiers: Set<Modifier>
        get() = TODO("Not yet implemented")
}
