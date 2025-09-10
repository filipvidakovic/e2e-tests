package example.tests;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class EventsTest {
    private WebDriver driver;

    static final String EMAIL = "ep@gmail.com";
    static final String PASSWORD = "123123";

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();

        // go to login page
        driver.get("http://localhost:4200/signin");

        // maximize
        driver.manage().window().maximize();

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // log title
        System.out.println("The title of this page is ===> " + driver.getTitle());
    }

    @Test
    public void loginTest() throws InterruptedException {
        // enter email
        driver.findElement(By.cssSelector("input[formcontrolname='email']")).clear();
        driver.findElement(By.cssSelector("input[formcontrolname='email']")).sendKeys(EMAIL);

        // enter password
        driver.findElement(By.cssSelector("input[formcontrolname='password']")).clear();
        driver.findElement(By.cssSelector("input[formcontrolname='password']")).sendKeys(PASSWORD);

        // click login
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        // assert redirected or snackbar visible
        assertTrue(driver.getPageSource().contains("Top 5 events"));

        // screenshot
        takeScreenshot("Test1_Login.png");
    }

    @Test(dependsOnMethods = "loginTest")
    public void createEventTestSuccess() throws InterruptedException {
        // go directly to new event page
        driver.get("http://localhost:4200/new-event");

        Thread.sleep(2000);
        // fill out event form (adjust IDs/names to your template)
        driver.findElement(By.id("name")).sendKeys("Automation Test Event");
        driver.findElement(By.id("description")).sendKeys("Created by Selenium");
        driver.findElement(By.id("max-attendances")).sendKeys("50");

        WebElement dateInput = driver.findElement(By.id("date"));
        dateInput.sendKeys("2025-12-31");

        // mat-select for event type
        WebElement select = driver.findElement(By.cssSelector("mat-select[formcontrolname='eventType']"));
        select.click();
        WebElement option = driver.findElement(By.cssSelector("mat-option"));

        option.click();

        // check "open"
        driver.findElement(By.id("is-open")).click();

        // save event
        driver.findElement(By.xpath("//button[contains(text(),'Save')]")).click();

        Thread.sleep(2000);

        // assert snackbar
        assertTrue(driver.getPageSource().toLowerCase().contains("success"));

        // screenshot
        takeScreenshot("Test2_CreateEvent.png");
    }

    @Test(dependsOnMethods = "loginTest")
    public void createEventTest_ShouldRedirectToHomePage() throws InterruptedException {
        // go directly to new event page
        driver.get("http://localhost:4200/new-event");

        // fill out form with invalid data (e.g., missing name, invalid date)
        driver.findElement(By.id("description")).sendKeys("This should fail");
        driver.findElement(By.id("max-attendances")).sendKeys("0"); // invalid attendance

        WebElement dateInput = driver.findElement(By.id("date"));
        dateInput.sendKeys("2020-01-01"); // past date should fail

        // skip event type selection → required

        // try to save
        driver.findElement(By.xpath("//button[contains(text(),'Cancel')]")).click();

        Thread.sleep(2000);

        // assert
        assertTrue(driver.getPageSource().contains("Top 5 events"));

        // screenshot
        takeScreenshot("Test3_CreateEventFail.png");
    }

    @Test(dependsOnMethods = "loginTest")
    public void createEventFailTest_ErrorMessage() throws InterruptedException {
        // go directly to new event page
        driver.get("http://localhost:4200/new-event");

        // fill out form with invalid data (e.g., missing name, invalid date)
        driver.findElement(By.id("description")).sendKeys("This should fail");
        driver.findElement(By.id("max-attendances")).sendKeys("0"); // invalid attendance

        WebElement dateInput = driver.findElement(By.id("date"));
        dateInput.sendKeys("2020-01-01"); // past date should fail

        // skip event type selection → required

        // try to save
        driver.findElement(By.xpath("//button[contains(text(),'Save')]")).click();

        Thread.sleep(2000);

        // assert snackbar shows failure
        assertTrue(driver.getPageSource().toLowerCase().contains("fail")
                        || driver.getPageSource().toLowerCase().contains("error"),
                "Expected failure message not found");

        // screenshot
        takeScreenshot("Test3_CreateEventFail.png");
    }

    @Test(dependsOnMethods = "loginTest")
    public void createEventFailTest_DisabledButton() throws InterruptedException {
        // go directly to new event page
        driver.get("http://localhost:4200/new-event");

        // fill out form with invalid data (e.g., missing name, invalid date)
        driver.findElement(By.id("description")).sendKeys("This should fail");
        driver.findElement(By.id("max-attendances")).sendKeys("0"); // invalid attendance

        WebElement dateInput = driver.findElement(By.id("date"));
        dateInput.sendKeys("2020-01-01"); // past date should fail

        // skip event type selection → required

        // try to save
        WebElement button = driver.findElement(By.xpath("//button[.//span[normalize-space()='Agenda']]"));

        Thread.sleep(2000);

        // assert snackbar shows failure
        assertFalse(button.isEnabled(), "Expected agenda button to be disabled for non-existent event");

        // screenshot
        takeScreenshot("Test3_CreateEventFail.png");
    }

    //Agenda tests
    @Test(dependsOnMethods = {"loginTest", "createEventTestSuccess"})
    public void addActivity_Success() throws InterruptedException {
        // navigate to my events
        driver.get("http://localhost:4200/my-events");

        // open first event (adjust if you need specific one)
        driver.findElement(By.xpath("//button[.//span[normalize-space()='View Details'] or normalize-space()='Details']")).click();

        Thread.sleep(2000);

        assertEquals(driver.getCurrentUrl().split("\\?")[0], "http://localhost:4200/new-event");

        // open agenda
        driver.findElement(By.xpath("//button[.//span[normalize-space()='Agenda'] or normalize-space()='Agenda']")).click();

        Thread.sleep(2000);

        // click add new activity
        driver.findElement(By.xpath("//button[.//span[normalize-space()='+ Add New Activity'] or normalize-space()='+ Add New Activity']")).click();

        Thread.sleep(1000);
        // fill out dialog
        driver.findElement(By.cssSelector("input[formcontrolname='name']")).sendKeys("Kickoff");
        driver.findElement(By.cssSelector("input[formcontrolname='description']")).sendKeys("Opening speech");
        driver.findElement(By.cssSelector("input[formcontrolname='startTime']")).sendKeys("09:00");
        driver.findElement(By.cssSelector("input[formcontrolname='endTime']")).sendKeys("10:00");
        driver.findElement(By.cssSelector("input[formcontrolname='location']")).sendKeys("Main Hall");

        Thread.sleep(1000);
        // save
        driver.findElement(By.xpath("//button[normalize-space()='Save']")).click();

        Thread.sleep(2000);

        // assert snackbar
        assertTrue(driver.getPageSource().toLowerCase().contains("success"),
                "Expected success message not found");

        takeScreenshot("Test4_AddActivity_Success.png");
    }

    @Test(dependsOnMethods = "addActivity_Success")
    public void addActivity_Fail_EmptyName() throws InterruptedException {
        // open agenda again
        driver.findElement(By.xpath("//button[.//span[normalize-space()='+ Add New Activity'] or normalize-space()='+ Add New Activity']")).click();
        Thread.sleep(1000);

        // leave name empty
        driver.findElement(By.cssSelector("input[formcontrolname='description']")).sendKeys("No name here");
        driver.findElement(By.cssSelector("input[formcontrolname='startTime']")).sendKeys("11:00");
        driver.findElement(By.cssSelector("input[formcontrolname='endTime']")).sendKeys("12:00");
        driver.findElement(By.cssSelector("input[formcontrolname='location']")).sendKeys("Room 1");

        // save
        Thread.sleep(2000);

        WebElement saveBtn = driver.findElement(By.xpath("//button[normalize-space()='Save']"));
        assertFalse(saveBtn.isEnabled());
        driver.findElement(By.xpath("//button[normalize-space()='Cancel']")).click();

        takeScreenshot("Test5_AddActivity_Fail_EmptyName.png");
    }

    @Test(dependsOnMethods = "addActivity_Success")
    public void addActivity_Fail_InvalidTimeRange() throws InterruptedException {
        // open agenda again
        driver.findElement(By.xpath("//button[.//span[normalize-space()='+ Add New Activity'] or normalize-space()='+ Add New Activity']")).click();
        Thread.sleep(1000);

        driver.findElement(By.cssSelector("input[formcontrolname='name']")).sendKeys("Invalid Time");
        driver.findElement(By.cssSelector("input[formcontrolname='description']")).sendKeys("End before start");
        driver.findElement(By.cssSelector("input[formcontrolname='startTime']")).sendKeys("15:00");
        driver.findElement(By.cssSelector("input[formcontrolname='endTime']")).sendKeys("14:00");
        driver.findElement(By.cssSelector("input[formcontrolname='location']")).sendKeys("Room 2");

        Thread.sleep(1000);

        driver.findElement(By.xpath("//button[normalize-space()='Save']")).click();

        Thread.sleep(2000);

        // assert snackbar invalid time range
        assertTrue(driver.getPageSource().toLowerCase().contains("invalid time range"),
                "Expected invalid time range message not found");

        takeScreenshot("Test6_AddActivity_Fail_InvalidTime.png");
    }

    @Test(dependsOnMethods = "addActivity_Success")
    public void addActivity_Fail_Overlap() throws InterruptedException {
        // try to add overlapping activity
        driver.findElement(By.xpath("//button[.//span[normalize-space()='+ Add New Activity'] or normalize-space()='+ Add New Activity']")).click();
        Thread.sleep(1000);

        driver.findElement(By.cssSelector("input[formcontrolname='name']")).sendKeys("Overlap Test");
        driver.findElement(By.cssSelector("input[formcontrolname='description']")).sendKeys("Should overlap");
        driver.findElement(By.cssSelector("input[formcontrolname='startTime']")).sendKeys("09:30");
        driver.findElement(By.cssSelector("input[formcontrolname='endTime']")).sendKeys("10:30");
        driver.findElement(By.cssSelector("input[formcontrolname='location']")).sendKeys("Main Hall");
        Thread.sleep(1000);

        driver.findElement(By.xpath("//button[normalize-space()='Save']")).click();

        Thread.sleep(2000);

        // assert snackbar invalid time range
        assertTrue(driver.getPageSource().toLowerCase().contains("invalid time range"),
                "Expected overlap error not found");

        takeScreenshot("Test7_AddActivity_Fail_Overlap.png");
    }

    private void takeScreenshot(String filename) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File file = ts.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File("./ScreenShot_Folder/" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Screenshot saved: " + filename);
    }

    @AfterClass
    public void tearDown() {
        try {
            // Click the Delete button
            WebElement deleteBtn = driver.findElement(By.xpath("//span[text()='Delete']/ancestor::button"));
            deleteBtn.click();

            // Wait for the confirmation dialog to appear and click "Yes, delete"
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("delete") // id of the confirm button
            ));
            confirmBtn.click();

            // Optional: wait a moment for deletion to complete
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("No activity to delete or already deleted.");
        } finally {
            driver.quit();
        }
    }


}
