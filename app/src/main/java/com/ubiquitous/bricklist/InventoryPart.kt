package com.ubiquitous.bricklist

class InventoryPart {
    var id: Int = 0
    var inventoryID: Int = 0
    var typeID: Int = 0
    var itemID: Int = 0
    var quantityInSet: Int = 0
    var quantityInStore: Int = 0
    var colorID: Int = 0
    var extra: Int = 0

    constructor(
        id: Int,
        inventoryID: Int,
        typeID: Int,
        itemID: Int,
        quantityInSet: Int,
        colorID: Int,
        extra: Int
    ) {
        this.id = id
        this.inventoryID = inventoryID
        this.typeID = typeID
        this.itemID = itemID
        this.quantityInSet = quantityInSet
        this.colorID = colorID
        this.extra = extra
    }

    constructor(
        id: Int,
        inventoryID: Int,
        typeID: Int,
        itemID: Int,
        quantityInSet: Int,
        quantityInStore: Int,
        colorID: Int,
        extra: Int
    ) {
        this.id = id
        this.inventoryID = inventoryID
        this.typeID = typeID
        this.itemID = itemID
        this.quantityInSet = quantityInSet
        this.quantityInStore = quantityInStore
        this.colorID = colorID
        this.extra = extra
    }
}