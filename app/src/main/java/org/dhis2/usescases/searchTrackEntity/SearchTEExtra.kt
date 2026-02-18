package org.dhis2.usescases.searchTrackEntity

import android.os.Bundle
import org.dhis2.commons.Constants
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

enum class SearchTEExtra(
    val key: String,
) {
    TEI_UID("TRACKED_ENTITY_UID"),
    PROGRAM_UID("PROGRAM_UID"),
    QUERY_ATTR("QUERY_DATA_ATTR"),
    QUERY_VALUES("QUERY_DATA_VALUES"),
}

@Suppress("UNCHECKED_CAST")
fun SearchTEActivity.queryDataExtra(savedInstanceState: Bundle?): Map<String, List<String>> {
    return when {
        savedInstanceState == null -> {
            val attributes =
                intent
                    .getStringArrayListExtra(SearchTEExtra.QUERY_ATTR.key)
                    ?.toList() ?: emptyList()
            val values =
                intent
                    .getStringArrayListExtra(SearchTEExtra.QUERY_VALUES.key)
                    ?.toList() ?: emptyList()
            if (attributes.size != values.size) return emptyMap()
            attributes
                .mapIndexed { index, attributeUid ->
                    attributeUid to values[index].split(",")
                }.toMap()
        }
        savedInstanceState.containsKey(Constants.QUERY_DATA) -> {
            val jsonString = savedInstanceState.getString(Constants.QUERY_DATA)
            if (jsonString != null) {
                parseMapFromJson(jsonString)
            } else {
                emptyMap()
            }
        }
        else -> {
            emptyMap()
        }
    }
}

private fun parseMapFromJson(jsonString: String): Map<String, List<String>> =
    try {
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, List<String>>()
        jsonObject.keys().forEach { key ->
            val jsonArray = jsonObject.getJSONArray(key)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            map[key] = list
        }
        map
    } catch (e: Exception) {
        emptyMap()
    }

fun mapToJsonString(queryData: Map<String, List<String>?>): String? =
    try {
        val jsonObject = JSONObject()
        queryData.forEach { (key, values) ->
            if (values != null && values.isNotEmpty()) {
                val jsonArray = JSONArray()
                values.forEach { value ->
                    jsonArray.put(value)
                }
                jsonObject.put(key, jsonArray)
            }
        }
        val jsonString = jsonObject.toString()
        Timber.d("SearchTEExtra: Guardando queryData como JSON: $jsonString")
        jsonString
    } catch (e: Exception) {
        Timber.e(e, "SearchTEExtra: Error al convertir Map a JSON")
        null
    }
