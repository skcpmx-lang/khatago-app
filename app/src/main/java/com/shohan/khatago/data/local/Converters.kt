package com.shohan.khatago.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toFrequency(value: String?): Frequency? = value?.let(Frequency::valueOf)

    @TypeConverter
    fun fromFrequency(value: Frequency?): String? = value?.name

    @TypeConverter
    fun toPersonalDirection(value: String?): PersonalDirection? = value?.let(PersonalDirection::valueOf)

    @TypeConverter
    fun fromPersonalDirection(value: PersonalDirection?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let(TransactionType::valueOf)

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toCashEffect(value: String?): TransactionCashEffect? = value?.let(TransactionCashEffect::valueOf)

    @TypeConverter
    fun fromCashEffect(value: TransactionCashEffect?): String? = value?.name
}
