import RundomeTest.A
import RundomeTest.B
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import kotlin.reflect.KClass

class RundomeTest : JUnit5Minutests {
    data class A(val a: Int, val b: Int, val c: B)
    data class B(val a: String)


    override val tests = context<B> {
        fixture {
            Rundome.create()
        }

        test("fixture works") {
            // no idea what i could assert on the object
        }

        derivedContext<A>("different type") {
            deriveFixture { Rundome.create() }
        }
    }
}

object Rundome {
    inline fun <reified T> create(): T = create(T::class) as T

    fun create(kClass: KClass<*>): Any {
        return when (kClass) {
            A::class -> A(1, 2, B("blah"))
            B::class -> B("blah")
            else -> throw RuntimeException()
        }
    }

}

