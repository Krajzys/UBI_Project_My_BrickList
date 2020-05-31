package com.ubiquitous.bricklist

class ItemType {
    var id: Int = 0
    var code: String = ""
    var name: String = ""
    var namePL: String = ""

    constructor(id: Int, code: String, name: String) {
        this.id = id
        this.code = code
        this.name = name
    }

    constructor(id: Int, code: String, name: String, namePL: String) {
        this.id = id
        this.code = code
        this.name = name
        this.namePL = namePL
    }
}