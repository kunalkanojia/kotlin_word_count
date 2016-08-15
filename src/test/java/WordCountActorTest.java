import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.hamcrest.collection.IsMapContaining;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import static org.hamcrest.MatcherAssert.assertThat;

public class WordCountActorTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void teardown() {
    system.shutdown();
    system.awaitTermination(Duration.create("10 seconds"));
  }

  @Test
  public void testWordCount() {
    new JavaTestKit(system) {{

      final ActorRef masterActor = system.actorOf(Props.create(WordCountActor.WordCountMaster.class), "master");

      masterActor.tell(new WordCountActor.StartCounting("src/main/resources/", 2), getTestActor());

      final WordCountActor.WordCountSuccess wcs = expectMsgClass(duration("20 seconds"), WordCountActor.WordCountSuccess.class);

      new Within(duration("20 seconds")) {
        protected void run() {
          Assert.assertEquals(2, wcs.getResult().size());
          assertThat(wcs.getResult(), IsMapContaining.hasEntry("src/main/resources/File1.txt", 500));
          assertThat(wcs.getResult(), IsMapContaining.hasEntry("src/main/resources/File2.txt", 500));
        }
      };
    }};
  }
}
