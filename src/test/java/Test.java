import com.augmentedframework.utils.Log;
import org.testng.Assert;

public class Test {

    @org.testng.annotations.Test
    public void loggingTest() {
        Log.message("Test..");
        Log.event("Event Test");
        Assert.assertTrue(true);
    }
}
