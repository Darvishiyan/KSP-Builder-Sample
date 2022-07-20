package com.darvishiyan.processor

import com.darvishiyan.annotations.AutoBuilder
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

class AutoBuilderSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols: Sequence<KSClassDeclaration> = resolver
            .getSymbolsWithAnnotation(AutoBuilder::class.java.name)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.iterator().hasNext().not()) return emptyList()

        symbols.forEach { symbol ->
            if (symbol.modifiers.containsIgnoreCase("data").not()) {
                logger.error("You should write this function on a data class", symbol)
                return emptyList()
            }

            val flexible = symbol.annotations.getAnnotation(AutoBuilder::class.java.simpleName).arguments.getParameterValue<Boolean>(AutoBuilder.flexible)

            if (flexible)
                symbol.accept(MutableCreatorVisitor(codeGenerator, logger, options), Unit)

            symbol.accept(AutoBuilderVisitor(codeGenerator, logger, options, flexible), Unit)
        }

        return symbols.filterNot { it.validate() }.toList()
    }
}
