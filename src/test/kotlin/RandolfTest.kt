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

class RandolfTest : JUnit5Minutests {
    data class StringDC(val stringProperty: String)


    override val tests = rootContext<Unit>(transform = skipAndFocus) {
        test("sets string properties to 20 random characters") {
            val firstInstance = Randolf.create<StringDC>()
            expectThat(firstInstance).get { stringProperty }.hasLength(20)

            expectThat(firstInstance).isNotEqualTo(Randolf.create())
        }

        test("sets Integer properties to a random value") {
            data class IntegerDC(val integerProperty: Int)
            expectThat(Randolf.create<IntegerDC>()).isNotEqualTo(Randolf.create())
        }
        test("sets Long properties to a random value") {
            data class LongDC(val longProperty: Long)
            expectThat(Randolf.create<LongDC>()).isNotEqualTo(Randolf.create())
        }
        test("sets Double properties to a random value") {
            data class DoubleDC(val doubleProperty: Double)
            expectThat(Randolf.create<DoubleDC>()).isNotEqualTo(Randolf.create())
        }

        test("initializes nested data classes") {
            data class DataClassDC(val otherDataClassProperty: StringDC)
            expectThat(Randolf.create<DataClassDC>()).isNotEqualTo(Randolf.create())
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


