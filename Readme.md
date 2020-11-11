# Randolf the reindeer
Autogenerated data for your tests.

[![Download](https://api.bintray.com/packages/christophsturm/maven/randolf/images/download.svg)](https://bintray.com/christophsturm/maven/randolf/_latestVersion)
[![Github CI](https://github.com/christophsturm/randolf/workflows/CI/badge.svg)](https://github.com/christophsturm/randolf/actions)

This is how it looks in action:
```
enum class BeanType { ROBUSTA, ARABICA }
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
print(Randolf().create<User>())
        
//=> User(firstName=QV muCLbUVfVheboeSuN, name=MSdOzpRCFIykprACOHjv, age=2116525025, lat=0.9518687079417872, lon=0.8331958938906572, isBoss=false, teamSize=4310, flags=83, shortName=Z, efficiency=0.76879007, favoriteCoffee=ARABICA, groups=[Group(name=TpiFXUrucFA cMARKLiR), Group(name=floFeNep XBEZUfXwQgL)])
```

# Usage:
Add this to your Gradle dependencies:

```
    testCompile("com.christophsturm:randolf:0.1.0")
```

Add JCenter if you haven't already:

```
repositories {
    jcenter()
}
```

You can create any Kotlin data class like in the example above. All fields will be set to random values,
even nullable fields.
You can chose to set only necessary fields by calling `Randolf.create(RandolfConfig(minimal=true))` instead.
This will set all nullable fields to null, numbers to 0 and make strings, lists and maps empty.
For more usage examples take a look at the [unit tests](src/test/kotlin/randolf/RandolfTest.kt).


Currently supported types:
* String
* Int
* Long
* Double
* Short
* Float
* Byte
* Boolean
* Char
* Enums
* List
* Set
* Collection
* Map

# Configuration

If you want to change defaults or support more types, you can pass a [RandolfConfig](src/main/kotlin/randolf/RandolfConfig.kt) to the Randolf constructor.

Here is a config that returns now for `Instant`:
```
RandolfConfig(additionalValueCreators = mapOf(Instant::class to { _, _ -> Instant.now() }))
```
You can override supported types, and use the name and type of the field:
```
RandolfConfig(additionalValueCreators = mapOf(String::class to { type: KType, name: String ->
                    "field $name of type $type"
                }))
```
You can also inject a random generator for example to set the seed: 
```
RandolfConfig(random = Random(1234))
```
 
Next steps:
* add an optional non random mode inspired by [Fakir](https://github.com/dmcg/fakir)
* build for Kotlin/JS and Kotlin/Native. 

[![Download](https://api.bintray.com/packages/christophsturm/maven/randolf/images/download.svg)](https://bintray.com/christophsturm/maven/randolf/_latestVersion)
[![CircleCI](https://circleci.com/gh/christophsturm/randolf/tree/master.svg?style=svg)](https://circleci.com/gh/christophsturm/randolf/tree/master)
