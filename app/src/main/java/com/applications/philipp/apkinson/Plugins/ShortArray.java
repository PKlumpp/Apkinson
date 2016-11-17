package com.applications.philipp.apkinson.Plugins;

import android.os.Parcel;
import android.os.Parcelable;

public class ShortArray implements Parcelable {

	private short[] m_data;
	
	public ShortArray(short[] data) {
		m_data = data;
	}
	
	private ShortArray(Parcel in) {
		int[] tmpData = new int[in.readInt()];
		m_data = new short[tmpData.length];
		in.readIntArray(tmpData);
		for (int i = 0; i < tmpData.length; i++) {
			m_data[i] = (short)tmpData[i];
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int[] tmpData = new int[m_data.length];
		for (int i = 0; i < tmpData.length; i++) {
			tmpData[i] = m_data[i];
		}
		dest.writeInt(m_data.length);
		dest.writeIntArray(tmpData);
	}
	
	public static final Parcelable.Creator<ShortArray> CREATOR = new Parcelable.Creator<ShortArray>() {
		public ShortArray createFromParcel(Parcel in) {
			return new ShortArray(in);
		}
		public ShortArray[] newArray(int size) {
			return new ShortArray[size];
		}
	};
	
	public short[] getShortArray() {
		return m_data;
	}
}
