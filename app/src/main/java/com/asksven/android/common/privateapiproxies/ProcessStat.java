/*
 * Copyright (C) 2011 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asksven.android.common.privateapiproxies;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.asksven.android.common.dto.ProcessDto;
import com.asksven.android.common.nameutils.UidNameResolver;

//import android.content.Context;
//import android.content.pm.PackageManager;

/**
 * @author sven
 */
public class ProcessStat extends StatElement implements Comparable<ProcessStat>, Serializable {
    /**
     * the tag for logging
     */
    private transient static final String TAG = "ProcessStat";

    /**
     * the name of the process
     */
    @SerializedName("name")
    private String m_name;

    /**
     * the system time in ms
     */
    @SerializedName("system_time")
    private long m_systemTime;

    /**
     * the user time in ms
     */
    @SerializedName("user_time")
    private long m_userTime;

    /**
     * the number of starts
     */
    @SerializedName("starts")
    private int m_starts;

    /**
     * Constructor
     *
     * @param name
     * @param userTime
     * @param systemTime
     * @param starts
     */
    public ProcessStat(String name, long userTime, long systemTime, int starts) {

        m_name = name;
        m_userTime = userTime;
        m_systemTime = systemTime;
        m_starts = starts;
    }

    public ProcessStat(ProcessDto source) {

        this.setUid(source.m_uid);
        this.m_name = source.m_name;
        this.m_starts = source.m_starts;
        this.m_systemTime = source.m_systemTime;
        this.m_userTime = source.m_userTime;
        this.setTotal(source.m_total);
    }

    public ProcessDto toDto() {
        ProcessDto ret = new ProcessDto();
        ret.m_uid = this.getuid();
        ret.m_name = this.m_name;
        ret.m_starts = this.m_starts;
        ret.m_systemTime = this.m_systemTime;
        ret.m_userTime = this.m_userTime;

        return ret;
    }

    public ProcessStat clone() {
        ProcessStat clone = new ProcessStat(m_name, m_userTime, m_systemTime, m_starts);
        clone.setUid(getuid());
        clone.setUidInfo(getUidInfo());
        return clone;
    }

    /**
     * Substracts the values from a previous object
     * found in myList from the current ProcessStat
     * in order to obtain an object containing only the data since a referenc
     *
     * @param myList
     */
    public void substractFromRef(List<StatElement> myList) {
        if (myList != null) {
            for (int i = 0; i < myList.size(); i++) {
                try {
                    ProcessStat myRef = (ProcessStat) myList.get(i);
                    if ((this.getName().equals(myRef.getName())) && (this.getuid() == myRef.getuid())) {
                        this.m_userTime -= myRef.getUserTime();
                        this.m_systemTime -= myRef.getSystemTime();
                        this.m_starts -= myRef.getStarts();
                        if ((m_userTime < 0) || (m_systemTime < 0) || (m_starts < 0)) {
                            Log.e(TAG, "substractFromRef generated negative values (" + this.toString() + " - " + myRef.toString() + ")");
                        }
                        break;
                    }
                } catch (ClassCastException e) {
                    // just log as it is no error not to change the process
                    // being substracted from to do nothing
                    Log.e(TAG, "substractFromRef was called with a wrong list type");
                }

            }
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the system time
     */
    public long getSystemTime() {
        return m_systemTime;
    }

    /**
     * @return the user time
     */
    public long getUserTime() {
        return m_userTime;
    }

    /**
     * @return the number of starts
     */
    public int getStarts() {
        return m_starts;
    }

    /**
     * Compare a given WakelockStat with this object.
     * If the duration of this object is
     * greater than the received object,
     * then this object is greater than the other.
     */
    public int compareTo(ProcessStat o) {
        // we want to sort in descending order
        return ((int) ((o.getSystemTime() + o.getUserTime()) - (this.getSystemTime() + this.getUserTime())));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ProcessStat [m_name=" + m_name + ", m_systemTime=" + m_systemTime
                + ", m_userTime=" + m_userTime + ", m_starts=" + m_starts + "]";
    }

    /**
     * returns a string representation of the data
     */
    public String getData(long totalTime) {
        return "Uid: " + this.getuid() + " Sys: " + this.formatDuration(getSystemTime())
                + " Us: " + this.formatDuration(getUserTime())
                + " Starts: " + String.valueOf(getStarts());
    }

    /**
     * returns the values of the data
     */

    public double[] getValues() {
        double[] retVal = new double[2];
        retVal[0] = getUserTime();
        retVal[1] = getUserTime() + getSystemTime();
        return retVal;
    }

    public static class ProcessCountComparator implements Comparator<ProcessStat> {
        public int compare(ProcessStat a, ProcessStat b) {
            return ((int) (b.getStarts() - a.getStarts()));
        }
    }

    public static class ProcessTimeComparator implements Comparator<ProcessStat> {
        public int compare(ProcessStat a, ProcessStat b) {
            return ((int) ((b.getSystemTime() + b.getUserTime())
                    - (a.getSystemTime() + a.getUserTime())));
        }
    }

    public Drawable getIcon(UidNameResolver resolver) {
        if (m_icon == null) {
            // retrieve and store the icon for that package
            try {
                String myPackage = m_uidInfo.getNamePackage();
                m_icon = resolver.getIcon(myPackage);
            } catch (Exception e) {
                Log.e(TAG, "Icon for process could not be retrieved");
            }
        }
        return m_icon;
    }

    public String getPackageName() {
        if (m_uidInfo != null) {
            return m_uidInfo.getNamePackage();
        } else {
            return "";
        }
    }


}

