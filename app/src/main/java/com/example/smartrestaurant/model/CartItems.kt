package com.example.smartrestaurant.model

data class CartItems(
    var foodname:String ?= null,
    var foodPrice:String ?= null,
    var foodDescription:String ?= null,
    var foodImage:String ?= null,
    var foodQuantity:Int ?= null,
    var foodIngredients:String ?= null,
)
