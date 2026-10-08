// VIVA GUIDE: Explicit regression/fixture tool: inspect assertions and cleanup; database checks use their configured database. Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
import assert from 'node:assert/strict';
import { randomBytes } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const repo=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const java=path.join(path.dirname(repo),'OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6/jdk-17.0.15+6/bin/java.exe');
const password=randomBytes(20).toString('hex'),email=`visit-${randomBytes(8).toString('hex')}@test.invalid`,staffEmail=`staff-${email}`;
const env={...process.env,QUEUE_DB_CONFIG:path.join(repo,'config/db.local.properties'),TEST_STAFF_PASSWORD:password};
const fixture=(...args)=>execFileSync(java,['-cp',`${repo}/target/classes;${repo}/target/dependency/*`,`${repo}/scripts/tests/IntegrationFixtures.java`,...args],{env,encoding:'utf8'}).trim();
const serviceId=Number(fixture('create',`Visit API ${email}`,staffEmail));
const base=process.env.QUEUE_FRONTEND_URL||'http://127.0.0.1:5173';
// Named helper client: read its arguments and return value; callers determine whether it renders UI or performs an action.
function client(){let cookie='';return async(route,payload)=>{const r=await fetch(base+route,{method:payload?'POST':'GET',headers:{Cookie:cookie,'X-Requested-With':'XMLHttpRequest'},body:payload?new URLSearchParams(payload):undefined});for(const v of r.headers.getSetCookie())if(v.startsWith('JSESSIONID='))cookie=v.split(';')[0];return {status:r.status,body:await r.json()};};}
const student=client(),anonymous=client();let count=0;
// Arrow-function helper check: keeps this operation reusable at its call sites.
const check=(ok,label)=>{assert.ok(ok,label);count++;console.log(`PASS ${label}`);};
try {
  check((await anonymous(`/api/student/bookings/preview?serviceId=${serviceId}`)).status===401,'preview requires student authentication');
  check((await student('/api/student/register',{studentName:'Validation fixture',email,password,phone:'12345'})).status===400,'registration rejects invalid phone');
  check((await student('/api/student/register',{studentName:'Validation fixture',email:'bad@@email',password,phone:'9876543210'})).status===400,'registration rejects invalid email');
  check((await student('/api/student/register',{studentName:'Validation fixture',email,password:'abcdef',phone:'9876543210'})).status===400,'password must contain a number');
  check((await student('/api/student/register',{studentName:'Validation fixture',email,password:'123456',phone:'9876543210'})).status===400,'password must contain a letter');
  check((await student('/api/student/register',{studentName:'Validation fixture',email,password:'abc12',phone:'9876543210'})).status===400,'registration rejects short password');
  check((await student('/api/student/register',{studentName:'Visit API fixture',email,password,phone:'9876543210'})).body.success,'student registration creates session');
  const overview=(await student("/api/student/overview")).body.data;
  check(overview.services.every(s=>typeof s.averageServiceTime==="number"&&s.averageServiceTime>0),"service cards receive positive numeric backend processing averages");
  console.log("Service card durations:",overview.services.filter(s=>["General Inquiries","Fee Payment","Document Verification"].includes(s.serviceName)).map(s=>({service:s.serviceName,minutes:s.averageServiceTime})));
  const result=await student(`/api/student/bookings/preview?serviceId=${serviceId}`),preview=result.body.data;
  check(result.status===200&&preview.position===1&&preview.ahead===0,'live preview provides projected position and people ahead');
  check(preview.hours==='10:00–13:00 / 14:00–15:00','preview exposes agreed hours');
  check((await student(`/api/student/bookings/slots?serviceId=${serviceId}&date=${preview.date}`)).status===410,'slot endpoint retired');
  const tomorrow=new Date(`${preview.date}T12:00:00+05:30`);tomorrow.setUTCDate(tomorrow.getUTCDate()+1);
  check((await student('/api/student/bookings/create',{serviceId,bookingDate:tomorrow.toISOString().slice(0,10)})).status===400,'future-day reservation rejected');
  if(preview.canReserve) {
    const booked=await student('/api/student/bookings/create',{serviceId});check(booked.body.success,'reservation requires no time slot');
    check((await student('/api/student/bookings')).body.data.some(row=>row.id===booked.body.data.queueId),'reservation appears in visit history');
    check(!(await student('/api/student/bookings/create',{serviceId})).body.success,'second active reservation rejected');
    const contactOperator=client();await contactOperator('/api/staff/login',{email:staffEmail,password});
    const queue=(await contactOperator('/api/staff/queue')).body.data;
    const row=queue.find(q=>q.queueId===booked.body.data.queueId);
    check(row?.studentName==='Visit API fixture'&&row.studentEmail===email&&row.studentPhone==='+919876543210','assigned-service staff queue includes student identity and normalised phone');
    await contactOperator('/api/staff/logout',{});
    check((await student('/api/student/bookings/cancel',{bookingId:booked.body.data.queueId})).body.success,'waiting reservation can be cancelled');
  } else check(!(await student('/api/student/bookings/create',{serviceId})).body.success,'closed or full service rejects reservations');
  const operator=client();
  check((await operator('/api/staff/profile')).status===401,'anonymous staff profile rejected');
  check((await operator('/api/staff/login',{email:staffEmail,password})).body.success,'staff authentication creates server session');
  check((await operator('/api/staff/profile')).body.data.email===staffEmail,'staff profile restores authenticated identity');
  const metrics=(await operator('/api/staff/dashboard-summary')).body.data;
  check(metrics.servedToday===0&&metrics.averageWaitMinutes===null&&metrics.averageServiceMinutes===null,'empty service analytics excludes unrelated historical data');
  check((await anonymous('/api/analytics')).status===401,'historical analytics requires staff session');
  check((await operator('/api/analytics')).body.studentsServedToday===0,'analytics endpoint scopes to assigned service');
  check((await operator('/api/queue/skip',{queueId:1})).status===410,'skip endpoint retired');
  check((await operator('/api/staff/logout',{})).body.success,'staff logout succeeds');
  check((await operator('/api/staff/profile')).status===401,'staff logout invalidates server session');
  check((await student('/api/queue/no-show',{queueId:1})).status===401,'student cannot mark a missed turn');
  check((await student('/api/student/logout',{})).body.success,'logout invalidates session');
  check((await student('/api/student/bookings')).status===401,'logged-out visit data rejected');
  console.log(`${count} reservation API checks passed.`);
} finally {console.log(fixture('cleanup',String(serviceId),email));}
