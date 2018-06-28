package com.kinetic.fit.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Saxton on 9/20/17.
 */

public class WorkoutParser {
    static final String TAG = "WorkoutParser";

    public static class WorkoutDefinition {
        public String version;
        public String units;
        public String title;
        public  String author;
        public    String description;
        public   String filename;
        public   ArrayList<Interval> intervals;
        public   ArrayList<Text> text;

        public static class Interval {
            public Interval(Double duration, Integer percentFTPStart, Integer percentFTPEnd, Integer cadenceStart, Integer cadenceEnd) {
                this.duration = duration;
                this.percentFTPStart = percentFTPStart;
                this.percentFTPEnd = percentFTPEnd;
                this.cadenceStart = cadenceStart;
                this.cadenceEnd = cadenceEnd;
            }

            public Double duration;
            public Integer percentFTPStart;
            public Integer percentFTPEnd;
            public Integer cadenceStart;
            public Integer cadenceEnd;
        }

        static class Text {
            public Text(Double offset, String message, Double duration) {
                this.offset = offset;
                this.message = message;
                this.duration = duration;
            }

            public Double offset;
            public String message;
            public Double duration;
        }
    }

    public static boolean canParse(Uri fileUri) {
        String[] s = fileUri.getLastPathSegment().split(Pattern.quote("."));
        String ext = s[s.length - 1];
        switch (ext) {
            case "mrc":
                return true;
            case "erg":
                return true;
            default:
                return false;
        }
    }

    public static WorkoutDefinition parse(Uri fileUri, @Nullable Integer riderFtp, Context context) throws Exception {
        if (!canParse(fileUri)) {
            throw new Error("Invalid File Extension");
        }
        ArrayList<String> lines = readTextFile(fileUri, context.getContentResolver());
        Cursor cursor = context.getContentResolver().query(fileUri, null, null, null, null);
        cursor.moveToFirst();
        String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        cursor.close();
        String title = fileUri.getLastPathSegment();
        Log.d(TAG, "title: " + title.split("\\.")[0]);
        Log.d(TAG, "title: " + title.split("\\.")[1]);
        String ext = title.split("\\.")[title.split("\\.").length - 1];
        return parseLines(ext, lines, title, fileName, riderFtp);
    }

    private static WorkoutDefinition parseLines(String ext, ArrayList<String> lines, @Nullable String title, @Nullable String fileName, @Nullable Integer riderFtp) throws Exception {
        WorkoutDefinition definition = new WorkoutDefinition();
        definition.title = title;
        definition.filename = fileName;
        definition.text = new ArrayList<>();
        definition.intervals = new ArrayList<>();

        boolean inHeader = false;
        boolean inData = false;
        boolean inText = false;
        String timeUnits = "minutes";
        boolean pctUnits = ext.equals("mrc"); // assume units are PERCENT for MRC and WATTS for ERG (but be open to change ....)
        Double ftp;
        String targetToUnits = "none";
        Double dataTimeStart = null;
        Integer dataPercentStart = null;
        Integer dataCadenceStart = -1;

        if (riderFtp == null) {
            ftp = 200.;
        } else {
            ftp = riderFtp.doubleValue();
        }

        for (String line : lines) {
            if (line.contains("[COURSE HEADER]")) {
                inHeader = true;
            } else if (line.contains("[END COURSE HEADER]")) {
                inHeader = false;
            } else if (line.contains("[COURSE DATA]")) {
                inData = true;
            } else if (line.contains("[END COURSE DATA]")) {
                inData = false;
            } else if (line.contains("[COURSE TEXT]")) {
                inText = true;
            } else if (line.contains("[END COURSE TEXT]")) {
                inText = false;
            } else if (inHeader) {
                List<String> components = new ArrayList<>(Arrays.asList(line.split("=")));
                if (components.size() >= 2) {
                    String key = components.remove(0).trim().toLowerCase();
                    String value = TextUtils.join("", components).trim();
                    if (key.equals("version")) {
                        definition.version = value;
                    } else if (key.equals("units")) {
                        definition.units = value;
                    } else if (key.equals("title")) {
                        definition.title = value;
                    } else if (key.equals("author")) {
                        definition.author = value;
                    } else if (key.equals("description")) {
                        definition.description = value;
                    } else if (key.equals("file name")) {
                        definition.filename = value;
                    } else if (key.equals("FTP")) {
                        Double val;
                        try {
                            val = Double.valueOf(value);
                        } catch (NumberFormatException e) {
                            val = 1.0;
                        }
                        ftp = Math.max(val, 1);
                    }
                } else if (components.size() == 1) {
                    components = new ArrayList<>(Arrays.asList(line.split(" ")));
                    if (components.size() >= 2) {
                        timeUnits = components.remove(0).trim().toLowerCase();
                        String targerUnit = components.remove(0).trim().toLowerCase();
                        if (targerUnit.equals("watts")) {
                            pctUnits = false;
                        } else if (targerUnit.equals("percent")) {
                            pctUnits = true;
                        }
                        if (components.size() > 0) {
                            targetToUnits = components.remove(0).trim().toLowerCase();
                        }
                    }
                }
            } else if (inData) {
                List<String> components = new ArrayList<>(Arrays.asList(line.split("\\t")));
                if (components.size() >= 2) {
                    if (dataTimeStart != null && dataPercentStart != null) {
                        Double dts = dataTimeStart;
                        Integer dps = dataPercentStart;
                        if (components.get(0) != null && components.get(1) != null) {
                            Double dte = Double.valueOf(components.get(0));
                            Double dped = Double.valueOf(components.get(1));
                            Integer dpe;
                            if (pctUnits) {
                                dpe = dped.intValue();
                            } else {
                                dpe = Double.valueOf(Math.round(dped / ftp * 100)).intValue();
                            }
                            Integer dce = -1;
                            if (components.size() >= 3) {
                                if (targetToUnits.equals("cadence")) {
                                    dce = components.get(2) != null ? Integer.valueOf(components.get(2)) : 0;
                                }
                            }
                            if (!dts.equals(dte)) {
                                Double duration;
                                if (timeUnits.equals("seconds")) {
                                    duration = dte - dts;
                                } else if (timeUnits.equals("hours")) {
                                    duration = (dte - dts) * 3600.;
                                } else {
                                    duration = (dte - dts) * 60.;
                                }
                                WorkoutDefinition.Interval interval = new WorkoutDefinition.Interval(duration, dps, dpe, dataCadenceStart, dce);
                                definition.intervals.add(interval);
                            }
                            dataTimeStart = dte;
                            dataPercentStart = dpe;
                            dataCadenceStart = dce;
                        }
                    } else {
                        dataTimeStart = Double.valueOf(components.get(0));
                        if (pctUnits) {
                            dataPercentStart = Integer.valueOf(components.get(1));
                        } else {
                            try {
                                Double watts = Double.valueOf(components.get(1));
                                dataPercentStart = Double.valueOf(Math.round(watts / ftp * 100)).intValue();
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                            }
                            if (components.size() >= 3) {
                                if (targetToUnits.equals("cadence")) {
                                    try {
                                        dataCadenceStart = Integer.valueOf(components.get(2));
                                    } catch (Exception e) {
                                        Crashlytics.logException(e);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (inText) {
                // Text MUST be separated by TABS
                List<String> components = new ArrayList<>(Arrays.asList(line.split("\t")));
                if (components.size() >= 2) {
                    String off = components.remove(0).trim();
                    String message = components.remove(0).trim();
                    String dur = components.size() > 0 ? components.get(0).trim() : "10";
                    Double offset = null;
                    Double duration = null;
                    try {
                        offset = Double.valueOf(off);
                        duration = Double.valueOf(dur);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                    if (offset != null && duration != null) {
                        Double offsetConverted;
                        switch (timeUnits) {
                            case "seconds":
                                offsetConverted = offset;
                                break;
                            case "hours":
                                offsetConverted = offset * 3600.0;
                                break;
                            default:
                                offsetConverted = offset * 60.0;
                                break;
                        }
                        WorkoutDefinition.Text t = new WorkoutDefinition.Text(offsetConverted, message, duration);
                        definition.text.add(t);
                    }
                }
            }
        }
        return definition;
    }

    private static ArrayList<String> readTextFile(Uri uri, ContentResolver cr) {
        BufferedReader reader = null;
        ArrayList<String> lines = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(cr.openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

}
