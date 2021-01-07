@file:Suppress("NAME_SHADOWING")

package randolf

import failfast.Suite
import failfast.describe
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*
import java.time.Instant
import kotlin.random.Random
import kotlin.reflect.KType

fun main() {
    Suite(RandolfTest.context).run().check()
}

object RandolfTest {
    data class StringDC(val stringProperty: String)

    @Suppress("unused")
    enum class ManyValues {
        BLAH1,
        BLAH2,
        BLAH3,
        BLAH4,
        BLAH5,
        BLAH6,
        BLAH7,
    }

    enum class BeanType { ROBUSTA, ARABICA }


    val context = describe(Randolf::class) {
        val randolf = Randolf()

        // not using the fixture in the test below to make it self contained for the readme
        test("how it looks and what it does") {
            data class Group(val name: String)
            data class User(
                val firstName: String,
                val name: String,
                val age: Int,
                val lat: Double,
                val lon: Double,
                val isBoss: Boolean,
                val teamSize: Short,
                val flags: Byte,
                val shortName: Char,
                val efficiency: Float,
                val favoriteCoffee: BeanType,
                val groups: List<Group>
            )
//            print(Randolf().create<User>())
            // => User(firstName=QV muCLbUVfVheboeSuN, name=MSdOzpRCFIykprACOHjv, age=2116525025, lat=0.9518687079417872, lon=0.8331958938906572, isBoss=false, teamSize=4310, flags=83, shortName=Z, efficiency=0.76879007, favoriteCoffee=ARABICA, groups=[Group(name=TpiFXUrucFA cMARKLiR), Group(name=floFeNep XBEZUfXwQgL)])
        }

        test("sets string properties to 20 random characters") {
            val firstInstance = randolf.create<StringDC>()
            expectThat(firstInstance).get { stringProperty }.hasLength(20)
            expectThat(firstInstance).isNotEqualTo(randolf.create())
        }
        test("list of characters to choose from can be configured") {
            val randolf = Randolf(RandolfConfig(stringLength = 5, stringCharacters = listOf('a')))
            val firstInstance = randolf.create<StringDC>()
            expectThat(firstInstance).get { stringProperty }.isEqualTo("aaaaa")
        }
        test("sets Integer properties to a random value") {
            data class IntegerDC(val integerProperty: Int)
            expectThat(randolf.create<IntegerDC>()).isNotEqualTo(randolf.create())
        }
        test("sets Long properties to a random value") {
            data class LongDC(val longProperty: Long)
            expectThat(randolf.create<LongDC>()).isNotEqualTo(randolf.create())
        }
        test("sets Double properties to a random value") {
            data class DoubleDC(val doubleProperty: Double)
            expectThat(randolf.create<DoubleDC>()).isNotEqualTo(randolf.create())
        }

        test("sets enum properties to a random value") {
            data class EnumDC(val enumProperty: BeanType)
            // create 10 instances and check that not all of them are the same
            expectThat((1..10).map { randolf.create<EnumDC>() }.map { it.enumProperty }).contains(
                BeanType.ROBUSTA, BeanType.ARABICA
            )
        }
        test("initializes nested data classes") {
            data class DataClassDC(val otherDataClassProperty: StringDC)
            expectThat(randolf.create<DataClassDC>()).isNotEqualTo(randolf.create())
        }
        data class NullableFieldsDC(
            val string: String?, val int: Int?, val long: Long?, val double: Double?, val enum: ManyValues?,
            val byte: Byte?, val float: Float?, val short: Short?, val boolean: Boolean?, val char: Char?
        )

        test("also sets nullable fields") {
            expectThat(randolf.create<NullableFieldsDC>()) {
                get { string }.isNotNull()
                get { int }.isNotNull()
                get { long }.isNotNull()
                get { double }.isNotNull()
                get { enum }.isNotNull()
                get { byte }.isNotNull()
                get { float }.isNotNull()
                get { short }.isNotNull()
                get { boolean }.isNotNull()
                get { char }.isNotNull()
            }
        }
        test("does not use default values per default") {
            data class StringWithDefaultDC(val stringProperty: String = "unused default")
            expectThat(randolf.create<StringWithDefaultDC>()).get { stringProperty }.isNotEqualTo("unused default")
        }
        context("collections support") {
            data class ListDC(
                val strings: List<String>, val ints: List<Int>, val longs: List<Long>,
                val doubles: List<Double>, val enums: List<BeanType>
            )
            test("sets lists of supported  values") {
                expectThat(randolf.create<ListDC>()).isNotEqualTo(randolf.create())
            }
            test("lists have 1 to 10 entries per default") {
                expectThat((0..10).map { randolf.create<ListDC>() }).all {
                    get { strings }.isNotEmpty().size.isLessThan(11)
                    get { ints }.isNotEmpty().size.isLessThan(11)
                    get { longs }.isNotEmpty().size.isLessThan(11)
                    get { doubles }.isNotEmpty().size.isLessThan(11)
                    get { enums }.isNotEmpty().size.isLessThan(11)
                }
            }
            test("collection size can be configured") {
                val randolf = Randolf(RandolfConfig(maxCollectionSize = 100))
                expectThat((0..10).map { randolf.create<ListDC>() }).all {
                    get { strings }.isNotEmpty().size.isLessThan(101)
                    get { ints }.isNotEmpty().size.isLessThan(101)
                    get { longs }.isNotEmpty().size.isLessThan(101)
                    get { doubles }.isNotEmpty().size.isLessThan(101)
                    get { enums }.isNotEmpty().size.isLessThan(101)
                }
            }

            data class SetDC(
                val strings: Set<String>, val ints: Set<Int>, val longs: Set<Long>,
                val doubles: Set<Double>, val enums: Set<ManyValues>
            )
            test("sets sets of supported values") {
                expectThat(randolf.create<SetDC>()).isNotEqualTo(randolf.create())
            }
            test("sets have 1 to 10 entries") {
                expectThat((0..10).map { randolf.create<SetDC>() }).all {
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
                expectThat(randolf.create<CollectionDC>()).isNotEqualTo(randolf.create())
            }
            test("lists have 1 to 10 entries") {
                expectThat((0..10).map { randolf.create<CollectionDC>() }).all {
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
            test("sets maps of supported values") {
                expectThat(randolf.create<MapDC>()).isNotEqualTo(randolf.create())
            }
            test("maps have 1 to 10 entries") {
                expectThat((0..10).map { randolf.create<MapDC>() }).all {
                    get { int2StringMap.entries }.isNotEmpty().size.isLessThan(11)
                }
            }
        }
        context("custom types") {
            data class DataClassWithInstant(val instant: Instant)
            test("supports additional value creators for custom types") {
                val now = Instant.now()
                val config = RandolfConfig(additionalValueCreators = mapOf(Instant::class to { _, _ ->
                    now
                }))
                expectThat(Randolf(config).create<DataClassWithInstant>()).get { instant }.isEqualTo(now)
            }
            test("can override creators for internal types") {
                val config =
                    RandolfConfig(additionalValueCreators = mapOf(String::class to { type: KType, name: String ->
                        "field $name of type $type"
                    }))
                expectThat(Randolf(config).create<StringDC>()).get { stringProperty }
                    .isEqualTo("field stringProperty of type kotlin.String")

            }
        }
        context("configuration") {
            test("string length is configurable") {
                expectThat(Randolf(config = RandolfConfig(stringLength = 25)).create<StringDC>()).get { stringProperty }
                    .hasLength(25)
            }
            test("can configure random generator") {
                val initial = Randolf(RandolfConfig(random = Random(1234))).create<NullableFieldsDC>()
                repeat(2) {
                    expectThat(initial).isEqualTo(Randolf(RandolfConfig(random = Random(1234))).create())
                }
                repeat(2) {
                    val new = Randolf(RandolfConfig(random = Random(1236))).create<NullableFieldsDC>()
                    expectThat(initial) {
                        get { string }.isNotEqualTo(new.string)
                        get { int }.isNotEqualTo(new.int)
                        get { long }.isNotEqualTo(new.long)
                        get { double }.isNotEqualTo(new.double)
                        get { enum }.isNotEqualTo(new.enum)
                        get { byte }.isNotEqualTo(new.byte)
                        get { float }.isNotEqualTo(new.float)
                        get { short }.isNotEqualTo(new.short)
                        get { boolean }.isNotEqualTo(new.boolean)
                        get { char }.isNotEqualTo(new.char)
                    }
                }

            }
        }
        context("minimal mode") {
            val minimalRandolf = Randolf(RandolfConfig(minimal = true))

            test("sets nullable properties to null") {
                expectThat(minimalRandolf.create<NullableFieldsDC>()) {
                    get { string }.isNull()
                    get { int }.isNull()
                    get { long }.isNull()
                    get { double }.isNull()
                    get { enum }.isNull()
                    get { byte }.isNull()
                    get { float }.isNull()
                    get { short }.isNull()
                    get { boolean }.isNull()
                    get { char }.isNull()
                }
            }

            test("sets string properties to empty string") {
                data class StringDC(val a: String)
                expectThat(minimalRandolf.create<StringDC>()).get { a }.isEmpty()
            }
            test("sets list properties to empty list") {
                data class ListDC(val list: List<String>, val set: Set<String>, val collection: Collection<String>)
                expectThat(minimalRandolf.create<ListDC>()) {
                    get { list }.isEmpty()
                    get { set }.isEmpty()
                    get { collection }.isEmpty()
                }
            }
            test("sets map properties to empty maps") {
                data class MapDC(val map: Map<String, String>)
                expectThat(minimalRandolf.create<MapDC>()) {
                    get { map }.isEmpty()
                }
            }
            test("sets not nullable properties to their default value") {
                data class StringDC(val string: String = "string theory", val int: Int = 42)
                expectThat(minimalRandolf.create<StringDC>()) {
                    get { string }.isEqualTo("string theory")
                    get { int }.isEqualTo(42)
                }
            }


        }
        context("constructor selection") {
            test("uses the public constructor") {
                class DataClassWithPrivatePrimaryConstructor private constructor(val string: String) {
                    @Suppress("unused")
                    constructor(int: Int) : this(int.toString())
                }
                Randolf().create<DataClassWithPrivatePrimaryConstructor>()
            }
        }
        context("error handling") {
            test("detects dependency loops") {
                @Suppress("SelfReferenceConstructorParameter")
                data class DataClassThatReferencesItself(val recursiveField: DataClassThatReferencesItself)
                expectThrows<RandolfException> {
                    randolf.create<DataClassThatReferencesItself>()
                }.get { message }.isNotNull().and {
                    contains("Recursion ")
                    contains("detected ")
                    contains("recursiveField ")
                    contains(" DataClassThatReferencesItself")
                }

            }


            test("outputs decent error message when there is no public constructor") {
                class WithPrivateConstructor private constructor()
                expectThrows<RandolfException> {
                    Randolf().create(WithPrivateConstructor::class)
                }.message.isNotNull().and {
                    contains(" WithPrivateConstructor")
                    contains("No public constructor ")


                }
            }
        }
    }
}



