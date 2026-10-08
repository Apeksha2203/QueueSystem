// VIVA GUIDE: Campus-time policy and processing-window arithmetic. Study real/test time, open boundaries, next opening, lunch projection and interval overlap.
package com.queue.service;

import java.time.*;

/** Campus schedule in Asia/Kolkata. End boundaries are exclusive. */
public final class ServiceHours {
    public static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    /** Optional ignored local clock file beside the DB config; never bundled in the WAR. */
    // Reads real Asia/Kolkata time unless an explicitly configured ignored local test-clock file is enabled.
    public static LocalDateTime now() {
        LocalDateTime real=LocalDateTime.now(ZONE);
        String config=System.getProperty("queue.config",System.getenv("QUEUE_DB_CONFIG"));
        if(config==null)return real;
        java.nio.file.Path file=java.nio.file.Path.of(config).toAbsolutePath().resolveSibling("test-clock.local.properties");
        if(!java.nio.file.Files.isRegularFile(file))return real;
        java.util.Properties properties=new java.util.Properties();
        try(java.io.Reader reader=java.nio.file.Files.newBufferedReader(file)) {
            properties.load(reader);
            if(!"true".equalsIgnoreCase(properties.getProperty("enabled")))return real;
            return real.toLocalDate().atTime(LocalTime.parse(properties.getProperty("time")));
        }catch(Exception error){throw new IllegalStateException("Invalid local test clock configuration.",error);}
    }
    // Opening intervals are 10:00 <= time < 13:00 and 14:00 <= time < 15:00; lunch/closing boundaries are exclusive.
    public static boolean open(LocalDateTime at) {
        LocalTime t=at.toLocalTime();
        return (!t.isBefore(LocalTime.of(10,0)) && t.isBefore(LocalTime.of(13,0))) ||
               (!t.isBefore(LocalTime.of(14,0)) && t.isBefore(LocalTime.of(15,0)));
    }
    // Projects pre-opening to 10 AM, lunch to 2 PM and returns null after 3 PM for this day.
    public static LocalDateTime nextOpen(LocalDateTime at) {
        LocalTime t=at.toLocalTime();
        if(t.isBefore(LocalTime.of(10,0)))return at.toLocalDate().atTime(10,0);
        if(!t.isBefore(LocalTime.of(15,0)))return null;
        if(!t.isBefore(LocalTime.of(13,0)) && t.isBefore(LocalTime.of(14,0)))return at.toLocalDate().atTime(14,0);
        return at;
    }
    // Returns a projection only if it falls before the 3 PM closing boundary.
    public static LocalDateTime estimate(LocalDateTime at,long serviceMinutes) {
        LocalDateTime predicted=project(at,serviceMinutes);
        return predicted!=null && predicted.isBefore(at.toLocalDate().atTime(15,0))?predicted:null;
    }
    /** Preserve the projection beyond closing so the UI can explain the risk. */
    // Adds processing minutes from the next open time and skips lunch; preserves an over-closing projection for a warning.
    public static LocalDateTime project(LocalDateTime at,long serviceMinutes) {
        LocalDateTime start=nextOpen(at);
        if(start==null)return null;
        LocalDateTime predicted=start.plusMinutes(Math.max(0,serviceMinutes));
        LocalDateTime lunch=start.toLocalDate().atTime(13,0);
        if(start.isBefore(lunch) && !predicted.isBefore(lunch))predicted=predicted.plusHours(1);
        return predicted;
    }
    /** Count only scheduled processing windows, excluding pre-opening, lunch and overnight. */
    // Intersects start/end with each daily operating window, excluding pre-opening, lunch and overnight from processing duration.
    public static double processingMinutes(LocalDateTime start,LocalDateTime end) {
        if(start==null||end==null||!end.isAfter(start))return 0;
        long seconds=0;
        for(LocalDate day=start.toLocalDate();!day.isAfter(end.toLocalDate());day=day.plusDays(1)) {
            for(int[] range:new int[][]{{10,13},{14,15}}) {
                LocalDateTime a=day.atTime(range[0],0),b=day.atTime(range[1],0);
                if(start.isAfter(a))a=start;if(end.isBefore(b))b=end;
                if(b.isAfter(a))seconds+=Duration.between(a,b).getSeconds();
            }
        }
        return seconds/60.0;
    }}
