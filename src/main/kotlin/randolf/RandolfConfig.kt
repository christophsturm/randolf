package randolf

import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * This class can be passed to the Randolf constructor to specify the configuration.
 * The parameter default values are the default config.
 * @property minimal enable minimal mode where all nullable fields are null,
 * all fields with default values get their default value, all numbers are 0 and all strings, and collections are empty
 * @property additionalValueCreators adds value creators that are not supported out of the box
 * @property random inject a random instance. beware that random instances are mutable so don't reuse them
 *
 */
data class RandolfConfig(
    val minimal: Boolean = false,
    val stringLength: Int = 20,
    val stringCharacters: List<Char> = ('A'..'Z').toList() + (('a'..'z').toList()).plus(' '),
    val maxCollectionSize: Int = 10,
    val additionalValueCreators: Map<KClass<*>, (type: KType, name: String) -> Any> = emptyMap(),
    val random: Random = Random
)