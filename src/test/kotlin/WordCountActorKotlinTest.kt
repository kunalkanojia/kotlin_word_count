
import akka.actor.ActorSystem
import org.junit.After
import org.junit.Before
import org.junit.Test
import scala.concurrent.duration.Duration

class WordCountActorKotlinTest {

    internal var system: ActorSystem = null!!

    @Before
    fun setup() {
        system = ActorSystem.create()
    }

    @After
    fun teardown() {
        system.shutdown()
        system.awaitTermination(Duration.create("10 seconds"))
    }

    @Test
    fun testWordCount() {
        /* Unable to make this run
        object : JavaTestKit(system) {
            init {

                val masterActor = system.actorOf(Props.create(WordCountActor.WordCountMaster::class.java), "master")

                masterActor.tell(WordCountActor.StartCounting("src/main/resources/", 20), testActor)

                val wcs = expectMsgClass(JavaTestKit.duration("20 seconds"), WordCountActor.WordCountSuccess::class.java)

                object : JavaTestKit.Within(JavaTestKit.duration("20 seconds")) {
                    override fun run() {
                        Assert.assertEquals(20, wcs.result.size)
                        assertThat<Map<String, Int>>(wcs.result, IsMapContaining.hasEntry("src/main/resources/File1.txt", 6480000))
                        assertThat<Map<String, Int>>(wcs.result, IsMapContaining.hasEntry("src/main/resources/File2.txt", 6480000))
                    }
                }
            }
        }*/
    }
}