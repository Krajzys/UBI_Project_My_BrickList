package com.ubiquitous.bricklist

class Code {
    var id: Int = 0
    var itemID: Int = 0
    var colorID: Int = 0
    var code: Int = 0
    var image: ByteArray? = null

    constructor(id: Int, itemID: Int, colorID: Int, code: Int) {
        this.id = id
        this.itemID = itemID
        this.colorID = colorID
        this.code = code
    }

    constructor(id: Int, itemID: Int, colorID: Int, code: Int, image: ByteArray?) {
        this.id = id
        this.itemID = itemID
        this.colorID = colorID
        this.code = code
        this.image = image
    }

}