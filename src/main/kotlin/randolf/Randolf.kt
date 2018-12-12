package randolf

import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

class Randolf private constructor(private val minimal: Boolean) {
    companion object {
        inline fun <reified T : Any> create(minimal: Boolean = false): T = create(T::class, minimal)
        fun <T : Any> create(kClass: KClass<T>, minimal: Boolean = false): T = Randolf(minimal).create(kClass, "<root>")

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
            createValue(parameter.type, parameter.name!!)
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

    private fun createValue(type: KType, parameterName: String): Any? {
        val parameterKClass = type.classifier as KClass<*>
        val isEnum = type.javaType.let { it is Class<*> && it.isEnum }
        return if (minimal && type.isMarkedNullable) null
        else if (isEnum) {
            parameterKClass.java.enumConstants.random()
        } else when (parameterKClass) {
            List::class -> {
                if (minimal)
                    emptyList<Any>()
                else
                    listOf(createValue(type.arguments.single().type!!, parameterName))
            }
            String::class -> if (minimal) "" else (1..20).map { STRING_CHARACTERS.random() }.joinToString("")
            Int::class -> Random.nextInt()
            Long::class -> Random.nextLong()
            Double::class -> Random.nextDouble()
            else -> create(parameterKClass, parameterName)
        }
    }

}

class RandolfException(message: String) : RuntimeException(message)
