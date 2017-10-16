import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by brian on 10/14/17.
 */
public class Main {

	public static final String USERNAME = "";
	public static final String PASSWORD = "";


	public static void main(String[] args) {
		// declaration and instantiation of objects/variables
		System.setProperty("webdriver.firefox.marionette", "/home/brian/youtubeadvertiser/geckodriver");
		WebDriver driver = new FirefoxDriver();
		//comment the above 2 lines and uncomment below 2 lines to use Chrome
		//System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();

		String baseUrl = "https://accounts.google.com/signin";


		// launch Fire fox and direct it to the Base URL
		driver.get(baseUrl);

        driver.findElement(By.id("identifierId")).sendKeys(USERNAME);

		driver.findElement(By.id("identifierNext")).click();

		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));

		driver.findElement(By.name("password")).sendKeys(PASSWORD);

		driver.findElement(By.name("password")).sendKeys(Keys.ENTER);

		wait.until(ExpectedConditions.titleIs("My Account"));

		Set<String> visited = new HashSet<String>();
		Queue<String> relatedVideos = new LinkedList<String>();

		// Start node
		try {
			commentOnVideo("https://www.youtube.com/watch?v=VWUh4z57v9o&t=3s", relatedVideos, driver);
		} catch (Exception e) {
			e.printStackTrace();
		}



		while(visited.size() < 1000) {
			String nextVideo = relatedVideos.poll();
			if (visited.contains(nextVideo)) {
				continue;
			}
			try {
				commentOnVideo(nextVideo, relatedVideos, driver);
			} catch (Exception e) {
				e.printStackTrace();
				writeLog("Failed to comment on video: " + nextVideo);
			}
			visited.add(nextVideo);
			writeLog(nextVideo);
			System.out.println("related video queue size " + relatedVideos.size());
		}



		//close Fire fox
//		driver.close();
	}

	static void commentOnVideo(String videoID, Queue<String> relatedVideos, WebDriver driver) {

		WebDriverWait wait = new WebDriverWait(driver, 8);
		driver.get(videoID);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("related")));

		// This  will scroll page 750 pixel vertical
		((JavascriptExecutor)driver).executeScript("scroll(0,750)");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("simplebox-placeholder")));

		driver.findElement(By.id("simplebox-placeholder")).click();

		driver.findElement(By.tagName("textarea")).sendKeys("Wow this is almost as great as pubgswag.com");

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		driver.findElement(By.cssSelector("#submit-button a[is='yt-endpoint'")).click();

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<WebElement> hrefList =  driver.findElements(By.cssSelector(".yt-simple-endpoint.style-scope.ytd-compact-video-renderer"));

		for (WebElement video : hrefList) {
			String related = video.getAttribute("href");
			relatedVideos.add(related);
		}

	}

	static void writeLog(String message) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("/home/brian/youtubeadvertiser/visited.txt", true));
			writer.append('\n');
			writer.append(message);

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
