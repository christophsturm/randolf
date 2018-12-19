package randolf

import com.oneeyedmen.minutest.experimental.SKIP
import com.oneeyedmen.minutest.experimental.skipAndFocus
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.hasLength
import strikt.assertions.isEmpty
import strikt.assertions.isLessThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.size

class RandolfTest : JUnit5Minutests {
    data class StringDC(val stringProperty: String)


    enum class BeanType { ROBUSTA, ARABICA }

    override val tests = rootContext<Randolf>(transform = skipAndFocus) {
        fixture { Randolf() }


        // not using the fixture in the test below to make it self contained for the readme
        test("how it looks and what it does") {
            data class Group(val name: String)
            data class User(
                val firstName: String,
                val name: String,
                val age: Int,
                val lat: Double,
                val lon: Double,
                val favoriteCoffee: BeanType,
                val groups: List<Group>
            )
            print(Randolf().create<User>())
            // => User(firstName=UVQyniDCPDuxYleOqVlx, name=xKQtkvybVJGeXHWUPBSE, age=12517728, lat=0.39825335948498386, long=0.1518043402275937, favoriteCoffee=ARABICA, groups=[Group(name=TCZRDtzVuKrDqrmjgdqS), Group(name= txATFFGAPTXTNBeuCcN), Group(name=EiMytHwslatclHMaCDig), Group(name=I DFdlkHxiXxwXMdjHYo), Group(name=wbhsAlk wPmZkLCkBCJe), Group(name=LwBn WbOrUHKojWjbYBU), Group(name=qZryMZSZMhjgGtYUWQHP), Group(name=QcfoKlV p akKYqrEXRG)])
        }

        test("sets string properties to 20 random characters") {
            val firstInstance = fixture.create<StringDC>()
            expectThat(firstInstance).get { stringProperty }.hasLength(20)
            expectThat(firstInstance).isNotEqualTo(fixture.create())
        }

        test("sets Integer properties to a random value") {
            data class IntegerDC(val integerProperty: Int)
            expectThat(fixture.create<IntegerDC>()).isNotEqualTo(fixture.create())
        }
        test("sets Long properties to a random value") {
            data class LongDC(val longProperty: Long)
            expectThat(fixture.create<LongDC>()).isNotEqualTo(fixture.create())
        }
        test("sets Double properties to a random value") {
            data class DoubleDC(val doubleProperty: Double)
            expectThat(fixture.create<DoubleDC>()).isNotEqualTo(fixture.create())
        }

        test("sets enum properties to a random value") {
            data class EnumDC(val enumProperty: BeanType)
            // create 10 instances and check that not all of them are the same
            expectThat((1..10).map { fixture.create<EnumDC>() }.map { it.enumProperty }).contains(
                BeanType.ROBUSTA, BeanType.ARABICA
            )
        }
        test("initializes nested data classes") {
            data class DataClassDC(val otherDataClassProperty: StringDC)
            expectThat(fixture.create<DataClassDC>()).isNotEqualTo(fixture.create())
        }
        data class NullableFieldsDC(
            val string: String?, val int: Int?, val long: Long?, val double: Double?, val enum: BeanType?
        )

        test("also sets nullable fields") {
            expectThat(fixture.create<NullableFieldsDC>()) {
                get { string }.isNotNull()
                get { int }.isNotNull()
                get { long }.isNotNull()
                get { double }.isNotNull()
                get { enum }.isNotNull()
            }
        }
        context("collections support") {
            data class ListDC(
                val strings: List<String>, val ints: List<Int>, val longs: List<Long>,
                val doubles: List<Double>, val enums: List<BeanType>
            )
            test("sets lists of supported  values") {
                expectThat(fixture.create<ListDC>()).isNotEqualTo(fixture.create())
            }
            test("lists have 1 to 10 entries") {
                expectThat((0..10).map { fixture.create<ListDC>() }).all {
                    get { strings }.isNotEmpty().size.isLessThan(11)
                    get { ints }.isNotEmpty().size.isLessThan(11)
                    get { longs }.isNotEmpty().size.isLessThan(11)
                    get { doubles }.isNotEmpty().size.isLessThan(11)
                    get { enums }.isNotEmpty().size.isLessThan(11)
                }
            }

            data class SetDC(
                val strings: Set<String>, val ints: Set<Int>, val longs: Set<Long>,
                val doubles: Set<Double>, val enums: Set<BeanType>
            )
            test("sets sets of supported values") {
                expectThat(fixture.create<SetDC>()).isNotEqualTo(fixture.create())
            }
            test("sets have 1 to 10 entries") {
                expectThat((0..10).map { fixture.create<SetDC>() }).all {
                    get { strings }.isNotEmpty().size.isLessThan(11)
                    get { ints }.isNotEmpty().size.isLessThan(11)
                    get { longs }.isNotEmpty().size.isLessThan(11)
                    get { doubles }.isNotEmpty().size.isLessThan(11)
                    get { enums }.isNotEmpty().size.isLessThan(11)
                }
            }

            data class CollectionDC(
                val strings: Collection<String>, val ints: Collection<Int>, val longs: Collection<Long>,
                val doubles: Collection<Double>, val enums: Collection<BeanType>
            )

            test("sets lists of supported values") {
                expectThat(fixture.create<CollectionDC>()).isNotEqualTo(fixture.create())
            }
            test("lists have 1 to 10 entries") {
                expectThat((0..10).map { fixture.create<CollectionDC>() }).all {
                    get { strings }.isNotEmpty().size.isLessThan(11)
                    get { ints }.isNotEmpty().size.isLessThan(11)
                    get { longs }.isNotEmpty().size.isLessThan(11)
                    get { doubles }.isNotEmpty().size.isLessThan(11)
                    get { enums }.isNotEmpty().size.isLessThan(11)
                }
            }

            data class MapDC(
                val int2StringMap: Map<Int, String>
            )
            SKIP - test("sets maps of supported values") {
                expectThat(fixture.create<MapDC>()).isNotEqualTo(fixture.create())
            }
            SKIP - test("maps have 1 to 10 entries") {
                expectThat((0..10).map { fixture.create<MapDC>() }).all {
                    get { int2StringMap.entries }.isNotEmpty().size.isLessThan(11)
                }
            }
        }
        test("detects dependency loops") {
            data class DataClassThatReferencesItself(val recursiveField: DataClassThatReferencesItself)
            expectThrows<RandolfException> {
                fixture.create<DataClassThatReferencesItself>()
            }.get { message }.isNotNull().and {
                contains("recursion ")
                contains("detected ")
                contains("recursiveField ")
                contains(" DataClassThatReferencesItself")
            }

        }
        context("configuration") {
            test("string length is configurable") {
                expectThat(Randolf(config = RandolfConfig(stringLength = 25)).create<StringDC>()).get { stringProperty }
                    .hasLength(25)
            }
        }
        context("minimal mode") {
            fixture { Randolf(minimal = true) }

            test("sets nullable properties to null") {
                expectThat(fixture.create<NullableFieldsDC>()) {
                    get { string }.isNull()
                    get { int }.isNull()
                    get { long }.isNull()
                    get { double }.isNull()
                    get { enum }.isNull()
                }
            }

            test("sets string properties to empty string") {
                data class StringDC(val a: String)
                expectThat(fixture.create<StringDC>()).get { a }.isEmpty()
            }
            test("sets list properties to empty list") {
                data class ListDC(val list: List<String>, val set: Set<String>, val collection: Collection<String>)
                expectThat(fixture.create<ListDC>()) {
                    get { list }.isEmpty()
                    get { set }.isEmpty()
                    get { collection }.isEmpty()
                }
            }
            test("sets map properties to empty maps") {
                data class MapDC(val map: Map<String, String>)
                expectThat(fixture.create<MapDC>()) {
                    get { map }.isEmpty()
                }
            }

        }
    }
}


