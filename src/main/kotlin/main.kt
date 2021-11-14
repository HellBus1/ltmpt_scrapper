import com.google.gson.Gson
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.safari.SafariDriver
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


fun main() {
//    generateUniversityUrlList()
//    generatePoliteknikUrlList()
//    generateDetailUniverstyUrlList()
    generateUniversityListDocument()
}

fun splitQuery(url: URL): Map<String, String> {
    val query_pairs: MutableMap<String, String> = LinkedHashMap()
    val query: String = url.getQuery()
    val pairs = query.split("&".toRegex()).toTypedArray()
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        query_pairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
            URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
    }
    return query_pairs
}

fun generateUniversityListDocument() {
    val driver: WebDriver = SafariDriver()

    var dataMap = mutableMapOf<String, Any?>()
    var currentPtn = ""
    val dataAggregate = mutableMapOf<String, Any?>()

    var tesCounter = 1
    val gson = Gson()

//    run lit@ {

        File("src/resources/university_url_list_saintek.txt").useLines { lines -> lines.forEach {
//            if (tesCounter == 4) {
//                return@lit
//            }

            try {
//                driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS)
                driver.get(it)
                val queryStringsMap = splitQuery(URL(it))

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
//                val registrantMap = mutableMapOf<String, Map<String, String>>()
                val provinceMap = mutableMapOf<String, ArrayList<Map<String, Int>>>()

                val registrantDemandMap = mutableMapOf<String, Any?>()

                if (currentPtn == "") {
                    currentPtn = queryStringsMap["ptn"]!!
                }else if (currentPtn != queryStringsMap["ptn"]) {
//                    println("Ptn second $currentPtn")
                    dataAggregate.put("ucid", currentPtn)
                    dataAggregate.put("name", university.text)

//                    println("Data Aggregate : $dataAggregate")
                    dataMap[currentPtn] = dataAggregate

                    val fileName = "src/resources/saintek/${currentPtn}_saintek.json"
                    currentPtn = queryStringsMap["ptn"]!!

//                    println("Data Map : $dataMap")

//                    println(gson.toJson(dataMap).toString())
//                    println("Ptn third $currentPtn")

                    val file = File(fileName)
                    file.printWriter().use { out ->
                        out.println(gson.toJson(dataMap).toString())
                    }

                    dataAggregate.clear()
                    dataMap.clear()

                    tesCounter += 1
                }

                for (tr in informasiUmum) {
                    if (informasiCounter == 6) {
                        break
                    }
                    val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }
//                println(tdList[0].text.lowercase() + " " + tdList[1].text.lowercase())

                    when(tdList[0].text.lowercase()) {
                        "kode" -> {
                            majorCode = tdList[1].text
                        }
                        "nama" -> {
                            majorName = tdList[1].text
                        }
                        "jenjang" -> {
                            majorLevel = tdList[1].text
                        }
                        "jenis" -> {
                            majorType = tdList[1].text
                        }
                        "daya tampung" -> {
                            for (tr in sebaranPeminat) {
                                val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }

                                if (counter == 1) {
                                    val thList: List<WebElement> = tr.findElements(By.cssSelector("th")).map { it }
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
                                    // Get demand in every province
                                    var provinceCode = ""

                                    tdList.forEachIndexed { index, webElement ->
                                        if (index == 0) {
                                            provinceCode = provinceCodeMap[webElement.text].toString()
                                        } else {
                                            val temporaryProvinceDemand: ArrayList<Map<String, Int>> = arrayListOf()

                                            provinceMap[index.toString()].let { it ->
                                                if(!it.isNullOrEmpty()) {
                                                    temporaryProvinceDemand.addAll(provinceMap[index.toString()]!!)
//                                                    println("temporary : $temporaryProvinceDemand")
                                                }
                                            }

                                            temporaryProvinceDemand.add(mutableMapOf(provinceCode to if (webElement.text != "" && webElement.text != "-") webElement.text.toInt() else 0))

                                            provinceMap[index.toString()] = temporaryProvinceDemand
//                                            println("first : $provinceMap")
                                        }
                                    }
                                }

                                counter += 1
                            }

                            for ((key, value) in yearMap) {
                                registrantDemandMap.put(
                                    value, mutableMapOf(
                                        "total" to demandMap[key],
                                        "admitted" to admittedMap[key],
                                        "provinces" to provinceMap[key]
                                    )
                                )
                            }

                            dataAggregate.put(majorCode, mutableMapOf(
                                "mjid" to majorCode,
                                "name" to majorName,
                                "level" to majorLevel,
                                "type" to majorType,
                                "quota" to tdList[1].text,
                                "registrant" to registrantDemandMap
                            ))

                        }
                    }

                    informasiCounter += 1
                }

            } catch(e: Exception) {
                print("errror $e")
            }
        }}
//    }
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