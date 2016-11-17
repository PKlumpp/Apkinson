package com.applications.philipp.apkinson.Plugins;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class PluginOption implements Parcelable {

	private static final int FLAG_KEY = 1 << 0;
	private static final int FLAG_DESC = 1 << 1;
	private static final int FLAG_TYPE = 1 << 2;
	private static final int FLAG_MIN = 1 << 3;
	private static final int FLAG_MAX = 1 << 4;
	private static final int FLAG_VALS = 1 << 5;
	
	private String key;
	private String description;
	private Class<?> type;
	private int min;
	private int max;
	private List<String> values;
	private boolean hasMin;
	private boolean hasMax;
	private boolean hasValues;
	
	public PluginOption(String key, String description, Class<?> type) {
		this.key = key;
		this.description = description;
		this.type = type;
		min = 0;
		max = 0;
		values = null;
		hasMin = false;
		hasMax = false;
		hasValues = false;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public List<?> getValues() {
		return values;
	}
	
	public void setMin(int min) {
		this.min = min;
		hasMin = true;
	}
	
	public void clearMin() {
		this.min = 0;
		hasMin = false;
	}
	
	public void setMax(int max) {
		this.max = max;
		hasMax = true;
	}
	
	public void clearMax() {
		this.max = 0;
		hasMax = false;
	}
	
	public void addValue(String val) {
		if (values == null) {
			values = new ArrayList<String>();
		}
		values.add(val);
		hasValues = true;
	}
	
	public void clearValues() {
		values = null;
		hasValues = false;
	}
	
	public boolean hasMin() {
		return hasMin;
	}
	
	public boolean hasMax() {
		return hasMax;
	}
	
	public boolean hasValues() {
		return hasValues;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public String getTypeName() {
		return type.getName();
	}
	
	public boolean isIntegral() {
		if (type != null) {
			if (type.isPrimitive() && (type.getName().equals("int")
					|| type.getName().equals("short")
					|| type.getName().equals("long")
					|| type.getName().equals("byte")
					|| type.getName().equals("char")
					)) {
				return true;
			} else if (type.isPrimitive()) {
				return false;
			} else {
				if (type.equals(Integer.class)
						|| type.equals(Short.class)
						|| type.equals(Long.class)
						|| type.equals(Byte.class)
						|| type.equals(Character.class)
						) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isString() {
		return type != null && type.equals(String.class);
	}
	
	public PluginOption(Parcel in) {
		key = null;
		description = null;
		type = null;
		min = 0;
		max = 0;
		values = null;
		hasMin = false;
		hasMax = false;
		hasValues = false;
		int hasMap = in.readInt();
		if ((hasMap & FLAG_KEY) > 0) {
			key = in.readString();
		}
		if ((hasMap & FLAG_DESC) > 0) {
			description = in.readString();
		}
		if ((hasMap & FLAG_TYPE) > 0) {
			String name = in.readString();
			try {
				type = PluginOption.class.getClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				// this should really not happen?
			}
		}
		if ((hasMap & FLAG_MIN) > 0) {
			hasMin = true;
			min = in.readInt();
		}
		if ((hasMap & FLAG_MAX) > 0) {
			hasMax = true;
			max = in.readInt();
		}
		if ((hasMap & FLAG_VALS) > 0) {
			hasValues = true;
			values = new ArrayList<String>();
			in.readStringList(values);
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int hasMap = 0;
		if (this.key != null && this.key.length() > 0) {
			hasMap += FLAG_KEY;
		}
		if (this.description != null && this.description.length() > 0) {
			hasMap += FLAG_DESC;
		}
		if (this.type != null) {
			hasMap += FLAG_TYPE;
		}
		if (this.hasMin) {
			hasMap += FLAG_MIN;
		}
		if (this.hasMax) {
			hasMap += FLAG_MAX;
		}
		if (this.hasValues) {
			hasMap += FLAG_VALS;
		}
		dest.writeInt(hasMap);
		if (this.key != null && this.key.length() > 0) {
			dest.writeString(this.key);
		}
		if (this.description != null && this.description.length() > 0) {
			dest.writeString(this.description);
		}
		if (this.type != null) {
			dest.writeString(this.type.getName());
		}
		if (this.hasMin) {
			dest.writeInt(min);
		}
		if (this.hasMax) {
			dest.writeInt(max);
		}
		if (this.hasValues) {
			dest.writeStringList(values);
		}
	}
	
	public static final Creator<PluginOption> CREATOR = new Creator<PluginOption>() {
		public PluginOption createFromParcel(Parcel in) {
			return new PluginOption(in);
		}
		public PluginOption[] newArray(int size) {
			return new PluginOption[size];
		}
	};

}
