package example.tests;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;
import static org.testng.Assert.assertTrue;

public class EventFilteringTest {
    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();

        // go to home page
        driver.get("http://localhost:4200/home");

        // maximize
        driver.manage().window().maximize();

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // log title
        System.out.println("The title of this page is ===> " + driver.getTitle());
    }


    @Test
    public void searchTest() throws InterruptedException {
        goToHomePage();
        WebElement searchInput = driver.findElement(By.cssSelector("input[placeholder='Enter search term']"));
        searchInput.sendKeys("test");
        searchInput.sendKeys(Keys.ENTER);

        Thread.sleep(2000);

        List<WebElement> eventTitles = driver.findElements(By.cssSelector("mat-tab-body .event-card .name"));
        // checking if the event titles contain the search term
        assertFalse(eventTitles.isEmpty(), "No events found for the search term");
        eventTitles.forEach(title -> assertTrue(title.getText().toLowerCase().contains("test")));

        // screenshot
        takeScreenshot("Test1_search.png");
    }

    @Test
    public void searchNoResultsTest() throws InterruptedException {
        goToHomePage();
        WebElement searchInput = driver.findElement(By.cssSelector("input[placeholder='Enter search term']"));
        searchInput.sendKeys("egesb5sb5ssbt5d53w53wbt46d45gdr");
        searchInput.sendKeys(Keys.ENTER);

        waitForNoResults();

        List<WebElement> eventTitles = driver.findElements(By.cssSelector("mat-tab-body .event-card .name"));
        // there should be no results
        assertTrue(eventTitles.isEmpty());
        WebElement noResultsMessage = driver.findElement(By.cssSelector("mat-tab-body .empty-list"));
        assertTrue(noResultsMessage.getText().toLowerCase().contains("no events found"));

        // screenshot
        takeScreenshot("Test2_searchNoResults.png");
    }

    @Test
    public void searchAndClearTest() throws InterruptedException {
        goToHomePage();
        WebElement searchInput = driver.findElement(By.cssSelector("input[placeholder='Enter search term']"));
        searchInput.sendKeys("test");
        searchInput.sendKeys(Keys.ENTER);

        waitForEvents();
        assertTrue(getEventCount() > 0, "No events found for the search term");

        searchInput.clear();
        searchInput.sendKeys(Keys.ENTER);

        waitForEvents();
        assertFalse(driver.findElements(By.cssSelector("mat-tab-body .event-card")).isEmpty());

        // screenshot
        takeScreenshot("Test3_searchAndClear.png");
    }

    @Test
    public void filterByTypeTest() throws InterruptedException {
        goToHomePage();
        int oldCount = getEventCount();

        openFilterDialog();

        // filter options
        WebElement eventTypeSelect = driver.findElement(
                By.xpath("//label[mat-label[text()='Event type:']]/following-sibling::mat-select"));
        eventTypeSelect.click();
        driver.findElement(By.xpath("//mat-option[span[text()='Sajam']]")).click();
        eventTypeSelect.sendKeys(Keys.ESCAPE); // close dropdown

        applyFilters();

        waitForEvents();

        int newCount = getEventCount();
        assertNotEquals(newCount, 0, "Filter returned no results");
        assertTrue(newCount < oldCount, "Filter did not reduce results");

        scrollToLastEvent();
        takeScreenshot("Test4_filterByType.png");
    }

    @Test
    public void filterByMaxAttendancesTest() throws InterruptedException {
        goToHomePage();
        int oldCount = getEventCount();

        openFilterDialog();

        // filter options
        WebElement lowerBound = driver.findElement(By.cssSelector("input[title='Lower max attendances limit']"));
        WebElement upperBound = driver.findElement(By.cssSelector("input[title='Upper max attendances limit']"));
        for (int i = 0; i < 10; i++)
            lowerBound.sendKeys(Keys.ARROW_RIGHT);
        for (int i = 0; i < 10; i++)
            upperBound.sendKeys(Keys.ARROW_LEFT);
        applyFilters();

        waitForEvents();

        int newCount = getEventCount();
        assertNotEquals(newCount, 0, "Filter returned no results");
        assertTrue(newCount < oldCount, "Filter did not reduce results");

        scrollToLastEvent();
        takeScreenshot("Test5_filterByMaxAttendances.png");
    }

    @Test
    public void filterByLocationTest() throws InterruptedException {
        goToHomePage();
        int oldCount = getEventCount();

        openFilterDialog();

        // filter options
        WebElement maximumDistance = driver.findElement(By.cssSelector("input[title='Maximum distance']"));
        for (int i = 0; i < 10; i++)
            maximumDistance.sendKeys(Keys.ARROW_LEFT);

        WebElement eventTypeSelect = driver.findElement(
                By.xpath("//label[mat-label[text()='Location:']]/following-sibling::mat-select"));
        Thread.sleep(1000);
        eventTypeSelect.click();
        Thread.sleep(1500); // wait for dropdown to open
        driver.findElement(By.xpath("//mat-option[span[text()='Kula']]")).click();
        eventTypeSelect.sendKeys(Keys.ESCAPE); // close dropdown

        applyFilters();

        waitForEvents();

        List<WebElement> eventTitles = driver.findElements(By.cssSelector("mat-tab-body .event-card .name"));
        assertFalse(eventTitles.isEmpty(), "No events found for the filter");

        boolean found = false;
        for (WebElement eventTitle : eventTitles) {
            if (eventTitle.getText().toLowerCase().contains("kuli")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "No events containing 'kuli' found");

        int newCount = getEventCount();
        assertNotEquals(newCount, 0, "Filter returned no results");
        assertTrue(newCount < oldCount, "Filter did not reduce results");

        scrollToLastEvent();
        takeScreenshot("Test6_filterByLocation.png");
    }

    @Test
    public void filterByLocationThenDistanceTest() throws InterruptedException {
        goToHomePage();
        int oldCount = getEventCount();

        openFilterDialog();

        WebElement maximumDistance = driver.findElement(By.cssSelector("input[title='Maximum distance']"));
        for (int i = 0; i < 10; i++)
            maximumDistance.sendKeys(Keys.ARROW_LEFT);

        WebElement eventTypeSelect = driver.findElement(
                By.xpath("//label[mat-label[text()='Location:']]/following-sibling::mat-select"));
        Thread.sleep(1000);
        eventTypeSelect.click();
        Thread.sleep(1000); // wait for dropdown to open
        driver.findElement(By.xpath("//mat-option[span[text()='Kula']]")).click();
        eventTypeSelect.sendKeys(Keys.ESCAPE); // close dropdown

        applyFilters();
        waitForEvents();

        int midCount = getEventCount();
        assertTrue(midCount < oldCount, "Filter did not reduce results");

        openFilterDialog();
        WebElement maximumDistance2 = driver.findElement(By.cssSelector("input[title='Maximum distance']"));
        for (int i = 0; i < 10; i++)
            maximumDistance2.sendKeys(Keys.ARROW_RIGHT);

        Thread.sleep(1000);
        applyFilters();
        Thread.sleep(1000);
        waitForEvents();

        int newCount = getEventCount();
        assertNotEquals(newCount, 0, "Filter returned no results");
        assertTrue(newCount > midCount, "Relaxing distance filter did not increase results");

        scrollToLastEvent();
        takeScreenshot("Test7_filterByDistance.png");
    }

    @Test
    public void filterByDateTest() throws InterruptedException {
        goToHomePage();
        int oldCount = getEventCount();

        openFilterDialog();

        // filter options
        WebElement dateStart = driver.findElement(By.cssSelector("input[placeholder='Start date']"));
        WebElement dateEnd = driver.findElement(By.cssSelector("input[placeholder='End date']"));
        dateStart.sendKeys("08/03/2025"); // MM/DD/YYYY
        Thread.sleep(200);
        dateEnd.sendKeys("09/03/2025"); // MM/DD/YYYY

        Thread.sleep(500);
        applyFilters();
        Thread.sleep(500);

        waitForEvents();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH);
        List<WebElement> eventDates = driver.findElements(By.cssSelector("mat-tab-body .event-card .date"));
        LocalDate startDate = LocalDate.of(2025, 8, 3);
        LocalDate endDate = LocalDate.of(2025, 9, 3);
        for (WebElement eventDate : eventDates) {
            LocalDate date = LocalDate.parse(eventDate.getText().trim(), formatter);
            assertTrue(!date.isBefore(startDate) && !date.isAfter(endDate),
                    "Date not in range: " + eventDate.getText());
        }

        int newCount = getEventCount();
        assertNotEquals(newCount, 0, "Filter returned no results");
        assertTrue(newCount < oldCount, "Filter did not reduce results");

        scrollToLastEvent();
        takeScreenshot("Test8_filterByDate.png");
    }

    @Test
    public void filterByTypeAndMaxAttendancesSuccessfulTest_ShouldReturnResults() throws InterruptedException {
        goToHomePage();
        int oldCount = getEventCount();

        openFilterDialog();

        // filter options
        WebElement eventTypeSelect = driver.findElement(
                By.xpath("//label[mat-label[text()='Event type:']]/following-sibling::mat-select"));
        eventTypeSelect.click();
        driver.findElement(By.xpath("//mat-option[span[text()='Wedding']]")).click();
        eventTypeSelect.sendKeys(Keys.ESCAPE); // close dropdown

        WebElement lowerBound = driver.findElement(By.cssSelector("input[title='Lower max attendances limit']"));
        WebElement upperBound = driver.findElement(By.cssSelector("input[title='Upper max attendances limit']"));
        for (int i = 0; i < 10; i++)
            lowerBound.sendKeys(Keys.ARROW_RIGHT);
        for (int i = 0; i < 10; i++)
            upperBound.sendKeys(Keys.ARROW_LEFT);
        applyFilters();

        waitForEvents();

        int newCount = getEventCount();
        assertNotEquals(newCount, 0, "Filter returned no results");
        assertTrue(newCount < oldCount, "Filter did not reduce results");

        scrollToLastEvent();
        takeScreenshot("Test9_filterByTypeAndMaxAttendancesSuccessful.png");
    }

    @Test
    public void filterByTypeAndMaxAttendancesNoResultsTest_ShouldReturnNoResults() throws InterruptedException {
        goToHomePage();

        openFilterDialog();

        // filter options
        WebElement eventTypeSelect = driver.findElement(
                By.xpath("//label[mat-label[text()='Event type:']]/following-sibling::mat-select"));
        eventTypeSelect.click();
        driver.findElement(By.xpath("//mat-option[span[text()='Sajam']]")).click();
        eventTypeSelect.sendKeys(Keys.ESCAPE); // close dropdown

        WebElement lowerBound = driver.findElement(By.cssSelector("input[title='Lower max attendances limit']"));
        WebElement upperBound = driver.findElement(By.cssSelector("input[title='Upper max attendances limit']"));
        for (int i = 0; i < 10; i++)
            lowerBound.sendKeys(Keys.ARROW_RIGHT);
        for (int i = 0; i < 40; i++)
            upperBound.sendKeys(Keys.ARROW_LEFT);
        applyFilters();

        waitForNoResults();

        int newCount = getEventCount();
        assertEquals(newCount, 0, "Filter returned results");

        scrollToLastEvent();
        takeScreenshot("Test10_filterByTypeAndMaxAttendancesNoResults.png");
    }
    @Test
    public void searchAndFilterTest() throws InterruptedException {
        goToHomePage();
        WebElement searchInput = driver.findElement(By.cssSelector("input[placeholder='Enter search term']"));
        searchInput.sendKeys("test");
        searchInput.sendKeys(Keys.ENTER);
        Thread.sleep(500);

        openFilterDialog();
        WebElement eventTypeSelect = driver.findElement(
                By.xpath("//label[mat-label[text()='Event type:']]/following-sibling::mat-select"));
        eventTypeSelect.click();
        driver.findElement(By.xpath("//mat-option[span[text()='Wedding']]")).click();
        eventTypeSelect.sendKeys(Keys.ESCAPE); // close dropdown
        Thread.sleep(500);
        applyFilters();
        waitForEvents();


        List<WebElement> eventTitles = driver.findElements(By.cssSelector("mat-tab-body .event-card .name"));
        // checking if the event titles contain the search term
        assertFalse(eventTitles.isEmpty(), "No events found for the search term and filter");
        eventTitles.forEach(title -> assertTrue(title.getText().toLowerCase().contains("test")));
        assertEquals(eventTitles.size(), 1, "Search and filter did not return the expected results");

        // screenshot
        takeScreenshot("Test11_searchAndFilter.png");
    }


    private void takeScreenshot(String filename) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File file = ts.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File("./ScreenShot_Folder/EventFiltering/" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Screenshot saved: " + filename);
    }

    private void goToHomePage() {
        driver.get("http://localhost:4200/home");
    }
    private void waitForEvents() {
        try {
            Thread.sleep(500); // wait for current results to disappear
        } catch (InterruptedException e) {
            return;
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("mat-tab-body .event-card")));
    }
    private void waitForNoResults() {
        try {
            Thread.sleep(500); // wait for current results to disappear
        } catch (InterruptedException e) {
            return;
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("mat-tab-body .empty-list"), "No events found"));
    }
    private int getEventCount() {
        String label = driver.findElement(By.cssSelector(".mat-mdc-paginator-range-label")).getText();
        String total = label.substring(label.lastIndexOf(" ")+1);
        try {
            return Integer.parseInt(total);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private void openFilterDialog() {
        driver.findElement(By.cssSelector("button.filter-btn")).click();
    }
    private void applyFilters() {
        driver.findElement(By.cssSelector("mat-dialog-container button[type='submit']")).click();
    }
    private void scrollToLastEvent() throws InterruptedException {
        List<WebElement> eventCards = driver.findElements(By.cssSelector("mat-tab-body .event-card"));
        WebElement lastCard;
        if (!eventCards.isEmpty())
            lastCard = eventCards.get(eventCards.size() - 1);
        else
            lastCard = driver.findElement(By.cssSelector("mat-tab-body .empty-list"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", lastCard);
        Thread.sleep(200);
    }

    @AfterClass
    public void tearDown() {
//        try {
//            // Click the Delete button
//            WebElement deleteBtn = driver.findElement(By.xpath("//span[text()='Delete']/ancestor::button"));
//            deleteBtn.click();
//
//            // Wait for the confirmation dialog to appear and click "Yes, delete"
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
//            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
//                    By.id("delete") // id of the confirm button
//            ));
//            confirmBtn.click();
//
//            // Optional: wait a moment for deletion to complete
//            Thread.sleep(1000);
//        } catch (Exception e) {
//            System.out.println("No activity to delete or already deleted.");
//        } finally {
            driver.quit();
//        }
    }
}
