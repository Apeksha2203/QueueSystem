package com.queue.service;

import java.time.*;

/** Campus schedule in Asia/Kolkata. End boundaries are exclusive. */
public final class ServiceHours {
    public static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    /** Optional ignored local clock file beside the DB config; never bundled in the WAR. */
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
    public static boolean open(LocalDateTime at) {
        LocalTime t=at.toLocalTime();
        return (!t.isBefore(LocalTime.of(10,0)) && t.isBefore(LocalTime.of(13,0))) ||
               (!t.isBefore(LocalTime.of(14,0)) && t.isBefore(LocalTime.of(15,0)));
    }
    public static LocalDateTime nextOpen(LocalDateTime at) {
        LocalTime t=at.toLocalTime();
        if(t.isBefore(LocalTime.of(10,0)))return at.toLocalDate().atTime(10,0);
        if(!t.isBefore(LocalTime.of(15,0)))return null;
        if(!t.isBefore(LocalTime.of(13,0)) && t.isBefore(LocalTime.of(14,0)))return at.toLocalDate().atTime(14,0);
        return at;
    }
    public static LocalDateTime estimate(LocalDateTime at,long serviceMinutes) {
        LocalDateTime predicted=project(at,serviceMinutes);
        return predicted!=null && predicted.isBefore(at.toLocalDate().atTime(15,0))?predicted:null;
    }
    /** Preserve the projection beyond closing so the UI can explain the risk. */
    public static LocalDateTime project(LocalDateTime at,long serviceMinutes) {
        LocalDateTime start=nextOpen(at);
        if(start==null)return null;
        LocalDateTime predicted=start.plusMinutes(Math.max(0,serviceMinutes));
        LocalDateTime lunch=start.toLocalDate().atTime(13,0);
        if(start.isBefore(lunch) && !predicted.isBefore(lunch))predicted=predicted.plusHours(1);
        return predicted;
    }
    /** Count only scheduled processing windows, excluding pre-opening, lunch and overnight. */
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
