package randolf

import failgood.Test
import failgood.describe

@Test
class RandoldConfigDSLTest {
    val context = describe(RandolfConfigDSL::class){
        randolf {  }
    }
}
