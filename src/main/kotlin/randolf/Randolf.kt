package randolf

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class Randolf private constructor(private val minimal: Boolean) {
    companion object {
        inline fun <reified T : Any> create(minimal: Boolean = false): T = create(T::class, minimal)
        fun <T : Any> create(kClass: KClass<T>, minimal: Boolean = false): T = Randolf(minimal).create(kClass, "<root>")

        // just ASCII for now, this could easily be made configurable
        private val STRING_CHARACTERS = ('0'..'z').toList().toTypedArray()
        private var mappings: Map<KType, (minimal: Boolean) -> Any?> = emptyMap()

        fun addMapping(type: KClass<*>, mf: (minimal: Boolean) -> Any?) {
            mappings = mappings.plus(type.createType() to mf).plus(type.createType(nullable = true) to mf)
        }

        init {
            addMapping(String::class) { minimal ->
                if (minimal) "" else (1..20).map { STRING_CHARACTERS.random() }.joinToString("")
            }
            addMapping(Int::class) { kotlin.random.Random.nextInt() }
            addMapping(Long::class) { kotlin.random.Random.nextLong() }
            addMapping(Double::class) { kotlin.random.Random.nextDouble() }
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
                val mappingFunction = mappings[type]
                if (mappingFunction == null) {
                    create(type.classifier as KClass<*>, parameter.name!!)
                } else {
                    mappingFunction.invoke(minimal)
                }
            }
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

}

class RandolfException(message: String) : RuntimeException(message)
