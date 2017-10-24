import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;
import java.util.NoSuchElementException;

/**
 * Created by brian on 10/14/17.
 */
public class Main {


	public static void main(String[] args) {

		System.setProperty("webdriver.firefox.marionette", "/home/brian/youtubeadvertiser/geckodriver");
		WebDriver driver = new FirefoxDriver();
		//comment the above 2 lines and uncomment below 2 lines to use Chrome
		//System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();

		String baseUrl = "https://accounts.google.com/signin";


		// launch Fire fox and direct it to the Base URL
		driver.get(baseUrl);

		driver.findElement(By.id("identifierId")).sendKeys(Creds.USERNAME);

        driver.findElement(By.id("identifierNext")).click();

		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));

		driver.findElement(By.name("password")).sendKeys(Creds.PASSWORD);

		driver.findElement(By.name("password")).sendKeys(Keys.ENTER);

		wait.until(ExpectedConditions.titleIs("My Account"));

		Set<String> visited = getVisitedNodesFromLogs();
		Queue<String> relatedVideos = new LinkedList<String>();

		// Start node
		try {
			commentOnVideo("https://www.youtube.com/watch?v=Aqioba4qHZk", relatedVideos, driver);
		} catch (Exception e) {
			e.printStackTrace();
		}



		while(visited.size() < 10000 && relatedVideos.size() > 0) {
			String nextVideo = relatedVideos.poll();
			if (visited.contains(nextVideo)) {
				System.out.println(nextVideo + " was in the visited log");
				continue;
			}
			try {
				commentOnVideo(nextVideo, relatedVideos, driver);
			} catch (Exception e) {
				e.printStackTrace();
				writeLog("Failed to comment on video: " + nextVideo);
			}
			visited.add(nextVideo);
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

		boolean pubgVideoCategory = isPubgVideo(driver);

		if(pubgVideoCategory && !alreadyCommented(driver)) {
			// Click the submit button
			driver.findElement(By.cssSelector("#submit-button a[is='yt-endpoint'")).click();
			writeLog(videoID);
		}

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}


		// Only add the list of related videos, if the current video is a PUBG related video.
		// No need to explore unrelated branches of the tree.
		// Only targeting PUBG players.
		if (pubgVideoCategory) {
			thumbsUp(driver);
			List<WebElement> hrefList =  driver.findElements(By.cssSelector(".yt-simple-endpoint.style-scope.ytd-compact-video-renderer"));

			for (WebElement video : hrefList) {
				String related = video.getAttribute("href");
				relatedVideos.add(related);
			}
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

	static boolean isPubgVideo(WebDriver driver) {

		WebElement element;
		try {
			 element = driver.findElement(By.cssSelector("a[href='https://gaming.youtube.com/game/UC_qMbhHGOBF_dWCmXd5x4OQ']"));
		} catch(org.openqa.selenium.NoSuchElementException e) {
			System.out.println("This is not a PUBG related video");
			return false;
		}

		if(element == null) {
			return false;
		}

		return true;
	}

	static boolean alreadyCommented(WebDriver driver) {
		List<WebElement> elements = driver.findElements(By.cssSelector("#content-text"));
		for(WebElement element : elements) {
			if (element.getText().contains("Wow this is almost as great as pubgswag.com")) {
				System.out.println("Already commented on this video");
				return true;
			}
		}
		return false;
	}

	static void thumbsUp(WebDriver driver) {
		// TODO: make sure we are upvoting our own comment
//		List<WebElement> elements = driver.findElements(By.cssSelector("#comment"));
//		for(WebElement element : elements) {
//			if (element.findElement(By.cssSelector("#content-text")).getText().contains("Wow this is almost as great as pubgswag.com")) {
//				WebElement thumbUp = element.findElement(By.cssSelector("#like-button"));
//				thumbUp.click();
//			}
//		}

		List<WebElement> thumbUpList = driver.findElements(By.cssSelector("#like-button"));
				thumbUpList.get(0).click();
	}

	static Set<String> getVisitedNodesFromLogs() {
		Set<String> visited = new HashSet<String>();
		Scanner fileScanner;
		try {
			fileScanner = new Scanner(new File("/home/brian/youtubeadvertiser/visited.txt"));
			while (fileScanner.hasNext()){
				visited.add(fileScanner.next());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return visited;
	}
}
