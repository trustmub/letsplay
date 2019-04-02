package com.trustathanas.letsplay.models

import android.os.Parcel
import android.os.Parcelable

class ConnectionModel constructor(val ipAddress: String, val port: Int): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ipAddress)
        parcel.writeInt(port)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConnectionModel> {
        override fun createFromParcel(parcel: Parcel): ConnectionModel {
            return ConnectionModel(parcel)
        }

        override fun newArray(size: Int): Array<ConnectionModel?> {
            return arrayOfNulls(size)
        }
    }
}