/*
 * Copyright 2022 Google LLC
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.ksp.impl.symbol.kotlin

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin
import org.jetbrains.kotlin.analysis.api.analyseWithCustomToken
import org.jetbrains.kotlin.analysis.api.annotations.annotations
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertySymbol
import org.jetbrains.kotlin.analysis.api.tokens.AlwaysAccessibleValidityTokenFactory
import org.jetbrains.kotlin.psi.KtFile

class KSFileImpl(private val ktFile: KtFile) : KSFile {
    override val packageName: KSName by lazy {
        KSNameImpl(ktFile.packageFqName.asString())
    }
    override val fileName: String by lazy {
        ktFile.name
    }
    override val filePath: String by lazy {
        ktFile.virtualFilePath
    }
    override val declarations: Sequence<KSDeclaration> by lazy {
        analyseWithCustomToken(ktFile, AlwaysAccessibleValidityTokenFactory) {
            ktFile.getFileSymbol().getFileScope().getAllSymbols().map {
                when (it) {
                    is KtNamedClassOrObjectSymbol -> KSClassDeclarationImpl(it)
                    is KtFunctionSymbol -> KSFunctionDeclarationImpl(it)
                    is KtPropertySymbol -> KSPropertyDeclarationImpl(it)
                    else -> throw IllegalStateException("Unhandled ")
                }
            }
        }
    }
    override val origin: Origin = Origin.KOTLIN

    override val location: Location by lazy {
        ktFile.toLocation()
    }

    override val parent: KSNode? = null

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitFile(this, data)
    }

    override val annotations: Sequence<KSAnnotation> by lazy {
        analyseWithCustomToken(ktFile, AlwaysAccessibleValidityTokenFactory) {
            ktFile.getFileSymbol().annotations.map { KSAnnotationImpl(it) }.asSequence()
        }
    }
}
