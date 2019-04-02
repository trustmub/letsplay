package com.trustathanas.letsplay.models

import android.net.nsd.NsdServiceInfo
import android.os.Parcel
import android.os.Parcelable

class NsdServiceIInfoParcelable(private val serviceInfo: NsdServiceInfo) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readParcelable(NsdServiceInfo::class.java.classLoader) as NsdServiceInfo) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(serviceInfo, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NsdServiceIInfoParcelable> {
        override fun createFromParcel(parcel: Parcel): NsdServiceIInfoParcelable {
            return NsdServiceIInfoParcelable(parcel)
        }

        override fun newArray(size: Int): Array<NsdServiceIInfoParcelable?> {
            return arrayOfNulls(size)
        }
    }
}