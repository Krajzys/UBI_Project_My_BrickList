package com.ubiquitous.bricklist

class Part {
    var id: Int = 0
    var typeID: Int = 0
    var code: String = ""
    var name: String = ""
    var namePL: String = ""
    var categoryID: Int = 0

    constructor(id: Int, typeID: Int, code: String, name: String) {
        this.id = id
        this.typeID = typeID
        this.code = code
        this.name = name
    }

    constructor(id: Int, typeID: Int, code: String, name: String, namePL: String) {
        this.id = id
        this.typeID = typeID
        this.code = code
        this.name = name
        this.namePL = namePL
    }

    constructor(id: Int, typeID: Int, code: String, name: String, namePL: String, categoryID: Int) {
        this.id = id
        this.typeID = typeID
        this.code = code
        this.name = name
        this.namePL = namePL
        this.categoryID = categoryID
    }
}