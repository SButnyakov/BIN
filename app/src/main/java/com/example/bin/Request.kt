package com.example.bin

import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Request : RealmObject {
    @PrimaryKey var bin: String? = null
    var scheme: String? = null
    var type: String? = null
    var brand: String? = null
    var prepaid: Boolean? = null
    var numberLength: String? = null
    var numberLuhn: Boolean? = null
    var countryName: String? = null
    var countryEmoji: String? = null
    var countryCurrency: String? = null
    var bankName: String? = null
    var bankUrl: String? = null
    var bankPhone: String? = null
    var bankCity: String? = null
}
