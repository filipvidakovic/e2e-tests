package example.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class BudgetTest {

    private WebDriver driver;
    private WebDriverWait wait;

    static final String EMAIL = "test@org.com";
    static final String PASSWORD = "123456";

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

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
    }

//    @Test
//    void shouldShowNameRequiredError() throws InterruptedException {
//        driver.get("http://localhost:4200/my-events");
//
//        WebElement seeBudgetPlanBtn = wait.until(
//                ExpectedConditions.elementToBeClickable(
//                        By.xpath("(//mat-card[contains(@class,'event-card')])[1]//button[contains(.,'See budget plan')]")
//                )
//        );
//        Thread.sleep(2000);
//        seeBudgetPlanBtn.click();
//
//        WebElement nameInput = wait.until(
//                ExpectedConditions.elementToBeClickable(By.cssSelector("input[formControlName='name']"))
//        );
//        nameInput.clear();
//        Thread.sleep(1000);
//
//        WebElement spendingInput = driver.findElement(By.cssSelector("input[formControlName='plannedSpending']"));
//        spendingInput.sendKeys("100");
//        Thread.sleep(1000);
//
//        WebElement categorySelect = driver.findElement(By.cssSelector("mat-select[formControlName='category']"));
//        categorySelect.click();
//        Thread.sleep(1000);
//
//        WebElement firstOption = wait.until(
//                ExpectedConditions.elementToBeClickable(By.cssSelector("mat-option:nth-child(1)"))
//        );
//        firstOption.click();
//        Thread.sleep(1000);
//
//        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
//        submitButton.click();
//
//        WebElement nameError = wait.until(
//                ExpectedConditions.visibilityOfElementLocated(
//                        By.xpath("//mat-error[contains(text(),'This field is required.')]")
//                )
//        );
//        assertTrue(nameError.isDisplayed(), "Error message for empty name should be visible");
//    }
//
//    @Test
//    void shouldShowNameAmountError() throws InterruptedException {
//
//        WebElement nameInput = wait.until(
//                ExpectedConditions.elementToBeClickable(By.cssSelector("input[formControlName='name']"))
//        );
//        nameInput.sendKeys("Test Budget Item");
//        Thread.sleep(1000);
//
//        WebElement spendingInput = driver.findElement(By.cssSelector("input[formControlName='plannedSpending']"));
//        spendingInput.clear();
//        Thread.sleep(200);
//        spendingInput.sendKeys("-50");
//        Thread.sleep(1000);
//
//        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
//        submitButton.click();
//
//        WebElement amountError = wait.until(
//                ExpectedConditions.visibilityOfElementLocated(
//                        By.xpath("//mat-error[contains(text(),'Minimal amount is 0.')]")
//                )
//        );
//        assertTrue(amountError.isDisplayed(), "Error message for invalid amount should be visible");
//    }


    @Test
    void shouldCreateNewBudgetItem() throws InterruptedException{
        driver.get("http://localhost:4200/my-events");
        WebElement seeBudgetPlanBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//mat-card[contains(@class,'event-card')])[1]//button[contains(.,'See budget plan')]")
                )
        );
        seeBudgetPlanBtn.click();

        WebElement nameInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[formControlName='name']"))
        );
        nameInput.sendKeys("Test Budget Item");
        Thread.sleep(1000);

        WebElement spendingInput = driver.findElement(By.cssSelector("input[formControlName='plannedSpending']"));
        spendingInput.sendKeys("150");
        Thread.sleep(1000);

        WebElement categorySelect = driver.findElement(By.cssSelector("mat-select[formControlName='category']"));
        categorySelect.click();
        Thread.sleep(1000);

        WebElement firstOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("mat-option:nth-child(1)"))
        );
        firstOption.click();
        Thread.sleep(1000);

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        WebElement createdItem = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Test Budget Item')]"))
        );
        assertTrue(createdItem.isDisplayed(), "Newly created budget item should be visible");
    }
}
