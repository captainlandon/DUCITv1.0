package com.ducit.data.local

import androidx.room.TypeConverter

/** Delimiter chosen to be vanishingly unlikely inside any stored string
 * field (a Unicode "unit separator" control character). */
private const val LIST_DELIMITER = ""

/**
 * Room type converters for the small set of non-primitive field shapes
 * used across the local entities: string lists (evidence refs, purpose
 * allowlists, used-for-plan-ids) and nullable longs.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        value?.joinToString(LIST_DELIMITER) ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split(LIST_DELIMITER)
}
