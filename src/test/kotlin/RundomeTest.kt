import RundomeTest.IntFieldsAndNestedDataClassDC
import RundomeTest.StringDC
import com.oneeyedmen.minutest.experimental.SKIP
import com.oneeyedmen.minutest.experimental.skipAndFocus
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import kotlin.reflect.KClass

class RundomeTest : JUnit5Minutests {
    data class StringDC(val a: String)
    data class IntFieldsAndNestedDataClassDC(val a: Int, val b: Int, val c: StringDC)
    data class AnotherStringDC(val a: String)


    override val tests = context<Unit>(transform = skipAndFocus) {
        test("can create a data class with a String field") {
            Rundome.create<StringDC>()
        }

        test("can create a data class with a nested data class") {
            Rundome.create<IntFieldsAndNestedDataClassDC>()
        }
        SKIP - test("can not create anything else because everything is hardcoded") {
            Rundome.create<AnotherStringDC>()
        }
    }
}

object Rundome {
    inline fun <reified T> create(): T = create(T::class) as T

    fun create(kClass: KClass<*>): Any {
        return when (kClass) {
            IntFieldsAndNestedDataClassDC::class -> IntFieldsAndNestedDataClassDC(1, 2, StringDC("blah"))
            StringDC::class -> StringDC("blah")
            else -> throw RuntimeException()
        }
    }

}

