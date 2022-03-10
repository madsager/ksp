package com.google.devtools.ksp.impl.symbol.kotlin

import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.toKSModifiers
import org.jetbrains.kotlin.analysis.api.annotations.annotations
import org.jetbrains.kotlin.analysis.api.InvalidWayOfUsingAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtAnalysisSessionProvider
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtAnnotatedSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class KSFunctionDeclarationImpl(private val ktFunctionSymbol: KtFunctionLikeSymbol) : KSFunctionDeclaration {
    override val functionKind: FunctionKind by lazy {
        when (ktFunctionSymbol.symbolKind) {
            KtSymbolKind.CLASS_MEMBER -> FunctionKind.MEMBER
            KtSymbolKind.TOP_LEVEL -> FunctionKind.TOP_LEVEL
            KtSymbolKind.SAM_CONSTRUCTOR -> FunctionKind.LAMBDA
            else -> throw IllegalStateException("Unexpected symbol kind ${ktFunctionSymbol.symbolKind}")
        }
    }
    override val isAbstract: Boolean by lazy {
        (ktFunctionSymbol as? KtFunctionSymbol)?.modality == Modality.ABSTRACT
    }
    override val extensionReceiver: KSTypeReference? by lazy {
        analyzeWithSymbolAsContext(ktFunctionSymbol) {
            if (!ktFunctionSymbol.isExtension) {
                null
            } else {
                ktFunctionSymbol.receiverType?.let { KSTypeReferenceImpl(it) }
            }
        }
    }
    override val returnType: KSTypeReference? by lazy {
        analyzeWithSymbolAsContext(ktFunctionSymbol) {
            KSTypeReferenceImpl(ktFunctionSymbol.returnType)
        }
    }
    override val parameters: List<KSValueParameter> by lazy {
        ktFunctionSymbol.valueParameters.map { KSValueParameterImpl(it) }
    }

    override fun findOverridee(): KSDeclaration? {
        TODO("Not yet implemented")
    }

    override fun asMemberOf(containing: KSType): KSFunction {
        TODO("Not yet implemented")
    }

    override val simpleName: KSName by lazy {
        if (ktFunctionSymbol is KtFunctionSymbol) {
            KSNameImpl(ktFunctionSymbol.name.asString())
        } else {
            KSNameImpl("<init>")
        }
    }
    override val qualifiedName: KSName? by lazy {
        (ktFunctionSymbol.psi as? KtFunction)?.fqName?.asString()?.let { KSNameImpl(it) }
    }
    override val typeParameters: List<KSTypeParameter> by lazy {
        (ktFunctionSymbol as? KtFunctionSymbol)?.typeParameters?.map { KSTypeParameterImpl(it) } ?: emptyList()
    }
    override val packageName: KSName by lazy {
        containingFile?.packageName ?: KSNameImpl("")
    }
    override val parentDeclaration: KSDeclaration?
        get() = TODO("Not yet implemented")
    override val containingFile: KSFile? by lazy {
        (ktFunctionSymbol.psi?.containingFile as? KtFile)?.let { KSFileImpl(it) }
    }

    override val docString: String? by lazy {
        ktFunctionSymbol.toDocString()
    }

    override val modifiers: Set<Modifier> by lazy {
        ktFunctionSymbol.psi?.safeAs<KtFunction>()?.toKSModifiers() ?: emptySet()
    }

    override val origin: Origin by lazy {
        mapAAOrigin(ktFunctionSymbol.origin)
    }

    override val location: Location by lazy {
        ktFunctionSymbol.psi?.toLocation() ?: NonExistLocation
    }

    override val parent: KSNode?
        get() = TODO("Not yet implemented")

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitFunctionDeclaration(this, data)
    }

    override val annotations: Sequence<KSAnnotation> by lazy {
        (ktFunctionSymbol as KtAnnotatedSymbol).annotations.asSequence().map { KSAnnotationImpl(it) }
    }
    override val isActual: Boolean
        get() = TODO("Not yet implemented")
    override val isExpect: Boolean
        get() = TODO("Not yet implemented")

    override fun findActuals(): Sequence<KSDeclaration> {
        TODO("Not yet implemented")
    }

    override fun findExpects(): Sequence<KSDeclaration> {
        TODO("Not yet implemented")
    }

    override val declarations: Sequence<KSDeclaration>
        get() = TODO("Not yet implemented")
}

@OptIn(InvalidWayOfUsingAnalysisSession::class)
internal inline fun <R> analyzeWithSymbolAsContext(
    contextSymbol: KtSymbol,
    action: KtAnalysisSession.() -> R
): R {
    return KtAnalysisSessionProvider
        .getInstance(contextSymbol.psi!!.project).analyzeWithSymbolAsContext(contextSymbol, action)
}
