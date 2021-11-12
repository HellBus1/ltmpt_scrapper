import model.University
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

fun main() {
//    generateUniversityUrlList()
//    generatePoliteknikUrlList()
//    generateDetailUniverstyUrlList()
}

fun generateDetailUniverstyUrlList() {
    val driver: WebDriver = SafariDriver()
//    val fileName = "src/resources/university_url_list_saintek.txt"
    val fileName = "src/resources/university_url_list_soshum.txt"
    val file = File(fileName)
    val urlList = ArrayList<String>()

    File("src/resources/university_url_list.txt").useLines { lines -> lines.forEach {
        try {
//            driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS)
            driver.get(it)

            val tableContents: List<WebElement> = driver.findElements(By.cssSelector("#jenis2 tbody tr"))
            for (tr in tableContents) {
                val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }
//                println(tdList[2].text)
                urlList.add(tdList[2].findElement(By.cssSelector("a")).getAttribute("href").toString())
            }
            file.printWriter().use { out ->
                urlList.forEach { value -> out.println(value) }
            }
        } catch(e: Exception) {
            print("errror $e")
        }
    }}
    driver.close()
}

fun generatePoliteknikUrlList() {
    val driver: WebDriver = SafariDriver()
    val fileName = "src/resources/politeknik_url_list.txt"
    val file = File(fileName)
    val urlList = ArrayList<String>()
    driver.get("https://sidata-ptn.ltmpt.ac.id/ptn_sb.php?ptn=-2")

    try {
        val tableContents: List<WebElement> = driver.findElements(By.cssSelector("tbody tr"))
        for (tr in tableContents) {
            val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }
            urlList.add("https://sidata-ptn.ltmpt.ac.id/ptn_sb.php?ptn=${tdList[1].text}")
        }
        file.printWriter().use { out ->
            urlList.forEach { value -> out.println(value) }
        }
    } catch (e: Exception) {
        println(e)
    } finally {
        driver.quit()
    }
}

fun generateUniversityUrlList() {
    val driver: WebDriver = SafariDriver()
    val fileName = "src/resources/university_url_list.txt"
    val file = File(fileName)
    val urlList = ArrayList<String>()
    driver.get("https://sidata-ptn.ltmpt.ac.id/ptn_sb.php")

    try {
        val tableContents: List<WebElement> = driver.findElements(By.cssSelector("tbody tr"))
        for (tr in tableContents) {
            val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }
            urlList.add("https://sidata-ptn.ltmpt.ac.id/ptn_sb.php?ptn=${tdList[1].text}")
        }
        file.printWriter().use { out ->
            urlList.forEach { value -> out.println(value) }
        }
    } catch (e: Exception) {
        println(e)
    } finally {
        driver.quit()
    }
}