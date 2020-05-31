package com.ubiquitous.bricklist

class Color {
    var id: Int = 0
    var code: Int = 0
    var name: String = ""
    var namePL: String = ""

    constructor(id: Int, code: Int, name: String) {
        this.id = id
        this.code = code
        this.name = name
    }

    constructor(id: Int, code: Int, name: String, namePL: String) {
        this.id = id
        this.code = code
        this.name = name
        this.namePL = namePL
    }
}