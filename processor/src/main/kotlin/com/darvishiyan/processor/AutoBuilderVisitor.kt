package com.darvishiyan.processor

import com.darvishiyan.annotations.BuilderProperty
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

class AutoBuilderVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
    private val flexible: Boolean,
) : KSVisitorVoid() {

    private lateinit var file: OutputStream

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val objectName = if (flexible) "mutable$className" else className.replaceFirstChar { c -> c.lowercase() }
        val fileName = "${className}Builder"
        val targetName = if (flexible) "Mutable$className" else className

        file = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = packageName,
            fileName = fileName
        )


        file += "package $packageName\n\n"

        file += "class $fileName(\n"
        classDeclaration.getAllProperties().forEach {
            visitPropertyDeclarationToCreateConstructor(it)
        }
        file += ") {\n\n"

        file += "\tprivate val $objectName: $targetName = $targetName(\n"
        classDeclaration.getAllProperties().forEach {
            visitPropertyDeclarationToCreatePrivateVariable(it)
        }
        file += "\t)\n\n"

        classDeclaration.getAllProperties().forEach {
            visitPropertyDeclarationToCreateBuilderFunctions(it, fileName, objectName)
        }

        file += "\tfun build(): $className = $objectName"
        file += if (flexible) ".to$className()\n"
        else "\n"

        file += "\n}"

        file.close()
    }

    private fun visitPropertyDeclarationToCreateBuilderFunctions(property: KSPropertyDeclaration, fileName: String, objectName: String) {
        if (property.annotations.hasAnnotation(BuilderProperty::class.java.simpleName)) {
            val name: String = property.simpleName.asString()
            val type: String = property.type.resolve().declaration.qualifiedName?.asString() ?: ""
            val genericArguments: List<KSTypeArgument> = property.type.element?.typeArguments ?: emptyList()
            val generic = visitTypeArguments(genericArguments, logger::error)
            file += "\tfun $name($name: $type$generic): $fileName {\n"
            file += "\t\t$objectName.$name = $name\n"
            file += "\t\treturn this\n"
            file += "\t}\n\n"
        }
    }

    private fun visitPropertyDeclarationToCreatePrivateVariable(property: KSPropertyDeclaration) {
        val name: String = property.simpleName.asString()
        file += (if (property.annotations.hasAnnotation(BuilderProperty::class.java.simpleName).not())
            "\t\t$name = $name,\n"
        else "\t\t$name = null,\n")
    }

    private fun visitPropertyDeclarationToCreateConstructor(property: KSPropertyDeclaration) {
        val typeResolve = property.type.resolve()
        if (property.annotations.hasAnnotation(BuilderProperty::class.java.simpleName).not()) {
            val name: String = property.simpleName.asString()
            val type: String = typeResolve.declaration.qualifiedName?.asString() ?: ""
            val nullable = if (typeResolve.nullability == Nullability.NULLABLE) "?" else ""
            val genericArguments: List<KSTypeArgument> = property.type.element?.typeArguments ?: emptyList()
            val generic = visitTypeArguments(genericArguments, logger::error)
            file += "\t$name: $type$generic$nullable,\n"
        } else {
            if (typeResolve.nullability != Nullability.NULLABLE)
                logger.error("BuilderProperties have to be nullable", property)
        }
    }
}
