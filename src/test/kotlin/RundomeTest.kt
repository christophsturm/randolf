import com.oneeyedmen.minutest.experimental.SKIP
import com.oneeyedmen.minutest.experimental.skipAndFocus
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

class RundomeTest : JUnit5Minutests {
    data class StringDC(val a: String)
    data class IntFieldsAndNestedDataClassDC(val a: Int, val b: Int, val c: StringDC)
    data class AnotherStringDC(val a: String)


    override val tests = context<Unit>(transform = skipAndFocus) {
        SKIP - test("can create a minimal object where everything that can be null is null") {

        }
        test("can create a data class with a String field") {
            Rundome.create<StringDC>()
        }

        test("can create a data class with a nested data class") {
            Rundome.create<IntFieldsAndNestedDataClassDC>()
        }
        test("can not create anything else because everything is hardcoded") {
            Rundome.create<AnotherStringDC>()
        }
    }
}

object Rundome {
    inline fun <reified T> create(): T = create(T::class) as T

    fun create(kClass: KClass<*>): Any {
        val constructor = kClass.constructors.single()
        val parameters = constructor.parameters
        val parameterValues = parameters.map {
            val type = it.type

            when (type) {
                String::class.createType() -> "blah"
                Int::class.createType() -> 1
                else -> create(Class.forName(type.javaType.typeName).kotlin)
            }
        }
        return constructor.call(*parameterValues.toTypedArray())
    }

}

