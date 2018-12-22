package randolf

import java.util.*
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType


class Randolf(val config: RandolfConfig = RandolfConfig()) {

    /**
     * Instantiates a kotlin class by calling its single constructor with autocreated values.
     */
    fun <T : Any> create(kClass: KClass<T>, propertyName: String = "root"): T {
        if (path.contains(kClass)) throw RandolfException("recursion detected when trying to set property $propertyName with type ${kClass.simpleName}")
        path.add(kClass)
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.mapNotNull { parameter ->
            if (config.minimal && parameter.type.isMarkedNullable)
                Pair(parameter, null)
            else if (config.minimal && parameter.isOptional)
                null
            else
                Pair(parameter, createValue(parameter.type, parameter.name!!))
        }.toMap()
        path.remove(kClass)
        return constructor.callBy(parameterValues)
    }

    private val random = config.random
    private val path = mutableSetOf<KClass<*>>()

    private val valueCreators = mapOf<KClass<*>, (type: KType, name: String) -> Any>(
        Int::class to { _, _ -> random.nextInt() },
        Double::class to { _, _ -> random.nextDouble() },
        Float::class to { _, _ -> random.nextFloat() },
        Long::class to { _, _ -> random.nextLong() },
        Short::class to { _, _ -> random.nextInt().toShort() },
        Byte::class to { _, _ -> random.nextBytes(1).single() },
        Boolean::class to { _, _ -> random.nextBoolean() },
        Char::class to { _, _ -> config.stringCharacters.random(random) },
        String::class to { _, _ ->
            if (config.minimal) "" else (1..config.stringLength).map { config.stringCharacters.random(random) }.joinToString(
                ""
            )
        },
        Map::class to { type, name -> makeMap(type, name) },
        List::class to { type, name -> makeList(type, name) },
        Set::class to { type, name -> makeList(type, name).toSet() },
        Collection::class to { type, name -> makeList(type, name) }
    ).plus(config.additionalValueCreators)


    private fun createValue(type: KType, parameterName: String): Any {
        val parameterKClass = type.classifier as KClass<*>
        val isEnum = (type.javaType as? Class<*>)?.isEnum ?: false
        return if (isEnum) {
            parameterKClass.java.enumConstants.random(random)
        } else {
            val valueCreator = valueCreators[parameterKClass]
            valueCreator?.invoke(type, parameterName) ?: create(parameterKClass, parameterName)
        }
    }

    private fun makeMap(type: KType, parameterName: String): Map<Any, Any> {
        return if (config.minimal)
            emptyMap()
        else {
            val arguments = type.arguments
            val keyType = arguments[0].type!!
            val valueType = arguments[1].type!!
            (0..random.nextInt(config.maxCollectionSize - 1) + 1).map {
                Pair(createValue(keyType, parameterName), createValue(valueType, parameterName))
            }.toMap()
        }
    }

    private fun makeList(type: KType, parameterName: String): List<Any> {
        return if (config.minimal)
            emptyList()
        else
            (0..random.nextInt(config.maxCollectionSize - 1) + 1).mapTo(LinkedList()) {
                createValue(type.arguments.single().type!!, parameterName)
            }
    }
}

/**
 * infix shortcut for the Randolf.create method
 * @see Randolf.create
 */
inline fun <reified T : Any> Randolf.create(): T = this.create(T::class)

/**
 * This class can be passed to the Randolf constructor to specify the configuration.
 * The parameter default values are the default config.
 * @property minimal enable minimal mode where all nullable fields are null,
 * all fields with default values get their default value, all numbers are 0 and all strings, and collections are empty
 * @property additionalValueCreators adds value creators that are not supported out of the box
 * @property random inject a random instance. beware that random instances are mutable so don't reuse them
 *
 */
data class RandolfConfig(
    val minimal: Boolean = false,
    val stringLength: Int = 20,
    val stringCharacters: List<Char> = ('A'..'Z').toList() + (('a'..'z').toList()).plus(' '),
    val maxCollectionSize: Int = 10,
    val additionalValueCreators: Map<KClass<*>, (type: KType, name: String) -> Any> = emptyMap(),
    val random: Random = Random
)


class RandolfException(message: String) : RuntimeException(message)
