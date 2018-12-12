package randolf

import com.oneeyedmen.minutest.experimental.SKIP
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


    enum class BeanType { ROBUSTA, ARABICA
    }

    override val tests = rootContext<Unit>(transform = skipAndFocus) {
        test("how it looks and what it does") {
            data class Group(val name: String)
            data class User(
                val firstName: String,
                val name: String,
                val age: Int,
                val lat: Double,
                val long: Double,
                val group: Group
            )
            print(Randolf.create<User>())
            // => User(firstName=xdUpnBjqLMgOmb[U25ym, name=`6Eb2K4uO4;pv:i;V@AI, age=-776962503, lat=0.8399883812751676, long=0.40143311870843756, group=Group(name=Q\M1^ZIphuEZwlC<43SX))
        }

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

        test("sets enum properties to a random value") {
            data class EnumDC(val enumProperty: BeanType)
            // create 10 instances and check that not all of them are the same
            expectThat((1..10).map { Randolf.create<EnumDC>() }.map { it.enumProperty }).contains(
                BeanType.ROBUSTA,
                BeanType.ARABICA
            )
        }
        test("initializes nested data classes") {
            data class DataClassDC(val otherDataClassProperty: StringDC)
            expectThat(Randolf.create<DataClassDC>()).isNotEqualTo(Randolf.create())
        }
        test("also sets nullable fields") {
            data class NullableStringDC(val a: String?)
            expectThat(Randolf.create<NullableStringDC>(false)).get { a }.isNotNull()
        }

        SKIP - test("sets lists of supported values") {
            // this will be fun and require some refactoring.
            data class ListDC(val listOfStrings: List<String>)
            expectThat(Randolf.create<ListDC>()).isNotEqualTo(Randolf.create())
        }

        test("detects dependency loops") {
            data class DataClassThatReferencesItself(val recursiveField: DataClassThatReferencesItself)
            expectThrows<RandolfException> {
                Randolf.create<DataClassThatReferencesItself>()
            }.get { message }.isNotNull().and {
                contains("recursion ")
                contains("detected ")
                contains("recursiveField ")
                contains(" DataClassThatReferencesItself")
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


