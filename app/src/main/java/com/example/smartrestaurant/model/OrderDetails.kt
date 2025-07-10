package com.example.smartrestaurant.model

import android.os.Parcel
import android.os.Parcelable

data class OrderDetails(
    var userId: String? = null,
    var userName: String? = null,
    var address: String? = null,
    var totalPrice: String? = null,
    var itemPushKey: String? = null,
    var phoneNumber: String? = null,
    var orderAccepted: Boolean = false,
    var paymentReceived: Boolean = false,
    var foodNames: MutableList<String>? = null,
    var foodImages: MutableList<String>? = null,
    var foodPrices: MutableList<String>? = null,
    var foodQuantities: MutableList<Int>? = null,
    var currentTime: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        userId = parcel.readString(),
        userName = parcel.readString(),
        address = parcel.readString(),
        totalPrice = parcel.readString(),
        itemPushKey = parcel.readString(),
        phoneNumber = parcel.readString(),
        orderAccepted = parcel.readByte() != 0.toByte(),
        paymentReceived = parcel.readByte() != 0.toByte(),
        foodNames = parcel.createStringArrayList(),
        foodImages = parcel.createStringArrayList(),
        foodPrices = parcel.createStringArrayList(),
        foodQuantities = parcel.readArrayList(Int::class.java.classLoader)?.map { it as Int }?.toMutableList(),
        currentTime = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(address)
        parcel.writeString(totalPrice)
        parcel.writeString(itemPushKey)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeStringList(foodNames)
        parcel.writeStringList(foodImages)
        parcel.writeStringList(foodPrices)
        parcel.writeList(foodQuantities)
        parcel.writeLong(currentTime)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails = OrderDetails(parcel)
        override fun newArray(size: Int): Array<OrderDetails?> = arrayOfNulls(size)
    }
}
