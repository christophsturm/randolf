package randolf

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType


/**
 * reified shortcut for the Randolf.create method
 * @see Randolf.create
 */
inline fun <reified T : Any> Randolf.create(): T = this.create(T::class)

class Randolf(private val config: RandolfConfig = RandolfConfig()) {

    /**
     * Instantiates a kotlin class by calling its constructor with random values
     */
    fun <T : Any> create(kClass: KClass<T>) = create(kClass, "root")

    private fun <T : Any> create(kClass: KClass<T>, propertyName: String): T {
        if (unfinishedClasses.contains(kClass))
            throw RandolfException("Recursion detected when trying to set property $propertyName of type ${kClass.simpleName}")
        unfinishedClasses.add(kClass)

        val constructors = kClass.constructors
        val constructor: KFunction<T> = constructors.singleOrNull { it.visibility == KVisibility.PUBLIC }
            ?: kClass.primaryConstructor.let { if (it?.visibility == KVisibility.PUBLIC) it else null } ?: throw RandolfException("No public constructor found when trying to set property $propertyName of type ${kClass.simpleName}")

        // create a map to use for callBy, because callBy respects default parameters
        val parameterValues = constructor.parameters.mapNotNull { parameter ->
            // in minimal mode set nullable parameters to null and omit optional parameters
            if (config.minimal && parameter.type.isMarkedNullable)
                Pair(parameter, null)
            else if (config.minimal && parameter.isOptional)
                null
            else
                Pair(parameter, createValue(parameter.type, parameter.name!!))
        }.toMap()
        unfinishedClasses.remove(kClass)
        return constructor.callBy(parameterValues)
    }

    private val random = config.random

    // keep a list of classes that we are currently working on to detect recursion
    private val unfinishedClasses = mutableSetOf<KClass<*>>()

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
            valueCreators[parameterKClass]?.invoke(type, parameterName) ?: create(parameterKClass, parameterName)
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


class RandolfException(message: String) : RuntimeException(message)
