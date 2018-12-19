package randolf

import java.util.*
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

data class RandolfConfig(
    val stringLength: Int = 20,
    val minimal: Boolean = false,
    val customMappings: Map<KClass<*>, (type: KType, name: String) -> Any> = emptyMap()
)

class Randolf(val config: RandolfConfig = RandolfConfig()) {
    companion object {
        // just ASCII for now, this could easily be made configurable
        private val STRING_CHARACTERS = ('A'..'Z').toList() + (('a'..'z').toList()).plus(' ').toTypedArray()
    }

    private val path = mutableSetOf<KClass<*>>()

    private val typeMappings = mapOf<KClass<*>, (type: KType, name: String) -> Any>(
        Int::class to { _, _ -> Random.nextInt() },
        Double::class to { _, _ -> Random.nextDouble() },
        Float::class to { _, _ -> Random.nextFloat() },
        Long::class to { _, _ -> Random.nextLong() },
        Short::class to { _, _ -> Random.nextInt().toShort() },
        Byte::class to { _, _ -> Random.nextBytes(1).single() },
        Boolean::class to { _, _ -> Random.nextBoolean() },
        Char::class to { _, _ -> STRING_CHARACTERS.random() },
        String::class to { _, _ ->
            if (config.minimal) "" else (1..config.stringLength).map { STRING_CHARACTERS.random() }.joinToString(
                ""
            )
        },
        Map::class to { type, name -> makeMap(type, name) },
        List::class to { type, name -> makeList(type, name) },
        Set::class to { type, name -> makeList(type, name).toSet() },
        Collection::class to { type, name -> makeList(type, name) }
    ).plus(config.customMappings)

    fun <T : Any> create(kClass: KClass<T>, propertyName: String): T {
        if (path.contains(kClass)) throw RandolfException("recursion detected when trying to set property $propertyName with type ${kClass.simpleName}")
        path.add(kClass)
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map { parameter ->
            if (config.minimal && parameter.type.isMarkedNullable) null
            else
                createValue(parameter.type, parameter.name!!)
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

    private fun createValue(type: KType, parameterName: String): Any {
        val parameterKClass = type.classifier as KClass<*>
        val isEnum = (type.javaType as? Class<*>)?.isEnum ?: false
        return if (isEnum) {
            parameterKClass.java.enumConstants.random()
        } else {
            val mappingFunction = typeMappings[parameterKClass]
            mappingFunction?.invoke(type, parameterName) ?: create(parameterKClass, parameterName)
        }
    }

    private fun makeMap(type: KType, parameterName: String): Map<Any, Any> {
        return if (config.minimal)
            emptyMap()
        else {
            val arguments = type.arguments
            val keyType = arguments[0].type!!
            val valueType = arguments[1].type!!
            (0..Random.nextInt(9) + 1).map {
                Pair(createValue(keyType, parameterName), createValue(valueType, parameterName))
            }.toMap()
        }
    }

    private fun makeList(type: KType, parameterName: String): List<Any> {
        return if (config.minimal)
            emptyList()
        else
            (0..Random.nextInt(9) + 1).mapTo(LinkedList()) {
                createValue(type.arguments.single().type!!, parameterName)
            }
    }
}

inline fun <reified T : Any> Randolf.create(): T = this.create(T::class, "root")


class RandolfException(message: String) : RuntimeException(message)
