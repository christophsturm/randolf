package randolf

import java.util.*
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

data class RandolfConfig(val stringLength: Int = 20, val minimal: Boolean = false)

class Randolf(val config: RandolfConfig = RandolfConfig()) {
    companion object {
        // just ASCII for now, this could easily be made configurable
        private val STRING_CHARACTERS = ('A'..'Z').toList() + (('a'..'z').toList()).plus(' ').toTypedArray()
    }

    private val path = mutableSetOf<KClass<*>>()


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
        } else when (parameterKClass) {
            Map::class -> makeMap(type, parameterName)
            Collection::class -> makeList(type, parameterName)
            List::class -> makeList(type, parameterName)
            Set::class -> makeList(type, parameterName).toSet()
            String::class -> if (config.minimal) "" else (1..config.stringLength).map { STRING_CHARACTERS.random() }.joinToString(
                    ""
            )
            Int::class -> Random.nextInt()
            Long::class -> Random.nextLong()
            Double::class -> Random.nextDouble()
            else -> create(parameterKClass, parameterName)
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
