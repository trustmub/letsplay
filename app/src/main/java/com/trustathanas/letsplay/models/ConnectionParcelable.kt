package com.trustathanas.letsplay.models

import android.os.Parcel
import android.os.Parcelable

class ConnectionParcelable(val ipAddress: String, val port: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConnectionParcelable> {
        override fun createFromParcel(parcel: Parcel): ConnectionParcelable {
            return ConnectionParcelable(parcel)
        }

        override fun newArray(size: Int): Array<ConnectionParcelable?> {
            return arrayOfNulls(size)
        }
    }
}