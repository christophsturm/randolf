import com.oneeyedmen.minutest.experimental.skipAndFocus
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.hasLength
import strikt.assertions.isEmpty
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

class RandolfTest : JUnit5Minutests {
    data class StringDC(val stringProperty: String)


    override val tests = rootContext<Unit>(transform = skipAndFocus) {
        test("sets string properties to 20 random characters") {
            val firstInstance = Randolf.create<StringDC>()
            expectThat(firstInstance).get { stringProperty }.hasLength(20)

            expectThat(Randolf.create<StringDC>()).isNotEqualTo(firstInstance)
        }

        test("sets Integer properties to a random value") {
            data class IntegerDC(val integerProperty: Int)
            expectThat(Randolf.create<IntegerDC>()).isNotEqualTo(Randolf.create<IntegerDC>())
        }
        test("sets Long properties to a random value") {
            data class LongDC(val longProperty: Long)
            expectThat(Randolf.create<LongDC>()).isNotEqualTo(Randolf.create<LongDC>())
        }
        test("sets Double properties to a random value") {
            data class DoubleDC(val doubleProperty: Double)
            expectThat(Randolf.create<DoubleDC>()).isNotEqualTo(Randolf.create<DoubleDC>())
        }

        test("initializes nested data classes") {
            data class DataClassDC(val otherDataClassProperty: StringDC)
            expectThat(Randolf.create<DataClassDC>()).isNotEqualTo(Randolf.create<DataClassDC>())
        }
        test("also sets nullable fields") {
            data class NullableStringDC(val a: String?)
            expectThat(Randolf.create<NullableStringDC>(false)).get { a }.isNotNull()
        }
        test("detects dependency loops") {
            data class DataClassThatContainsItelf(val recursiveField: DataClassThatContainsItelf)
            expectThrows<RandolfException> {
                Randolf.create<DataClassThatContainsItelf>()
            }.get { message }.isNotNull().and {
                contains("recursion")
                contains("detected")
                contains("recursiveField")
                contains(DataClassThatContainsItelf::class.java.name)
            }

        }
        context("minimal mode") {

            test("sets nullable properties to null") {
                data class NullableStringDC(val a: String?)
                expectThat(Randolf.create<NullableStringDC>(true)).get { a }.isNull()
            }

            test("sets string properties to empty string") {
                data class StringDC(val a: String)
                expectThat(Randolf.create<StringDC>(true)).get { a }.isEmpty()
            }

        }
    }
}

class RandolfException(message: String) : RuntimeException(message)

class Randolf private constructor(private val minimal: Boolean) {
    companion object {
        inline fun <reified T : Any> create(minimal: Boolean = false): T = Randolf.create(T::class, minimal)
        fun <T : Any> create(kClass: KClass<T>, minimal: Boolean = false): T = Randolf(minimal).create(kClass, "<root>")
    }

    val path = mutableListOf<KClass<*>>()

    fun <T : Any> create(kClass: KClass<T>, propertyName: String): T {
        if (path.contains(kClass)) throw RandolfException("recursion detected when trying to set property $propertyName of type $kClass")
        path.add(kClass)
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map { parameter ->
            val type = parameter.type

            if (minimal && type.isMarkedNullable) null else when (type) {
                String::class.createType() -> if (minimal) "" else ('A'..'z').map { it }.shuffled().subList(
                    0, 20
                ).joinToString("")
                Int::class.createType() -> ThreadLocalRandom.current().nextInt()
                Long::class.createType() -> ThreadLocalRandom.current().nextLong()
                Double::class.createType() -> ThreadLocalRandom.current().nextDouble()
                else -> create(Class.forName(type.javaType.typeName).kotlin, parameter.name!!)
            }
        }
        path.remove(kClass)
        return constructor.call(*parameterValues.toTypedArray())
    }

}

