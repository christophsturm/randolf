package randolf

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class Randolf private constructor(private val minimal: Boolean) {
    companion object {
        inline fun <reified T : Any> create(minimal: Boolean = false): T = create(T::class, minimal)
        fun <T : Any> create(kClass: KClass<T>, minimal: Boolean = false): T = Randolf(minimal).create(kClass, "<root>")

        private val STRING = String::class.createType()
        private val INT = Int::class.createType()
        private val LONG = Long::class.createType()
        private val DOUBLE = Double::class.createType()
        // just ASCII for now, this could easily be made configurable
        private val STRING_CHARACTERS = ('0'..'z').toList().toTypedArray()
        private var mappings: Map<KType, (minimal: Boolean) -> Any?> = mapOf(
                STRING to { minimal ->
                    if (minimal) "" else (1..20).map { STRING_CHARACTERS.random() }.joinToString("")
                },
                INT to { _ -> kotlin.random.Random.nextInt() },
                LONG to { _ -> kotlin.random.Random.nextLong() },
                DOUBLE to { _ -> kotlin.random.Random.nextDouble() })

        fun addMapping(type: KType, mf: (minimal: Boolean) -> Any?) {
            mappings = mappings.plus(type to mf)
        }
    }

    private val path = mutableSetOf<KClass<*>>()


    fun <T : Any> create(kClass: KClass<T>, propertyName: String): T {
        if (path.contains(kClass)) throw RandolfException("recursion detected when trying to set property $propertyName with type ${kClass.simpleName}")
        path.add(kClass)
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map { parameter ->
            val type = parameter.type
            if (minimal && type.isMarkedNullable) null else {
                mappings[type]?.invoke(minimal) ?: create(type.classifier as KClass<*>, parameter.name!!)
            }
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

}

class RandolfException(message: String) : RuntimeException(message)
