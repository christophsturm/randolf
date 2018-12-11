import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

class Randolf private constructor(private val minimal: Boolean) {
    companion object {
        inline fun <reified T : Any> create(minimal: Boolean = false): T = create(T::class, minimal)
        fun <T : Any> create(kClass: KClass<T>, minimal: Boolean = false): T = Randolf(minimal).create(kClass, "<root>")

        private val STRING = String::class.createType()
        private val INT = Int::class.createType()
        private val LONG = Long::class.createType()
        private val DOUBLE = Double::class.createType()
    }

    private val path = mutableSetOf<KClass<*>>()


    fun <T : Any> create(kClass: KClass<T>, propertyName: String): T {
        if (path.contains(kClass)) throw RandolfException("recursion detected when trying to set property $propertyName with type ${kClass.simpleName}")
        path.add(kClass)
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map { parameter ->
            val type = parameter.type

            if (minimal && type.isMarkedNullable) null else when (type) {
                STRING -> if (minimal) "" else ('A'..'z').map { it }.shuffled().subList(0, 20).joinToString("")
                INT -> ThreadLocalRandom.current().nextInt()
                LONG -> ThreadLocalRandom.current().nextLong()
                DOUBLE -> ThreadLocalRandom.current().nextDouble()
                else -> create(Class.forName(type.javaType.typeName).kotlin, parameter.name!!)
            }
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

}

class RandolfException(message: String) : RuntimeException(message)
