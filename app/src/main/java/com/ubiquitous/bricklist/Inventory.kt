package com.ubiquitous.bricklist

class Inventory {
    var id: Int = 0
    var name: String = ""
    var active: Int = 0
    var lastAccessed: Int = 0

    constructor(id: Int, name: String, active: Int, lastAccessed: Int) {
        this.id = id
        this.name = name
        this.active = active
        this.lastAccessed = lastAccessed
    }
}