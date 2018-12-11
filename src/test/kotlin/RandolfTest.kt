import com.oneeyedmen.minutest.experimental.skipAndFocus
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import strikt.api.expectThat
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


    override val tests = context<Unit>(transform = skipAndFocus) {
        test("also sets nullable fields") {
            data class NullableStringDC(val a: String?)
            expectThat(Randolf.create<NullableStringDC>(false)).get { a }.isNotNull()
        }
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

        test("can create a data class with a nested data class") {
            data class IntFieldsAndNestedDataClassDC(val a: Int, val b: Int, val c: StringDC)
            Randolf.create<IntFieldsAndNestedDataClassDC>()
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

object Randolf {
    inline fun <reified T> create(minimal: Boolean = false): T = create(T::class, minimal) as T

    fun create(kClass: KClass<*>, minimal: Boolean = false): Any {
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map { parameter ->
            val type = parameter.type

            if (minimal && type.isMarkedNullable) null else when (type) {
                String::class.createType() -> if (minimal) "" else ('A'..'z').map { it }.shuffled().subList(
                    0,
                    20
                ).joinToString("")
                Int::class.createType() -> ThreadLocalRandom.current().nextInt()
                Long::class.createType() -> ThreadLocalRandom.current().nextLong()
                Double::class.createType() -> ThreadLocalRandom.current().nextDouble()
                else -> create(Class.forName(type.javaType.typeName).kotlin)
            }
        }
        return constructor.call(*parameterValues.toTypedArray())
    }

}

