package randolf

import kotlin.reflect.KClass
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
            val type = parameter.type

            val parameterKClass = type.classifier as KClass<*>
            val isEnum = type.javaType.let { it is Class<*> && it.isEnum }
            if (isEnum) {
                parameterKClass.java.enumConstants.random()
            } else if (minimal && type.isMarkedNullable) null else when (parameterKClass) {
                String::class -> if (minimal) "" else (1..20).map { STRING_CHARACTERS.random() }.joinToString("")
                Int::class -> kotlin.random.Random.nextInt()
                Long::class -> kotlin.random.Random.nextLong()
                Double::class -> kotlin.random.Random.nextDouble()
                else -> create(parameterKClass, parameter.name!!)
            }
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

}

class RandolfException(message: String) : RuntimeException(message)
