import com.oneeyedmen.minutest.experimental.skipAndFocus
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

class RundomeTest : JUnit5Minutests {
    data class StringDC(val a: String)


    override val tests = context<Unit>(transform = skipAndFocus) {
        test("also sets nullable fields") {
            data class NullableStringDC(val a: String?)
            expectThat(Rundome.create<NullableStringDC>(false)).get { a }.isNotNull()
        }
        test("can create a minimal object where everything that can be null is null") {
            data class NullableStringDC(val a: String?)
            expectThat(Rundome.create<NullableStringDC>(true)).get { a }.isNull()
        }
        test("can create a data class with a String field") {
            Rundome.create<StringDC>()
        }

        test("can create a data class with a nested data class") {
            data class IntFieldsAndNestedDataClassDC(val a: Int, val b: Int, val c: StringDC)
            Rundome.create<IntFieldsAndNestedDataClassDC>()
        }
    }
}

object Rundome {
    inline fun <reified T> create(minimal: Boolean = false): T = create(T::class, minimal) as T

    fun create(kClass: KClass<*>, minimal: Boolean = false): Any {
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map {
            val type = it.type

            if (minimal && type.isMarkedNullable) null else when (type) {
                String::class.createType() -> "blah"
                Int::class.createType() -> 1
                else -> create(Class.forName(type.javaType.typeName).kotlin)
            }
        }
        return constructor.call(*parameterValues.toTypedArray())
    }

}

