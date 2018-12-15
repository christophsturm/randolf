# Randolf the reindeer
Randolf is busy 24/7 instantiating your kotlin data classes with autogenerated values

this is how it looks in action.
```
enum class BeanType { ROBUSTA, ARABICA }
data class Group(val name: String)
data class User(
    val firstName: String,
    val name: String,
    val age: Int,
    val lat: Double,
    val lon: Double,
    val favoriteCoffee: BeanType,
    val group: List<Group>
)
print(Randolf.create<User>())
        
//=> User(firstName=UVQyniDCPDuxYleOqVlx, name=xKQtkvybVJGeXHWUPBSE, age=12517728, lat=0.39825335948498386, lon=0.1518043402275937, favoriteCoffee=ARABICA, groups=[Group(name=TCZRDtzVuKrDqrmjgdqS), Group(name= txATFFGAPTXTNBeuCcN), Group(name=EiMytHwslatclHMaCDig), Group(name=I DFdlkHxiXxwXMdjHYo), Group(name=wbhsAlk wPmZkLCkBCJe), Group(name=LwBn WbOrUHKojWjbYBU), Group(name=qZryMZSZMhjgGtYUWQHP), Group(name=QcfoKlV p akKYqrEXRG)])
```

currently supported types
* String
* Int
* Long
* Double
* Enums
* List
* Set
* Collection
* Map

potential next steps: 
* more types
* add an optional non random mode inspired by how fakir works (https://github.com/dmcg/fakir)