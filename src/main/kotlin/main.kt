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
//    generateDetailPoliteknikUrlList()
//    generateUniversityListDocument()
    generatePoliteknikListDocument()
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

fun emptyOrMinusConditionCheck(element: String): Boolean {
    return (element.trim() != "" && element.trim() != "-")
}

fun generateUniversityListDocument() {
    val driver: WebDriver = SafariDriver()

    var dataMap = mutableMapOf<String, Any?>()
    var currentPtn = ""
    val dataAggregate = mutableMapOf<String, Any?>()

    val urlPathName = "src/resources/university_url_list_soshum.txt"
//    val urlPathName = "src/resources/university_url_list_saintek.txt"

    var tesCounter = 1
    val gson = Gson()

    run lit@ {

        File(urlPathName).useLines { lines -> lines.forEach {
//            if (tesCounter == 2) {
//                return@lit
//            }

            try {
//                driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS)
                driver.get(it)
                val queryStringsMap = splitQuery(URL(it))

                val sebaranPeminat: List<WebElement> = driver.findElements(By.cssSelector(".table-condensed tr"))
                val informasiUmum: List<WebElement> = driver.findElements(By.cssSelector("td[valign=\"top\"] .panel-body .table tbody tr"))
                val university: WebElement = driver.findElement(By.cssSelector(".well .panel-title"))

                var counter = 1
                var informasiCounter = 0

                var majorCode = ""
                var majorName = ""
                var majorLevel = ""
                var majorType = ""

                val yearMap = mutableMapOf<String, String>()
                val demandMap = mutableMapOf<String, String>()
                val admittedMap = mutableMapOf<String, String>()
                val provinceMap = mutableMapOf<String, Map<String, Int>>()

                val registrantDemandMap = mutableMapOf<String, Any?>()

                if (currentPtn == "") {
                    currentPtn = queryStringsMap["ptn"]!!
                }else if (currentPtn != queryStringsMap["ptn"]) {
//                    println("Ptn second $currentPtn")
                    dataAggregate.put("ucid", currentPtn)
                    dataAggregate.put("name", university.text)

//                    println("Data Aggregate : $dataAggregate")
                    dataMap[currentPtn.trim()] = dataAggregate

//                    val fileName = "src/resources/saintek/${currentPtn}_saintek.json"
                    val fileName = "src/resources/soshum/${currentPtn}_soshum.json"
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

                    when(tdList[0].text.lowercase()) {
                        "kode" -> {
                            majorCode = tdList[1].text.trim()
                        }
                        "nama" -> {
                            majorName = tdList[1].text.trim()
                        }
                        "jenjang" -> {
                            majorLevel = tdList[1].text.trim()
                        }
                        "jenis" -> {
                            majorType = tdList[1].text.trim()
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
                                            val temporaryProvinceDemand: MutableMap<String, Int> = mutableMapOf()

                                            provinceMap[index.toString()].let { it ->
                                                if(!it.isNullOrEmpty()) {
                                                    temporaryProvinceDemand.putAll(provinceMap[index.toString()]!!)
                                                }
                                            }

                                            temporaryProvinceDemand[provinceCode] = (if (emptyOrMinusConditionCheck(webElement.text)) webElement.text.toInt() else 0)

                                            provinceMap[index.toString()] = temporaryProvinceDemand
                                        }
                                    }
                                }

                                counter += 1
                            }

                            for ((key, value) in yearMap) {
                                val formattedAdmitted = if (emptyOrMinusConditionCheck(admittedMap[key]!!)) arrayOf("") else admittedMap[key]?.split("(")?.toTypedArray()

                                registrantDemandMap.put(
                                    value, mutableMapOf(
                                        "total" to if (emptyOrMinusConditionCheck(demandMap[key]!!)) demandMap[key]?.toInt() else 0,
                                        "admitted" to if (emptyOrMinusConditionCheck(formattedAdmitted?.get(0)!!)) formattedAdmitted[0].toInt() else 0,
                                        "provinces" to provinceMap[key]
                                    )
                                )
                            }

                            dataAggregate.put(majorCode.trim(), mutableMapOf(
                                "mjid" to majorCode.trim(),
                                "name" to majorName.trim(),
                                "level" to majorLevel.trim(),
                                "type" to majorType.trim(),
                                "quota" to tdList[1].text.toInt(),
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
    }
    driver.close()
}

fun generatePoliteknikListDocument() {
    val driver: WebDriver = SafariDriver()

    var dataMap = mutableMapOf<String, Any?>()
    var currentPtn = ""
    val dataAggregate = mutableMapOf<String, Any?>()

//    val urlPathName = "src/resources/politeknik_url_list_soshum.txt"
    val urlPathName = "src/resources/politeknik_url_list_saintek.txt"

    var tesCounter = 1
    val gson = Gson()

    run lit@ {

        File(urlPathName).useLines { lines -> lines.forEach {
//            if (tesCounter == 2) {
//                return@lit
//            }

            try {
//                driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS)
                driver.get(it)
                val queryStringsMap = splitQuery(URL(it))

                val sebaranPeminat: List<WebElement> = driver.findElements(By.cssSelector(".table-condensed tr"))
                val informasiUmum: List<WebElement> = driver.findElements(By.cssSelector("td[valign=\"top\"] .panel-body .table tbody tr"))
                val university: WebElement = driver.findElement(By.cssSelector(".well .panel-title"))

                var counter = 1
                var informasiCounter = 0

                var majorCode = ""
                var majorName = ""
                var majorLevel = ""
                var majorType = ""

                val yearMap = mutableMapOf<String, String>()
                val demandMap = mutableMapOf<String, String>()
                val admittedMap = mutableMapOf<String, String>()
                val provinceMap = mutableMapOf<String, Map<String, Int>>()

                val registrantDemandMap = mutableMapOf<String, Any?>()

                if (currentPtn == "") {
                    currentPtn = queryStringsMap["ptn"]!!
                }else if (currentPtn != queryStringsMap["ptn"]) {
//                    println("Ptn second $currentPtn")
                    dataAggregate.put("ucid", currentPtn)
                    dataAggregate.put("name", university.text)

//                    println("Data Aggregate : $dataAggregate")
                    dataMap[currentPtn.trim()] = dataAggregate

//                    val fileName = "src/resources/saintek/${currentPtn}_saintek.json"
                    val fileName = "src/resources/soshum/${currentPtn}_soshum.json"
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

                    when(tdList[0].text.lowercase()) {
                        "kode" -> {
                            majorCode = tdList[1].text.trim()
                        }
                        "nama" -> {
                            majorName = tdList[1].text.trim()
                        }
                        "jenjang" -> {
                            majorLevel = tdList[1].text.trim()
                        }
                        "jenis" -> {
                            majorType = tdList[1].text.trim()
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
                                            val temporaryProvinceDemand: MutableMap<String, Int> = mutableMapOf()

                                            provinceMap[index.toString()].let { it ->
                                                if(!it.isNullOrEmpty()) {
                                                    temporaryProvinceDemand.putAll(provinceMap[index.toString()]!!)
                                                }
                                            }

                                            temporaryProvinceDemand[provinceCode] = (if (emptyOrMinusConditionCheck(webElement.text)) webElement.text.toInt() else 0)

                                            provinceMap[index.toString()] = temporaryProvinceDemand
                                        }
                                    }
                                }

                                counter += 1
                            }

                            for ((key, value) in yearMap) {
                                val formattedAdmitted = if (emptyOrMinusConditionCheck(admittedMap[key]!!)) arrayOf("") else admittedMap[key]?.split("(")?.toTypedArray()

                                registrantDemandMap.put(
                                    value, mutableMapOf(
                                        "total" to if (emptyOrMinusConditionCheck(demandMap[key]!!)) demandMap[key]?.toInt() else 0,
                                        "admitted" to if (emptyOrMinusConditionCheck(formattedAdmitted?.get(0)!!)) formattedAdmitted[0].toInt() else 0,
                                        "provinces" to provinceMap[key]
                                    )
                                )
                            }

                            dataAggregate.put(majorCode.trim(), mutableMapOf(
                                "mjid" to majorCode.trim(),
                                "name" to majorName.trim(),
                                "level" to majorLevel.trim(),
                                "type" to majorType.trim(),
                                "quota" to tdList[1].text.toInt(),
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
    }
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

fun generateDetailPoliteknikUrlList() {
    val driver: WebDriver = SafariDriver()
//    val fileName = "src/resources/politeknik_url_list_saintek.txt"
    val fileName = "src/resources/politeknik_url_list_soshum.txt"
    val file = File(fileName)
    val urlList = ArrayList<String>()

    File("src/resources/politeknik_url_list.txt").useLines { lines -> lines.forEach {
        try {
//            driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS)
            driver.get(it)

//            val jenis = "#jenis1"
            val jenis = "#jenis2"
            val tableContents: List<WebElement> = driver.findElements(By.cssSelector("$jenis tbody tr"))
            for (tr in tableContents) {
                val tdList: List<WebElement> = tr.findElements(By.cssSelector("td")).map { it }
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