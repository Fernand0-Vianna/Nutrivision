package com.example.nutrivision.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordRepository(private val context: Context) {
    private val gson = Gson()

    /**
     * Pasta de dados. A partir desta versão fica dentro de [Context.getFilesDir]
     * para entrar no backup do Android. Instalações antigas tinham a pasta em
     * `/data/data/<pkg>/.relatorio`; os arquivos são migrados na primeira abertura.
     */
    private val relatorioDir: File by lazy {
        val current = File(context.filesDir, ".relatorio")
        val legacy = File(context.filesDir.parentFile, ".relatorio")
        if (current.exists() || !legacy.exists()) {
            current
        } else {
            val copied = runCatching {
                legacy.copyRecursively(current, overwrite = false)
                current.exists()
            }.getOrDefault(false)
            if (copied) current else legacy
        }
    }
    private val scansDir: File by lazy { File(context.filesDir, "scans") }

    private val recordsFile: File by lazy { File(relatorioDir, "records.json") }
    private val mediaFile: File by lazy { File(relatorioDir, "media_por_registro.json") }
    private val goalsFile: File by lazy { File(relatorioDir, "metas.json") }
    private val waterFile: File by lazy { File(relatorioDir, "agua.json") }
    private val weightsFile: File by lazy { File(relatorioDir, "peso.json") }
    private val userProfileFile: File by lazy { File(relatorioDir, "user_profile.json") }
    private val pinnedFile: File by lazy { File(relatorioDir, "pinned.json") }
    private val quarantinedFiles = mutableSetOf<String>()

    init {
        if (!relatorioDir.exists()) relatorioDir.mkdirs()
        if (!recordsFile.exists()) writeAtomic(recordsFile, "[]")
        if (!pinnedFile.exists()) writeAtomic(pinnedFile, "[]")
        if (!scansDir.exists()) scansDir.mkdirs()
    }

    /**
     * Grava primeiro em um arquivo temporário no mesmo diretório e só então
     * substitui o original. Evita deixar o JSON pela metade se o app for
     * interrompido no meio da escrita.
     */
    private fun writeAtomic(file: File, text: String) {
        val parent = file.parentFile ?: context.filesDir
        if (!parent.exists()) parent.mkdirs()
        val tmp = File(parent, "${file.name}.tmp")
        try {
            tmp.writeText(text)
            if (!tmp.renameTo(file)) {
                file.writeText(text)
                tmp.delete()
            }
        } catch (e: Exception) {
            tmp.delete()
            file.writeText(text)
        }
    }

    /** Guarda uma cópia do arquivo ilegível antes que ele seja sobrescrito. */
    @Synchronized
    private fun quarantine(file: File) {
        if (!quarantinedFiles.add(file.absolutePath)) return
        runCatching {
            if (file.exists() && file.length() > 2) {
                val backup = File(
                    file.parentFile,
                    "${file.nameWithoutExtension}.corrupt-${System.currentTimeMillis()}.json"
                )
                file.copyTo(backup, overwrite = true)
            }
        }
    }

    // ---------- Datas ----------

    private fun dayKey(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(timestamp))

    private fun dayLabel(timestamp: Long): String =
        SimpleDateFormat("dd/MM", Locale.ROOT).format(Date(timestamp))

    fun todayKey(): String = dayKey(System.currentTimeMillis())

    // ---------- Registros ----------

    fun saveRecord(record: FoodRecord): Double {
        val records = loadRecords().toMutableList()
        records.add(record)
        writeAtomic(recordsFile, gson.toJson(records))
        val avg = record.average()
        saveMedia(record.id, avg)
        return avg
    }

    fun deleteRecord(id: String) {
        val records = loadRecords().toMutableList()
        val removed = records.firstOrNull { it.id == id }
        records.removeAll { it.id == id }
        writeAtomic(recordsFile, gson.toJson(records))
        val media: MutableMap<String, Double> = loadMedia().toMutableMap()
        media.remove(id)
        writeAtomic(mediaFile, gson.toJson(media))
        removed?.imagePath?.let { path ->
            runCatching {
                val file = File(path)
                if (file.canonicalPath.startsWith(scansDir.canonicalPath)) file.delete()
            }
        }
    }

    fun loadRecords(): List<FoodRecord> {
        return try {
            val json = recordsFile.readText()
            val type = object : TypeToken<List<FoodRecord>>() {}.type
            gson.fromJson<List<FoodRecord>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            quarantine(recordsFile)
            emptyList()
        }
    }

    fun recordsOfToday(): List<FoodRecord> {
        val today = todayKey()
        return loadRecords().filter { dayKey(it.timestamp) == today }
    }

    /** Registros dos últimos [days] dias (incluindo hoje). Use 0 para todo o histórico. */
    fun recordsSince(days: Int): List<FoodRecord> {
        if (days <= 0) return loadRecords()
        val limit = System.currentTimeMillis() - days.toLong() * 24L * 60L * 60L * 1000L
        return loadRecords().filter { it.timestamp >= limit }
    }

    /**
     * Registros agrupados por dia, do mais recente para o mais antigo.
     * O par é (rótulo "dd/MM", registros do dia do mais recente para o mais antigo).
     */
    fun recordsByDay(records: List<FoodRecord> = loadRecords()): List<Pair<String, List<FoodRecord>>> {
        return records
            .groupBy { dayKey(it.timestamp) }
            .toList()
            .sortedByDescending { it.first }
            .map { (_, list) ->
                val stamp = list.minOfOrNull { it.timestamp } ?: System.currentTimeMillis()
                dayLabel(stamp) to list.sortedByDescending { it.timestamp }
            }
    }

    /** Quantidade de dias distintos que possuem pelo menos um registro. */
    fun daysWithRecords(records: List<FoodRecord>): Int =
        records.map { dayKey(it.timestamp) }.distinct().size

    // ---------- Pratos fixados ----------

    fun loadPinnedMeals(): List<PinnedMeal> {
        return try {
            if (!pinnedFile.exists()) return emptyList()
            val type = object : TypeToken<List<PinnedMeal>>() {}.type
            gson.fromJson<List<PinnedMeal>>(pinnedFile.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            quarantine(pinnedFile)
            emptyList()
        }
    }

    fun isPinned(name: String, calories: Double): Boolean =
        loadPinnedMeals().any { it.matches(name, calories) }

    /**
     * Fixa/desafixa um prato. Devolve `true` quando o prato passou a estar
     * fixado e `false` quando foi removido.
     */
    @Synchronized
    fun togglePinned(
        name: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double
    ): Boolean {
        val current = loadPinnedMeals().toMutableList()
        val existing = current.firstOrNull { it.matches(name, calories) }
        return if (existing != null) {
            current.remove(existing)
            writeAtomic(pinnedFile, gson.toJson(current))
            false
        } else {
            current.add(
                0,
                PinnedMeal(
                    name = name.trim().ifBlank { "Refeição" },
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            )
            writeAtomic(pinnedFile, gson.toJson(current))
            true
        }
    }

    fun deletePinnedMeal(id: String) {
        val current = loadPinnedMeals().filterNot { it.id == id }
        writeAtomic(pinnedFile, gson.toJson(current))
    }

    /** Adiciona um prato fixado ao diário de hoje e devolve o registro criado. */
    fun addPinnedToToday(id: String): FoodRecord? {
        val meal = loadPinnedMeals().firstOrNull { it.id == id } ?: return null
        val record = meal.toRecord()
        saveRecord(record)
        return record
    }

    // ---------- Médias ----------

    private fun saveMedia(recordId: String, average: Double) {
        val media: MutableMap<String, Double> = loadMedia().toMutableMap()
        media[recordId] = average
        writeAtomic(mediaFile, gson.toJson(media))
    }

    /** Média aritmética por registro, indexada pelo id do registro. */
    fun loadMedia(): Map<String, Double> {
        return try {
            val json = if (mediaFile.exists()) mediaFile.readText() else "{}"
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson<Map<String, Double>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** Média de um único registro (recalculada a partir dos macros + calorias). */
    fun mediaDoRegistro(record: FoodRecord): Double = record.average()

    /** Média das médias por registro do histórico inteiro. */
    fun mediaGeral(): Double {
        val records = loadRecords()
        if (records.isEmpty()) return 0.0
        return records.map { it.average() }.average()
    }

    // ---------- Agregados ----------

    fun stats(records: List<FoodRecord> = loadRecords()): DailyStats {
        if (records.isEmpty()) return DailyStats()
        val calories = records.sumOf { it.calories }
        val protein = records.sumOf { it.protein }
        val carbs = records.sumOf { it.carbs }
        val fat = records.sumOf { it.fat }
        val averages = records.map { it.average() }
        return DailyStats(
            recordCount = records.size,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            averagePerRecord = averages.average(),
            averageOfAverages = averages.average(),
            bestRecord = records.maxByOrNull { it.calories },
            mealBreakdown = records.groupingBy { it.mealName.ifEmpty { "Refeição" } }.eachCount()
        )
    }

    // ---------- Metas ----------

    fun loadGoals(): Goals {
        return try {
            if (!goalsFile.exists()) return Goals()
            gson.fromJson(goalsFile.readText(), Goals::class.java) ?: Goals()
        } catch (e: Exception) {
            Goals()
        }
    }

    fun saveGoals(goals: Goals) {
        writeAtomic(goalsFile, gson.toJson(goals))
    }

    // ---------- Água (um valor por dia) ----------

    private fun loadWaterMap(): Map<String, Double> {
        if (!waterFile.exists()) return emptyMap()
        val raw = runCatching { waterFile.readText().trim() }.getOrDefault("")
        if (raw.isEmpty()) return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson<Map<String, Double>>(raw, type) ?: emptyMap()
        } catch (e: Exception) {
            // Migração do formato antigo (um único número).
            raw.toDoubleOrNull()?.let { mapOf(todayKey() to it) } ?: emptyMap()
        }
    }

    private fun saveWaterMap(map: Map<String, Double>) {
        writeAtomic(waterFile, gson.toJson(map))
    }

    fun loadWaterMl(): Double = loadWaterMap()[todayKey()] ?: 0.0

    fun loadWaterMl(day: String): Double = loadWaterMap()[day] ?: 0.0

    fun addWaterMl(ml: Double): Double {
        val map = loadWaterMap().toMutableMap()
        val total = (map[todayKey()] ?: 0.0) + ml
        map[todayKey()] = total.coerceAtLeast(0.0)
        saveWaterMap(map)
        return map[todayKey()] ?: 0.0
    }

    fun resetWater() {
        val map = loadWaterMap().toMutableMap()
        map[todayKey()] = 0.0
        saveWaterMap(map)
    }

    // ---------- Peso ----------

    fun loadWeights(): List<WeightEntry> {
        return try {
            if (!weightsFile.exists()) return emptyList()
            val type = object : TypeToken<List<WeightEntry>>() {}.type
            gson.fromJson<List<WeightEntry>>(weightsFile.readText(), type)
                ?.sortedBy { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Salva o peso do dia (substitui o registro anterior do mesmo dia) e atualiza o perfil. */
    fun saveWeight(kg: Double): WeightEntry {
        val entry = WeightEntry(kg = kg.coerceIn(20.0, 500.0))
        val list = loadWeights()
            .filterNot { dayKey(it.timestamp) == dayKey(entry.timestamp) }
            .toMutableList()
        list.add(entry)
        list.sortBy { it.timestamp }
        writeAtomic(weightsFile, gson.toJson(list))

        // Mantém IMC e dados do perfil alinhados com o peso mais recente.
        val profile = loadUserProfile()
        if (kotlin.math.abs(profile.weightKg - entry.kg) > 0.05) {
            saveUserProfile(profile.copy(weightKg = entry.kg))
        }
        return entry
    }

    fun deleteWeight(timestamp: Long) {
        val list = loadWeights().filterNot { it.timestamp == timestamp }
        writeAtomic(weightsFile, gson.toJson(list))
    }

    fun currentWeight(): WeightEntry? = loadWeights().maxByOrNull { it.timestamp }

    // ---------- Perfil do usuário ----------

    fun loadUserProfile(): UserProfile {
        return try {
            if (!userProfileFile.exists()) return UserProfile()
            gson.fromJson(userProfileFile.readText(), UserProfile::class.java) ?: UserProfile()
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        val toSave = profile.copy(updatedAt = System.currentTimeMillis())
        writeAtomic(userProfileFile, gson.toJson(toSave))
    }

    fun hasCompletedProfile(): Boolean = loadUserProfile().completed && loadUserProfile().heightCm > 0.0

    // ---------- Fotos dos scans ----------

    /** Salva um bitmap como JPEG na pasta privada de scans e devolve o caminho. */
    fun saveScanImage(bitmap: Bitmap): String? {
        return runCatching {
            if (!scansDir.exists()) scansDir.mkdirs()
            val file = File(scansDir, "scan_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            file.absolutePath
        }.getOrNull()
    }

    /**
     * Importa uma imagem da galeria para a pasta privada (com downscale para
     * evitar fotos gigantes) e devolve o caminho local.
     */
    fun importScanImage(uri: Uri): String? {
        return runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            val maxSide = 1600
            while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) {
                sample *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null
            saveScanImage(bitmap)
        }.getOrNull()
    }

    // ---------- Exportação ----------

    /**
     * Gera um CSV (separador ";" para Excel pt-BR) com registros, médias,
     * metas, água e histórico de peso. O arquivo fica em cache/reports.
     */
    fun exportCsv(): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.ROOT).format(Date())
        val file = File(reportsDir, "nutrivision_relatorio_$stamp.csv")
        val br = Locale("pt", "BR")

        fun num(value: Double, decimals: Int = 1): String =
            String.format(br, "%.${decimals}f", value)

        fun esc(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""

        OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8).use { out ->
            // BOM para o Excel reconhecer UTF-8
            out.write("\uFEFF")
            val records = loadRecords()
            val goals = loadGoals()

            out.write("NUTRIVISION - RELATORIO\r\n")
            out.write("Gerado em;${esc(SimpleDateFormat("dd/MM/yyyy HH:mm", br).format(Date()))}\r\n\r\n")

            out.write("METAS DIARIAS\r\n")
            out.write("Calorias (kcal);${num(goals.calories, 0)}\r\n")
            out.write("Proteina (g);${num(goals.protein, 0)}\r\n")
            out.write("Carboidrato (g);${num(goals.carbs, 0)}\r\n")
            out.write("Gordura (g);${num(goals.fat, 0)}\r\n")
            out.write("Agua (ml);${num(goals.waterMl, 0)}\r\n\r\n")

            out.write("RESUMO\r\n")
            out.write("Registros;${records.size}\r\n")
            out.write("Media por registro;${num(mediaGeral())}\r\n")
            out.write("Agua hoje (ml);${num(loadWaterMl(), 0)}\r\n")
            out.write("Peso atual (kg);${currentWeight()?.let { num(it.kg) } ?: "-"}\r\n\r\n")

            out.write("REGISTROS\r\n")
            out.write("Data;Hora;Refeicao;Calorias (kcal);Proteina (g);Carboidrato (g);Gordura (g);Media;Origem;Foto\r\n")
            val dateFmt = SimpleDateFormat("dd/MM/yyyy", br)
            val timeFmt = SimpleDateFormat("HH:mm", br)
            records.sortedBy { it.timestamp }.forEach { r ->
                out.write(
                    listOf(
                        dateFmt.format(Date(r.timestamp)),
                        timeFmt.format(Date(r.timestamp)),
                        esc(r.mealName.ifEmpty { "Refeicao" }),
                        num(r.calories, 0),
                        num(r.protein, 0),
                        num(r.carbs, 0),
                        num(r.fat, 0),
                        num(r.average()),
                        esc(r.source),
                        esc(r.imagePath ?: "")
                    ).joinToString(";") + "\r\n"
                )
            }
            out.write("\r\n")

            out.write("AGUA POR DIA\r\n")
            out.write("Dia;Total (ml)\r\n")
            loadWaterMap().toSortedMap().forEach { (day, ml) ->
                out.write("$day;${num(ml, 0)}\r\n")
            }
            out.write("\r\n")

            out.write("EVOLUCAO DE PESO\r\n")
            out.write("Data;Peso (kg)\r\n")
            loadWeights().forEach { w ->
                out.write("${dateFmt.format(Date(w.timestamp))};${num(w.kg)}\r\n")
            }
        }
        return file
    }
}
