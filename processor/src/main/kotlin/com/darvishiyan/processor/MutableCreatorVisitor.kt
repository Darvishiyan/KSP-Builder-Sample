package com.darvishiyan.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

class MutableCreatorVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : KSVisitorVoid() {

    private lateinit var file: OutputStream

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val fileName = "Mutable$className"

        file = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = packageName,
            fileName = fileName
        )

        file += "package $packageName\n\n"


        file += "internal class $fileName(\n"

        classDeclaration.getAllProperties().forEach(::visitPropertyDeclaration)
        file += ") {\n"
        file += "\tfun to$className(): $className = $className(\n"

        classDeclaration.getAllProperties().map { it.simpleName.asString() }.forEach {
            file += "\t\t${it} = ${it},\n"
        }
        file += "\t)\n"
        file += "}"

        file.close()
    }

    private fun visitPropertyDeclaration(property: KSPropertyDeclaration) {
        val name: String = property.simpleName.asString()
        val typeResolve: KSType = property.type.resolve()
        val type: String = typeResolve.declaration.qualifiedName?.asString() ?: ""
        val nullable: String = if (typeResolve.nullability == Nullability.NULLABLE) "?" else ""
        val genericArguments: List<KSTypeArgument> = property.type.element?.typeArguments ?: emptyList()
        val generic: String = visitTypeArguments(genericArguments, logger::error)
        file += "\tvar $name: $type$generic$nullable,\n"
    }
}
