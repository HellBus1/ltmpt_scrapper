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
import kotlin.collections.HashMap

fun main() {
//    generateUniversityUrlList()
//    generatePoliteknikUrlList()
//    generateDetailUniverstyUrlList()
    generateUniversityListDocument()
}

fun generateUniversityListDocument() {
    val driver: WebDriver = SafariDriver()
    val dataMap = HashMap<String, Any?>()

    File("src/resources/university_url_list_saintek.txt").useLines { lines -> lines.forEach {
        try {
//            driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS)
            driver.get(it)

            val sebaranPeminat: List<WebElement> = driver.findElements(By.cssSelector(".table-condensed tr"))
            val informasiUmum: List<WebElement> = driver.findElements(By.cssSelector("td[valign=\"top\"] .panel-body .table tbody tr"))
            val university: WebElement = driver.findElement(By.cssSelector(".well .panel-title"))
            val prodi: WebElement = driver.findElement(By.cssSelector("td[valign=\"top\"] .panel-title"))

            var counter = 1
            var informasiCounter = 0

            var majorCode = ""
            var majorName = ""
            var majorLevel = ""
            var majorType = ""

            val yearMap = mutableMapOf<String, String>()
            val demandMap = mutableMapOf<String, String>()
            val admittedMap = mutableMapOf<String, String>()
            val registrantMap = mutableMapOf<String, Map<String, String>>()
//            println("Unviversity : ${university.text}")
//            println("Prodi : ${prodi.text}")

//            for (tr in informasiUmum) {
//                if (informasiCounter == 6) {
//                    break
//                }
//                val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }
////                println(tdList[0].text.lowercase() + " " + tdList[1].text.lowercase())
//
//                when(tdList[0].text.lowercase()) {
//                    "kode" -> {
//                        majorCode = tdList[1].text
//                    }
//                    "nama" -> {
//                        majorName = tdList[1].text
//                    }
//                    "jenjang" -> {
//                        majorLevel = tdList[1].text
//                    }
//                    "jenis" -> {
//                        majorType = tdList[1].text
//                    }
//                    "daya tampung" -> {
//
//
//                        dataMap.put(majorCode.substring(0, 2), mutableMapOf(
//                            "ucid" to majorCode.substring(0, 2),
//                            "name" to university.text,
//                            majorCode to mutableMapOf(
//                                "mjid" to tdList[1].text,
//                                "name" to majorName,
//                                "level" to majorLevel,
//                                "type" to majorType,
//                            )
//                        ))
//                    }
//                    else -> {
//                        println("error")
//                    }
//                }
//
//                informasiCounter += 1
//            }

            for (tr in sebaranPeminat) {
                val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }

                if (counter == 1) {
                    val thList: List<WebElement> = tr.findElements(By.cssSelector("th")).map { it }
//                    println(thList[0].text.lowercase()
//                            + " " + thList[1].text.lowercase()
//                            + " " + thList[2].text.lowercase()
//                            + " " + thList[3].text.lowercase()
//                            + " " + thList[4].text.lowercase()
//                            + " " + thList[5].text.lowercase())
                    thList.forEachIndexed { index, webElement ->
                        if (index != 0 ){
                            yearMap[index.toString()] = webElement.text
                        }
                    }
                } else if (counter == 2) {
                    tdList.forEachIndexed { index, webElement ->
                        if (index != 0) {
                            demandMap[index.toString()] = webElement.text
                        }
                    }
                } else if (counter == 3) {
                    tdList.forEachIndexed { index, webElement ->
                        if (index != 0) {
                            admittedMap[index.toString()] = webElement.text
                        }
                    }
                } else if (counter > 4) {
//                    println(tdList[0].text.lowercase()
//                            + " " + tdList[1].text.lowercase()
//                            + " " + tdList[2].text.lowercase()
//                            + " " + tdList[3].text.lowercase()
//                            + " " + tdList[4].text.lowercase()
//                            + " " + tdList[5].text.lowercase())
                    var provinceCode = ""
                    var temporaryProvinceDemand = mutableMapOf<String, String>()
                    tdList.forEachIndexed { index, webElement ->
                        if (index == 0) {
                            provinceCode = provinceCodeMap[webElement.text].toString()
                        } else {
                            temporaryProvinceDemand.put(yearMap[index.toString()].toString(), webElement.text)
                        }
                    }
                    registrantMap.put(provinceCode, temporaryProvinceDemand)
                }

                counter += 1
            }

            println()
//            println(yearMap)
//            println(admittedMap)
            println(registrantMap)
            println()

        } catch(e: Exception) {
            print("errror $e")
        }
    }}
    driver.close()
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