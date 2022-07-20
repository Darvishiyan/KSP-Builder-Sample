package com.darvishiyan.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) = write(str.toByteArray())

fun Sequence<KSAnnotation>.getAnnotation(target: String): KSAnnotation {
    return getAnnotationIfExist(target) ?: throw NoSuchElementException("Sequence contains no element matching the predicate.")
}

fun Sequence<KSAnnotation>.getAnnotationIfExist(target: String): KSAnnotation? {
    for (element in this) if (element.shortName.asString() == target) return element
    return null
}

fun Sequence<KSAnnotation>.hasAnnotation(target: String): Boolean {
    for (element in this) if (element.shortName.asString() == target) return true
    return false
}

@Suppress("UNCHECKED_CAST")
fun <T> List<KSValueArgument>.getParameterValue(target: String): T {
    return getParameterValueIfExist(target) ?: throw NoSuchElementException("Sequence contains no element matching the predicate.")
}

@Suppress("UNCHECKED_CAST")
fun <T> List<KSValueArgument>.getParameterValueIfExist(target: String): T? {
    for (element in this) if (element.name?.asString() == target) (element.value as? T)?.let { return it }
    return null
}

fun Collection<Modifier>.containsIgnoreCase(name: String): Boolean {
    return stream().anyMatch { it.name.equals(name, true) }
}


fun visitTypeArguments(typeArguments: List<KSTypeArgument>, error: (String, KSNode) -> Unit): String {
    var result = ""
    if (typeArguments.isNotEmpty()) {
        result += "<"
        typeArguments.forEach { arg ->
            result += "${visitTypeArgument(arg, error)}, "
        }
        result += ">"
    }
    return result
}

private fun visitTypeArgument(typeArgument: KSTypeArgument, error: (String, KSNode) -> Unit): String {
    var result = ""
    when (val variance: Variance = typeArgument.variance) {
        Variance.STAR -> result += "*" // <*>
        Variance.COVARIANT, Variance.CONTRAVARIANT -> result += "${variance.label} " // <out ...>, <in ...>
        Variance.INVARIANT -> {} /*Do nothing.*/
    }
    if (result.endsWith("*").not()) {
        val resolvedType = typeArgument.type?.resolve()
        result += resolvedType?.declaration?.qualifiedName?.asString() ?: run {
            error("Invalid type argument", typeArgument)
        }

        // Generating nested generic parameters if any.
        val genericArguments = typeArgument.type?.element?.typeArguments ?: emptyList()
        result += visitTypeArguments(genericArguments, error)

        // Handling nullability.
        result += if (resolvedType?.nullability == Nullability.NULLABLE) "?" else ""
    }
    return result
}