import org.testng.annotations.Test;

public class IsolatePageTest {
    @Test
    public void testHarGenerator() {
        HarGenerator.getHar("https://www.google.com");
    }
}
