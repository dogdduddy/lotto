package manager

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.LottoData
import model.LottoResult
import java.io.File
import java.io.InputStream

// 로또 데이터 매니저 클래스
class LottoDataManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    // 파일에서 LottoResult 리스트 읽기
    fun loadLottoResultsFromFile(filePath: String): List<LottoResult> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                println("파일이 존재하지 않습니다: $filePath")
                return emptyList()
            }
            val jsonString = file.readText()
            json.decodeFromString<List<LottoResult>>(jsonString)
        } catch (e: Exception) {
            println("LottoResult 파일 읽기 오류: ${e.message}")
            emptyList()
        }
    }

    // 파일에서 LottoData 리스트 읽기
    fun loadLottoDataFromFile(filePath: String): List<LottoData> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                println("파일이 존재하지 않습니다: $filePath")
                return emptyList()
            }
            val jsonString = file.readText()
            json.decodeFromString<List<LottoData>>(jsonString)
        } catch (e: Exception) {
            println("LottoData 파일 읽기 오류: ${e.message}")
            emptyList()
        }
    }

    // 리소스에서 LottoResult 리스트 읽기 (resources 폴더)
    fun loadLottoResultsFromResource(resourcePath: String): List<LottoResult> {
        return try {
            val inputStream: InputStream? = this::class.java.classLoader?.getResourceAsStream(resourcePath)
            if (inputStream == null) {
                println("리소스를 찾을 수 없습니다: $resourcePath")
                return emptyList()
            }
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<List<LottoResult>>(jsonString)
        } catch (e: Exception) {
            println("LottoResult 리소스 읽기 오류: ${e.message}")
            emptyList()
        }
    }

    // 리소스에서 LottoData 리스트 읽기 (resources 폴더)
    fun loadLottoDataFromResource(resourcePath: String): List<LottoData> {
        return try {
            val inputStream: InputStream? = this::class.java.classLoader?.getResourceAsStream(resourcePath)
            if (inputStream == null) {
                println("리소스를 찾을 수 없습니다: $resourcePath")
                return emptyList()
            }
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<List<LottoData>>(jsonString)
        } catch (e: Exception) {
            println("LottoData 리소스 읽기 오류: ${e.message}")
            emptyList()
        }
    }

    // 파일에 LottoResult 리스트 저장
    fun saveLottoResultsToFile(data: List<LottoResult>, filePath: String) {
        try {
            val jsonString = json.encodeToString(data)
            File(filePath).writeText(jsonString)
            println("LottoResults 저장 완료: $filePath")
        } catch (e: Exception) {
            println("파일 저장 오류: ${e.message}")
        }
    }

    // 파일에 LottoData 리스트 저장
    fun saveLottoDataToFile(data: List<LottoData>, filePath: String) {
        try {
            val jsonString = json.encodeToString(data)
            File(filePath).writeText(jsonString)
            println("LottoData 저장 완료: $filePath")
        } catch (e: Exception) {
            println("파일 저장 오류: ${e.message}")
        }
    }

    // JSON 문자열에서 직접 LottoResult 리스트 파싱
    fun parseLottoResultsFromJson(jsonString: String): List<LottoResult> {
        return try {
            json.decodeFromString<List<LottoResult>>(jsonString)
        } catch (e: Exception) {
            println("LottoResult JSON 파싱 오류: ${e.message}")
            emptyList()
        }
    }

    // JSON 문자열에서 직접 LottoData 리스트 파싱
    fun parseLottoDataFromJson(jsonString: String): List<LottoData> {
        return try {
            json.decodeFromString<List<LottoData>>(jsonString)
        } catch (e: Exception) {
            println("LottoData JSON 파싱 오료: ${e.message}")
            emptyList()
        }
    }
}