/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.google.android.exoplayer.multitv.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.google.android.exoplayer.multitv.C;
import com.google.android.exoplayer.multitv.ExoPlayerLibraryInfo;
import com.google.android.exoplayer.multitv.upstream.DataSource;
import com.google.android.exoplayer.multitv.upstream.DataSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Miscellaneous utility functions.
 */
public final class Util {

  /**
   * Like {@link android.os.Build.VERSION#SDK_INT}, but in a place where it can be conveniently
   * overridden for local testing.
   */
  public static final int SDK_INT =
      (Build.VERSION.SDK_INT == 23 && Build.VERSION.CODENAME.charAt(0) == 'N') ? 24
      : Build.VERSION.SDK_INT;

  /**
   * Like {@link Build#DEVICE}, but in a place where it can be conveniently overridden for local
   * testing.
   */
  public static final String DEVICE = Build.DEVICE;

  /**
   * Like {@link Build#MANUFACTURER}, but in a place where it can be conveniently overridden for
   * local testing.
   */
  public static final String MANUFACTURER = Build.MANUFACTURER;

  /**
   * Like {@link Build#MODEL}, but in a place where it can be conveniently overridden for local
   * testing.
   */
  public static final String MODEL = Build.MODEL;

  /**
   * Value returned by {@link #inferContentType(String)} for DASH manifests.
   */
  public static final int TYPE_DASH = 0;

  /**
   * Value returned by {@link #inferContentType(String)} for Smooth Streaming manifests.
   */
  public static final int TYPE_SS = 1;

  /**
   * Value returned by {@link #inferContentType(String)} for HLS manifests.
   */
  public static final int TYPE_HLS = 2;

  /**
   * Value returned by {@link #inferContentType(String)} for files other than DASH, HLS or Smooth
   * Streaming manifests.
   */
  public static final int TYPE_OTHER = 3;

  private static final String TAG = "Util";
  private static final Pattern XS_DATE_TIME_PATTERN = Pattern.compile(
      "(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)[Tt]"
      + "(\\d\\d):(\\d\\d):(\\d\\d)(\\.(\\d+))?"
      + "([Zz]|((\\+|\\-)(\\d\\d):(\\d\\d)))?");
  private static final Pattern XS_DURATION_PATTERN =
      Pattern.compile("^(-)?P(([0-9]*)Y)?(([0-9]*)M)?(([0-9]*)D)?"
          + "(T(([0-9]*)H)?(([0-9]*)M)?(([0-9.]*)S)?)?$");
  private static final Pattern ESCAPED_CHARACTER_PATTERN = Pattern.compile("%([A-Fa-f0-9]{2})");
  private static final long MAX_BYTES_TO_DRAIN = 2048;

  private Util() {}

  /**
   * Returns whether the device is an AndroidTV.
   *
   * @param context A context.
   * @return True if the device is an AndroidTV. False otherwise.
   */
  @SuppressLint("InlinedApi")
  public static boolean isAndroidTv(Context context) {
    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK);
  }

  /**
   * Converts the entirety of an {@link InputStream} to a byte array.
   *
   * @param inputStream the {@link InputStream} to be read. The input stream is not closed by this
   *    method.
   * @return a byte array containing all of the inputStream's bytes.
   * @throws IOException if an error occurs reading from the stream.
   */
  public static byte[] toByteArray(InputStream inputStream) throws IOException {
    byte[] buffer = new byte[1024 * 4];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, bytesRead);
    }
    return outputStream.toByteArray();
  }

  /**
   * Returns true if the URI is a path to a local file or a reference to a local file.
   *
   * @param uri The uri to test.
   */
  public static boolean isLocalFileUri(Uri uri) {
    String scheme = uri.getScheme();
    return TextUtils.isEmpty(scheme) || scheme.equals("file");
  }

  /**
   * Tests two objects for {@link Object#equals(Object)} equality, handling the case where one or
   * both may be null.
   *
   * @param o1 The first object.
   * @param o2 The second object.
   * @return {@code o1 == null ? o2 == null : o1.equals(o2)}.
   */
  public static boolean areEqual(Object o1, Object o2) {
    return o1 == null ? o2 == null : o1.equals(o2);
  }

  /**
   * Tests whether an {@code items} array contains an object equal to {@code item}, according to
   * {@link Object#equals(Object)}.
   * <p>
   * If {@code item} is null then true is returned if and only if {@code items} contains null.
   *
   * @param items The array of items to search.
   * @param item The item to search for.
   * @return True if the array contains an object equal to the item being searched for.
   */
  public static boolean contains(Object[] items, Object item) {
    for (int i = 0; i < items.length; i++) {
      if (Util.areEqual(items[i], item)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Instantiates a new single threaded executor whose thread has the specified name.
   *
   * @param threadName The name of the thread.
   * @return The executor.
   */
  public static ExecutorService newSingleThreadExecutor(final String threadName) {
    return Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, threadName);
      }
    });
  }

  /**
   * Instantiates a new single threaded scheduled executor whose thread has the specified name.
   *
   * @param threadName The name of the thread.
   * @return The executor.
   */
  public static ScheduledExecutorService newSingleThreadScheduledExecutor(final String threadName) {
    return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, threadName);
      }
    });
  }

  /**
   * Closes a {@link DataSource}, suppressing any {@link IOException} that may occur.
   *
   * @param dataSource The {@link DataSource} to close.
   */
  public static void closeQuietly(DataSource dataSource) {
    try {
      dataSource.close();
    } catch (IOException e) {
      // Ignore.
    }
  }

  /**
   * Closes an {@link OutputStream}, suppressing any {@link IOException} that may occur.
   *
   * @param outputStream The {@link OutputStream} to close.
   */
  public static void closeQuietly(OutputStream outputStream) {
    try {
      outputStream.close();
    } catch (IOException e) {
      // Ignore.
    }
  }

  /**
   * Converts text to lower case using {@link Locale#US}.
   *
   * @param text The text to convert.
   * @return The lower case text, or null if {@code text} is null.
   */
  public static String toLowerInvariant(String text) {
    return text == null ? null : text.toLowerCase(Locale.US);
  }

  /**
   * Divides a {@code numerator} by a {@code denominator}, returning the ceiled result.
   *
   * @param numerator The numerator to divide.
   * @param denominator The denominator to divide by.
   * @return The ceiled result of the division.
   */
  public static int ceilDivide(int numerator, int denominator) {
    return (numerator + denominator - 1) / denominator;
  }

  /**
   * Divides a {@code numerator} by a {@code denominator}, returning the ceiled result.
   *
   * @param numerator The numerator to divide.
   * @param denominator The denominator to divide by.
   * @return The ceiled result of the division.
   */
  public static long ceilDivide(long numerator, long denominator) {
    return (numerator + denominator - 1) / denominator;
  }

  /**
   * Returns the index of the largest value in an array that is less than (or optionally equal to)
   * a specified key.
   * <p>
   * The search is performed using a binary search algorithm, and so the array must be sorted.
   *
   * @param a The array to search.
   * @param key The key being searched for.
   * @param inclusive If the key is present in the array, whether to return the corresponding index.
   *     If false then the returned index corresponds to the largest value in the array that is
   *     strictly less than the key.
   * @param stayInBounds If true, then 0 will be returned in the case that the key is smaller than
   *     the smallest value in the array. If false then -1 will be returned.
   */
  public static int binarySearchFloor(long[] a, long key, boolean inclusive, boolean stayInBounds) {
    int index = Arrays.binarySearch(a, key);
    index = index < 0 ? -(index + 2) : (inclusive ? index : (index - 1));
    return stayInBounds ? Math.max(0, index) : index;
  }

  /**
   * Returns the index of the smallest value in an array that is greater than (or optionally equal
   * to) a specified key.
   * <p>
   * The search is performed using a binary search algorithm, and so the array must be sorted.
   *
   * @param a The array to search.
   * @param key The key being searched for.
   * @param inclusive If the key is present in the array, whether to return the corresponding index.
   *     If false then the returned index corresponds to the smallest value in the array that is
   *     strictly greater than the key.
   * @param stayInBounds If true, then {@code (a.length - 1)} will be returned in the case that the
   *     key is greater than the largest value in the array. If false then {@code a.length} will be
   *     returned.
   */
  public static int binarySearchCeil(long[] a, long key, boolean inclusive, boolean stayInBounds) {
    int index = Arrays.binarySearch(a, key);
    index = index < 0 ? ~index : (inclusive ? index : (index + 1));
    return stayInBounds ? Math.min(a.length - 1, index) : index;
  }

  /**
   * Returns the index of the largest value in an list that is less than (or optionally equal to)
   * a specified key.
   * <p>
   * The search is performed using a binary search algorithm, and so the list must be sorted.
   *
   * @param list The list to search.
   * @param key The key being searched for.
   * @param inclusive If the key is present in the list, whether to return the corresponding index.
   *     If false then the returned index corresponds to the largest value in the list that is
   *     strictly less than the key.
   * @param stayInBounds If true, then 0 will be returned in the case that the key is smaller than
   *     the smallest value in the list. If false then -1 will be returned.
   */
  public static<T> int binarySearchFloor(List<? extends Comparable<? super T>> list, T key,
      boolean inclusive, boolean stayInBounds) {
    int index = Collections.binarySearch(list, key);
    index = index < 0 ? -(index + 2) : (inclusive ? index : (index - 1));
    return stayInBounds ? Math.max(0, index) : index;
  }

  /**
   * Returns the index of the smallest value in an list that is greater than (or optionally equal
   * to) a specified key.
   * <p>
   * The search is performed using a binary search algorithm, and so the list must be sorted.
   *
   * @param list The list to search.
   * @param key The key being searched for.
   * @param inclusive If the key is present in the list, whether to return the corresponding index.
   *     If false then the returned index corresponds to the smallest value in the list that is
   *     strictly greater than the key.
   * @param stayInBounds If true, then {@code (list.size() - 1)} will be returned in the case that
   *     the key is greater than the largest value in the list. If false then {@code list.size()}
   *     will be returned.
   */
  public static<T> int binarySearchCeil(List<? extends Comparable<? super T>> list, T key,
      boolean inclusive, boolean stayInBounds) {
    int index = Collections.binarySearch(list, key);
    index = index < 0 ? ~index : (inclusive ? index : (index + 1));
    return stayInBounds ? Math.min(list.size() - 1, index) : index;
  }

  /**
   * Creates an integer array containing the integers from 0 to {@code length - 1}.
   *
   * @param length The length of the array.
   * @return The array.
   */
  public static int[] firstIntegersArray(int length) {
    int[] firstIntegers = new int[length];
    for (int i = 0; i < length; i++) {
      firstIntegers[i] = i;
    }
    return firstIntegers;
  }

  /**
   * Parses an xs:duration attribute value, returning the parsed duration in milliseconds.
   *
   * @param value The attribute value to parse.
   * @return The parsed duration in milliseconds.
   */
  public static long parseXsDuration(String value) {
    Matcher matcher = XS_DURATION_PATTERN.matcher(value);
    if (matcher.matches()) {
      boolean negated = !TextUtils.isEmpty(matcher.group(1));
      // Durations containing years and months aren't completely defined. We assume there are
      // 30.4368 days in a month, and 365.242 days in a year.
      String years = matcher.group(3);
      double durationSeconds = (years != null) ? Double.parseDouble(years) * 31556908 : 0;
      String months = matcher.group(5);
      durationSeconds += (months != null) ? Double.parseDouble(months) * 2629739 : 0;
      String days = matcher.group(7);
      durationSeconds += (days != null) ? Double.parseDouble(days) * 86400 : 0;
      String hours = matcher.group(10);
      durationSeconds += (hours != null) ? Double.parseDouble(hours) * 3600 : 0;
      String minutes = matcher.group(12);
      durationSeconds += (minutes != null) ? Double.parseDouble(minutes) * 60 : 0;
      String seconds = matcher.group(14);
      durationSeconds += (seconds != null) ? Double.parseDouble(seconds) : 0;
      long durationMillis = (long) (durationSeconds * 1000);
      return negated ? -durationMillis : durationMillis;
    } else {
      return (long) (Double.parseDouble(value) * 3600 * 1000);
    }
  }

  /**
   * Parses an xs:dateTime attribute value, returning the parsed timestamp in milliseconds since
   * the epoch.
   *
   * @param value The attribute value to parse.
   * @return The parsed timestamp in milliseconds since the epoch.
   */
  public static long parseXsDateTime(String value) throws ParseException {
    Matcher matcher = XS_DATE_TIME_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new ParseException("Invalid date/time format: " + value, 0);
    }

    int timezoneShift;
    if (matcher.group(9) == null) {
      // No time zone specified.
      timezoneShift = 0;
    } else if (matcher.group(9).equalsIgnoreCase("Z")) {
      timezoneShift = 0;
    } else {
      timezoneShift = ((Integer.parseInt(matcher.group(12)) * 60
          + Integer.parseInt(matcher.group(13))));
      if (matcher.group(11).equals("-")) {
        timezoneShift *= -1;
      }
    }

    Calendar dateTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    dateTime.clear();
    // Note: The month value is 0-based, hence the -1 on group(2)
    dateTime.set(Integer.parseInt(matcher.group(1)),
                 Integer.parseInt(matcher.group(2)) - 1,
                 Integer.parseInt(matcher.group(3)),
                 Integer.parseInt(matcher.group(4)),
                 Integer.parseInt(matcher.group(5)),
                 Integer.parseInt(matcher.group(6)));
    if (!TextUtils.isEmpty(matcher.group(8))) {
      final BigDecimal bd = new BigDecimal("0." + matcher.group(8));
      // we care only for milliseconds, so movePointRight(3)
      dateTime.set(Calendar.MILLISECOND, bd.movePointRight(3).intValue());
    }

    long time = dateTime.getTimeInMillis();
    if (timezoneShift != 0) {
      time -= timezoneShift * 60000;
    }

    return time;
  }

  /**
   * Scales a large timestamp.
   * <p>
   * Logically, scaling consists of a multiplication followed by a division. The actual operations
   * performed are designed to minimize the probability of overflow.
   *
   * @param timestamp The timestamp to scale.
   * @param multiplier The multiplier.
   * @param divisor The divisor.
   * @return The scaled timestamp.
   */
  public static long scaleLargeTimestamp(long timestamp, long multiplier, long divisor) {
    if (divisor >= multiplier && (divisor % multiplier) == 0) {
      long divisionFactor = divisor / multiplier;
      return timestamp / divisionFactor;
    } else if (divisor < multiplier && (multiplier % divisor) == 0) {
      long multiplicationFactor = multiplier / divisor;
      return timestamp * multiplicationFactor;
    } else {
      double multiplicationFactor = (double) multiplier / divisor;
      return (long) (timestamp * multiplicationFactor);
    }
  }

  /**
   * Applies {@link #scaleLargeTimestamp(long, long, long)} to a list of unscaled timestamps.
   *
   * @param timestamps The timestamps to scale.
   * @param multiplier The multiplier.
   * @param divisor The divisor.
   * @return The scaled timestamps.
   */
  public static long[] scaleLargeTimestamps(List<Long> timestamps, long multiplier, long divisor) {
    long[] scaledTimestamps = new long[timestamps.size()];
    if (divisor >= multiplier && (divisor % multiplier) == 0) {
      long divisionFactor = divisor / multiplier;
      for (int i = 0; i < scaledTimestamps.length; i++) {
        scaledTimestamps[i] = timestamps.get(i) / divisionFactor;
      }
    } else if (divisor < multiplier && (multiplier % divisor) == 0) {
      long multiplicationFactor = multiplier / divisor;
      for (int i = 0; i < scaledTimestamps.length; i++) {
        scaledTimestamps[i] = timestamps.get(i) * multiplicationFactor;
      }
    } else {
      double multiplicationFactor = (double) multiplier / divisor;
      for (int i = 0; i < scaledTimestamps.length; i++) {
        scaledTimestamps[i] = (long) (timestamps.get(i) * multiplicationFactor);
      }
    }
    return scaledTimestamps;
  }

  /**
   * Applies {@link #scaleLargeTimestamp(long, long, long)} to an array of unscaled timestamps.
   *
   * @param timestamps The timestamps to scale.
   * @param multiplier The multiplier.
   * @param divisor The divisor.
   */
  public static void scaleLargeTimestampsInPlace(long[] timestamps, long multiplier, long divisor) {
    if (divisor >= multiplier && (divisor % multiplier) == 0) {
      long divisionFactor = divisor / multiplier;
      for (int i = 0; i < timestamps.length; i++) {
        timestamps[i] /= divisionFactor;
      }
    } else if (divisor < multiplier && (multiplier % divisor) == 0) {
      long multiplicationFactor = multiplier / divisor;
      for (int i = 0; i < timestamps.length; i++) {
        timestamps[i] *= multiplicationFactor;
      }
    } else {
      double multiplicationFactor = (double) multiplier / divisor;
      for (int i = 0; i < timestamps.length; i++) {
        timestamps[i] = (long) (timestamps[i] * multiplicationFactor);
      }
    }
  }

  /**
   * Converts a list of integers to a primitive array.
   *
   * @param list A list of integers.
   * @return The list in array form, or null if the input list was null.
   */
  public static int[] toArray(List<Integer> list) {
    if (list == null) {
      return null;
    }
    int length = list.size();
    int[] intArray = new int[length];
    for (int i = 0; i < length; i++) {
      intArray[i] = list.get(i);
    }
    return intArray;
  }

  /**
   * On platform API levels 19 and 20, okhttp's implementation of {@link InputStream#close} can
   * block for a long time if the stream has a lot of data remaining. Call this method before
   * closing the input stream to make a best effort to cause the input stream to encounter an
   * unexpected end of input, working around this issue. On other platform API levels, the method
   * does nothing.
   *
   * @param connection The connection whose {@link InputStream} should be terminated.
   * @param bytesRemaining The number of bytes remaining to be read from the input stream if its
   *     length is known. {@link C#LENGTH_UNBOUNDED} otherwise.
   */
  public static void maybeTerminateInputStream(HttpURLConnection connection, long bytesRemaining) {
    if (SDK_INT != 19 && SDK_INT != 20) {
      return;
    }

    try {
      InputStream inputStream = connection.getInputStream();
      if (bytesRemaining == C.LENGTH_UNBOUNDED) {
        // If the input stream has already ended, do nothing. The socket may be re-used.
        if (inputStream.read() == -1) {
          return;
        }
      } else if (bytesRemaining <= MAX_BYTES_TO_DRAIN) {
        // There isn't much data left. Prefer to allow it to drain, which may allow the socket to be
        // re-used.
        return;
      }
      String className = inputStream.getClass().getName();
      if (className.equals("com.android.okhttp.internal.http.HttpTransport$ChunkedInputStream")
          || className.equals(
              "com.android.okhttp.internal.http.HttpTransport$FixedLengthInputStream")) {
        Class<?> superclass = inputStream.getClass().getSuperclass();
        Method unexpectedEndOfInput = superclass.getDeclaredMethod("unexpectedEndOfInput");
        unexpectedEndOfInput.setAccessible(true);
        unexpectedEndOfInput.invoke(inputStream);
      }
    } catch (IOException e) {
      // The connection didn't ever have an input stream, or it was closed already.
    } catch (Exception e) {
      // Something went wrong. The device probably isn't using okhttp.
    }
  }

  /**
   * Given a {@link DataSpec} and a number of bytes already loaded, returns a {@link DataSpec}
   * that represents the remainder of the data.
   *
   * @param dataSpec The original {@link DataSpec}.
   * @param bytesLoaded The number of bytes already loaded.
   * @return A {@link DataSpec} that represents the remainder of the data.
   */
  public static DataSpec getRemainderDataSpec(DataSpec dataSpec, int bytesLoaded) {
    if (bytesLoaded == 0) {
      return dataSpec;
    } else {
      long remainingLength = dataSpec.length == C.LENGTH_UNBOUNDED ? C.LENGTH_UNBOUNDED
          : dataSpec.length - bytesLoaded;
      return new DataSpec(dataSpec.uri, dataSpec.position + bytesLoaded, remainingLength,
          dataSpec.key, dataSpec.flags);
    }
  }

  /**
   * Returns the integer equal to the big-endian concatenation of the characters in {@code string}
   * as bytes. {@code string} must contain four or fewer characters.
   */
  public static int getIntegerCodeForString(String string) {
    int length = string.length();
    Assertions.checkArgument(length <= 4);
    int result = 0;
    for (int i = 0; i < length; i++) {
      result <<= 8;
      result |= string.charAt(i);
    }
    return result;
  }

  /**
   * Returns the top 32 bits of a long as an integer.
   */
  public static int getTopInt(long value) {
    return (int) (value >>> 32);
  }

  /**
   * Returns the bottom 32 bits of a long as an integer.
   */
  public static int getBottomInt(long value) {
    return (int) value;
  }

  /**
   * Returns a long created by concatenating the bits of two integers.
   */
  public static long getLong(int topInteger, int bottomInteger) {
    return ((long) topInteger << 32) | (bottomInteger & 0xFFFFFFFFL);
  }

  /**
   * Returns a hex string representation of the data provided.
   *
   * @param data The byte array containing the data to be turned into a hex string.
   * @param beginIndex The begin index, inclusive.
   * @param endIndex The end index, exclusive.
   * @return A string containing the hex representation of the data provided.
   */
  public static String getHexStringFromBytes(byte[] data, int beginIndex, int endIndex) {
    StringBuilder dataStringBuilder = new StringBuilder(endIndex - beginIndex);
    for (int i = beginIndex; i < endIndex; i++) {
      dataStringBuilder.append(String.format(Locale.US, "%02X", data[i]));
    }
    return dataStringBuilder.toString();
  }

  /**
   * Returns a byte array containing values parsed from the hex string provided.
   *
   * @param hexString The hex string to convert to bytes.
   * @return A byte array containing values parsed from the hex string provided.
   */
  public static byte[] getBytesFromHexString(String hexString) {
    byte[] data = new byte[hexString.length() / 2];
    for (int i = 0; i < data.length; i++) {
      int stringOffset = i * 2;
      data[i] = (byte) ((Character.digit(hexString.charAt(stringOffset), 16) << 4)
          + Character.digit(hexString.charAt(stringOffset + 1), 16));
    }
    return data;
  }

  /**
   * Returns a string with comma delimited simple names of each object's class.
   *
   * @param objects The objects whose simple class names should be comma delimited and returned.
   * @return A string with comma delimited simple names of each object's class.
   */
  public static <T> String getCommaDelimitedSimpleClassNames(T[] objects) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < objects.length; i++) {
      stringBuilder.append(objects[i].getClass().getSimpleName());
      if (i < objects.length - 1) {
        stringBuilder.append(", ");
      }
    }
    return stringBuilder.toString();
  }

  /**
   * Returns a user agent string based on the given application name and the library version.
   *
   * @param context A valid context of the calling application.
   * @param applicationName String that will be prefix'ed to the generated user agent.
   * @return A user agent string generated using the applicationName and the library version.
   */
  public static String getUserAgent(Context context, String applicationName) {
    String versionName;
    try {
      String packageName = context.getPackageName();
      PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
      versionName = info.versionName;
    } catch (NameNotFoundException e) {
      versionName = "?";
    }
    return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
        + ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
  }

  /**
   * Executes a post request using {@link HttpURLConnection}.
   *
   * @param url The request URL.
   * @param data The request body, or null.
   * @param requestProperties Request properties, or null.
   * @return The response body.
   * @throws IOException If an error occurred making the request.
   */
  // TODO: Remove this and use HttpDataSource once DataSpec supports inclusion of a POST body.
  public static byte[] executePost(String url, byte[] data, Map<String, String> requestProperties)
      throws IOException {
    HttpURLConnection urlConnection = null;
    try {
      urlConnection = (HttpURLConnection) new URL(url).openConnection();
      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(data != null);
      urlConnection.setDoInput(true);
      if (requestProperties != null) {
        for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
          urlConnection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
        }
      }
      // Write the request body, if there is one.
      if (data != null) {
        OutputStream out = urlConnection.getOutputStream();
        try {
          out.write(data);
        } finally {
          out.close();
        }
      }
      // Read and return the response body.
      InputStream inputStream = urlConnection.getInputStream();
      try {
        return toByteArray(inputStream);
      } finally {
        inputStream.close();
      }
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }

  /**
   * Converts a sample bit depth to a corresponding PCM encoding constant.
   *
   * @param bitDepth The bit depth. Supported values are 8, 16, 24 and 32.
   * @return The corresponding encoding. One of {@link C#ENCODING_PCM_8BIT},
   *     {@link C#ENCODING_PCM_16BIT}, {@link C#ENCODING_PCM_24BIT} and
   *     {@link C#ENCODING_PCM_32BIT}. If the bit depth is unsupported then
   *     {@link C#ENCODING_INVALID} is returned.
   */
  public static int getPcmEncoding(int bitDepth) {
    switch (bitDepth) {
      case 8:
        return C.ENCODING_PCM_8BIT;
      case 16:
        return C.ENCODING_PCM_16BIT;
      case 24:
        return C.ENCODING_PCM_24BIT;
      case 32:
        return C.ENCODING_PCM_32BIT;
      default:
        return C.ENCODING_INVALID;
    }
  }

  /**
   * Makes a best guess to infer the type from a file name.
   *
   * @param fileName Name of the file. It can include the path of the file.
   * @return One of {@link #TYPE_DASH}, {@link #TYPE_SS}, {@link #TYPE_HLS} or {@link #TYPE_OTHER}.
   */
  public static int inferContentType(String fileName) {
    if (fileName == null) {
      return TYPE_OTHER;
    } else if (fileName.endsWith(".mpd")) {
      return TYPE_DASH;
    } else if (fileName.endsWith(".ism")) {
      return TYPE_SS;
    } else if (fileName.endsWith(".m3u8")) {
      return TYPE_HLS;
    } else {
      return TYPE_OTHER;
    }
  }

  /**
   * Escapes a string so that it's safe for use as a file or directory name on at least FAT32
   * filesystems. FAT32 is the most restrictive of all filesystems still commonly used today.
   *
   * <p>For simplicity, this only handles common characters known to be illegal on FAT32:
   * &lt;, &gt;, :, ", /, \, |, ?, and *. % is also escaped since it is used as the escape
   * character. Escaping is performed in a consistent way so that no collisions occur and
   * {@link #unescapeFileName(String)} can be used to retrieve the original file name.
   *
   * @param fileName File name to be escaped.
   * @return An escaped file name which will be safe for use on at least FAT32 filesystems.
   */
  public static String escapeFileName(String fileName) {
    int length = fileName.length();
    int charactersToEscapeCount = 0;
    for (int i = 0; i < length; i++) {
      if (shouldEscapeCharacter(fileName.charAt(i))) {
        charactersToEscapeCount++;
      }
    }
    if (charactersToEscapeCount == 0) {
      return fileName;
    }

    int i = 0;
    StringBuilder builder = new StringBuilder(length + charactersToEscapeCount * 2);
    while (charactersToEscapeCount > 0) {
      char c = fileName.charAt(i++);
      if (shouldEscapeCharacter(c)) {
        builder.append('%').append(Integer.toHexString(c));
        charactersToEscapeCount--;
      } else {
        builder.append(c);
      }
    }
    if (i < length) {
      builder.append(fileName, i, length);
    }
    return builder.toString();
  }

  private static boolean shouldEscapeCharacter(char c) {
    switch (c) {
      case '<':
      case '>':
      case ':':
      case '"':
      case '/':
      case '\\':
      case '|':
      case '?':
      case '*':
      case '%':
        return true;
      default:
        return false;
    }
  }

  /**
   * Unescapes an escaped file or directory name back to its original value.
   *
   * <p>See {@link #escapeFileName(String)} for more information.
   *
   * @param fileName File name to be unescaped.
   * @return The original value of the file name before it was escaped,
   *    or null if the escaped fileName seems invalid.
   */
  public static String unescapeFileName(String fileName) {
    int length = fileName.length();
    int percentCharacterCount = 0;
    for (int i = 0; i < length; i++) {
      if (fileName.charAt(i) == '%') {
        percentCharacterCount++;
      }
    }
    if (percentCharacterCount == 0) {
      return fileName;
    }

    int expectedLength = length - percentCharacterCount * 2;
    StringBuilder builder = new StringBuilder(expectedLength);
    Matcher matcher = ESCAPED_CHARACTER_PATTERN.matcher(fileName);
    int endOfLastMatch = 0;
    while (percentCharacterCount > 0 && matcher.find()) {
      char unescapedCharacter = (char) Integer.parseInt(matcher.group(1), 16);
      builder.append(fileName, endOfLastMatch, matcher.start()).append(unescapedCharacter);
      endOfLastMatch = matcher.end();
      percentCharacterCount--;
    }
    if (endOfLastMatch < length) {
      builder.append(fileName, endOfLastMatch, length);
    }
    if (builder.length() != expectedLength) {
      return null;
    }
    return builder.toString();
  }

  /**
   * Returns the result of updating a CRC with the specified bytes in a "most significant bit first"
   * order.
   *
   * @param bytes Array containing the bytes to update the crc value with.
   * @param start The index to the first byte in the byte range to update the crc with.
   * @param end The index after the last byte in the byte range to update the crc with.
   * @param initialValue The initial value for the crc calculation.
   * @return The result of updating the initial value with the specified bytes.
   */
  public static int crc(byte[] bytes, int start, int end, int initialValue) {
    for (int i = start; i < end; i++) {
      initialValue = (initialValue << 8)
          ^ CRC32_BYTES_MSBF[((initialValue >>> 24) ^ (bytes[i] & 0xFF)) & 0xFF];
    }
    return initialValue;
  }

  /**
   * Gets the physical size of the default display, in pixels.
   *
   * @param context Any context.
   * @return The physical display size, in pixels.
   */
  public static Point getPhysicalDisplaySize(Context context) {
    // Before API 25 the platform Display object does not provide a working way to identify Android
    // TVs that can show 4k resolution in a SurfaceView, so check for supported devices here.
    if (Util.SDK_INT < 25) {
      if ("Sony".equals(Util.MANUFACTURER) && Util.MODEL != null && Util.MODEL.startsWith("BRAVIA")
          && context.getPackageManager().hasSystemFeature("com.sony.dtv.hardware.panel.qfhd")) {
        return new Point(3840, 2160);
      } else if ("NVIDIA".equals(Util.MANUFACTURER) && Util.MODEL != null
          && Util.MODEL.contains("SHIELD")) {
        // Attempt to read sys.display-size.
        String sysDisplaySize = null;
        try {
          Class<?> systemProperties = Class.forName("android.os.SystemProperties");
          Method getMethod = systemProperties.getMethod("get", String.class);
          sysDisplaySize = (String) getMethod.invoke(systemProperties, "sys.display-size");
        } catch (Exception e) {
          Log.e(TAG, "Failed to read sys.display-size", e);
        }
        // If we managed to read sys.display-size, attempt to parse it.
        if (!TextUtils.isEmpty(sysDisplaySize)) {
          try {
            String[] sysDisplaySizeParts = sysDisplaySize.trim().split("x");
            if (sysDisplaySizeParts.length == 2) {
              int width = Integer.parseInt(sysDisplaySizeParts[0]);
              int height = Integer.parseInt(sysDisplaySizeParts[1]);
              if (width > 0 && height > 0) {
                return new Point(width, height);
              }
            }
          } catch (NumberFormatException e) {
            // Do nothing.
          }
          Log.e(TAG, "Invalid sys.display-size: " + sysDisplaySize);
        }
      }
    }

    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    Point displaySize = new Point();
    if (Util.SDK_INT >= 23) {
      getDisplaySizeV23(display, displaySize);
    } else if (Util.SDK_INT >= 17) {
      getDisplaySizeV17(display, displaySize);
    } else if (Util.SDK_INT >= 16) {
      getDisplaySizeV16(display, displaySize);
    } else {
      getDisplaySizeV9(display, displaySize);
    }
    return displaySize;
  }

  @TargetApi(23)
  private static void getDisplaySizeV23(Display display, Point outSize) {
    Display.Mode mode = display.getMode();
    outSize.x = mode.getPhysicalWidth();
    outSize.y = mode.getPhysicalHeight();
  }

  @TargetApi(17)
  private static void getDisplaySizeV17(Display display, Point outSize) {
    display.getRealSize(outSize);
  }

  @TargetApi(16)
  private static void getDisplaySizeV16(Display display, Point outSize) {
    display.getSize(outSize);
  }

  @SuppressWarnings("deprecation")
  private static void getDisplaySizeV9(Display display, Point outSize) {
    outSize.x = display.getWidth();
    outSize.y = display.getHeight();
  }

  /**
   * Allows the CRC calculation to be done byte by byte instead of bit per bit being the order
   * "most significant bit first".
   */
  private static final int[] CRC32_BYTES_MSBF = {
      0X00000000, 0X04C11DB7, 0X09823B6E, 0X0D4326D9, 0X130476DC, 0X17C56B6B, 0X1A864DB2,
      0X1E475005, 0X2608EDB8, 0X22C9F00F, 0X2F8AD6D6, 0X2B4BCB61, 0X350C9B64, 0X31CD86D3,
      0X3C8EA00A, 0X384FBDBD, 0X4C11DB70, 0X48D0C6C7, 0X4593E01E, 0X4152FDA9, 0X5F15ADAC,
      0X5BD4B01B, 0X569796C2, 0X52568B75, 0X6A1936C8, 0X6ED82B7F, 0X639B0DA6, 0X675A1011,
      0X791D4014, 0X7DDC5DA3, 0X709F7B7A, 0X745E66CD, 0X9823B6E0, 0X9CE2AB57, 0X91A18D8E,
      0X95609039, 0X8B27C03C, 0X8FE6DD8B, 0X82A5FB52, 0X8664E6E5, 0XBE2B5B58, 0XBAEA46EF,
      0XB7A96036, 0XB3687D81, 0XAD2F2D84, 0XA9EE3033, 0XA4AD16EA, 0XA06C0B5D, 0XD4326D90,
      0XD0F37027, 0XDDB056FE, 0XD9714B49, 0XC7361B4C, 0XC3F706FB, 0XCEB42022, 0XCA753D95,
      0XF23A8028, 0XF6FB9D9F, 0XFBB8BB46, 0XFF79A6F1, 0XE13EF6F4, 0XE5FFEB43, 0XE8BCCD9A,
      0XEC7DD02D, 0X34867077, 0X30476DC0, 0X3D044B19, 0X39C556AE, 0X278206AB, 0X23431B1C,
      0X2E003DC5, 0X2AC12072, 0X128E9DCF, 0X164F8078, 0X1B0CA6A1, 0X1FCDBB16, 0X018AEB13,
      0X054BF6A4, 0X0808D07D, 0X0CC9CDCA, 0X7897AB07, 0X7C56B6B0, 0X71159069, 0X75D48DDE,
      0X6B93DDDB, 0X6F52C06C, 0X6211E6B5, 0X66D0FB02, 0X5E9F46BF, 0X5A5E5B08, 0X571D7DD1,
      0X53DC6066, 0X4D9B3063, 0X495A2DD4, 0X44190B0D, 0X40D816BA, 0XACA5C697, 0XA864DB20,
      0XA527FDF9, 0XA1E6E04E, 0XBFA1B04B, 0XBB60ADFC, 0XB6238B25, 0XB2E29692, 0X8AAD2B2F,
      0X8E6C3698, 0X832F1041, 0X87EE0DF6, 0X99A95DF3, 0X9D684044, 0X902B669D, 0X94EA7B2A,
      0XE0B41DE7, 0XE4750050, 0XE9362689, 0XEDF73B3E, 0XF3B06B3B, 0XF771768C, 0XFA325055,
      0XFEF34DE2, 0XC6BCF05F, 0XC27DEDE8, 0XCF3ECB31, 0XCBFFD686, 0XD5B88683, 0XD1799B34,
      0XDC3ABDED, 0XD8FBA05A, 0X690CE0EE, 0X6DCDFD59, 0X608EDB80, 0X644FC637, 0X7A089632,
      0X7EC98B85, 0X738AAD5C, 0X774BB0EB, 0X4F040D56, 0X4BC510E1, 0X46863638, 0X42472B8F,
      0X5C007B8A, 0X58C1663D, 0X558240E4, 0X51435D53, 0X251D3B9E, 0X21DC2629, 0X2C9F00F0,
      0X285E1D47, 0X36194D42, 0X32D850F5, 0X3F9B762C, 0X3B5A6B9B, 0X0315D626, 0X07D4CB91,
      0X0A97ED48, 0X0E56F0FF, 0X1011A0FA, 0X14D0BD4D, 0X19939B94, 0X1D528623, 0XF12F560E,
      0XF5EE4BB9, 0XF8AD6D60, 0XFC6C70D7, 0XE22B20D2, 0XE6EA3D65, 0XEBA91BBC, 0XEF68060B,
      0XD727BBB6, 0XD3E6A601, 0XDEA580D8, 0XDA649D6F, 0XC423CD6A, 0XC0E2D0DD, 0XCDA1F604,
      0XC960EBB3, 0XBD3E8D7E, 0XB9FF90C9, 0XB4BCB610, 0XB07DABA7, 0XAE3AFBA2, 0XAAFBE615,
      0XA7B8C0CC, 0XA379DD7B, 0X9B3660C6, 0X9FF77D71, 0X92B45BA8, 0X9675461F, 0X8832161A,
      0X8CF30BAD, 0X81B02D74, 0X857130C3, 0X5D8A9099, 0X594B8D2E, 0X5408ABF7, 0X50C9B640,
      0X4E8EE645, 0X4A4FFBF2, 0X470CDD2B, 0X43CDC09C, 0X7B827D21, 0X7F436096, 0X7200464F,
      0X76C15BF8, 0X68860BFD, 0X6C47164A, 0X61043093, 0X65C52D24, 0X119B4BE9, 0X155A565E,
      0X18197087, 0X1CD86D30, 0X029F3D35, 0X065E2082, 0X0B1D065B, 0X0FDC1BEC, 0X3793A651,
      0X3352BBE6, 0X3E119D3F, 0X3AD08088, 0X2497D08D, 0X2056CD3A, 0X2D15EBE3, 0X29D4F654,
      0XC5A92679, 0XC1683BCE, 0XCC2B1D17, 0XC8EA00A0, 0XD6AD50A5, 0XD26C4D12, 0XDF2F6BCB,
      0XDBEE767C, 0XE3A1CBC1, 0XE760D676, 0XEA23F0AF, 0XEEE2ED18, 0XF0A5BD1D, 0XF464A0AA,
      0XF9278673, 0XFDE69BC4, 0X89B8FD09, 0X8D79E0BE, 0X803AC667, 0X84FBDBD0, 0X9ABC8BD5,
      0X9E7D9662, 0X933EB0BB, 0X97FFAD0C, 0XAFB010B1, 0XAB710D06, 0XA6322BDF, 0XA2F33668,
      0XBCB4666D, 0XB8757BDA, 0XB5365D03, 0XB1F740B4
  };

}
