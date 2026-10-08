// VIVA GUIDE: Retained fixed-slot regression checks; not the current same-day reservation suite. Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
import { execFileSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import assert from 'node:assert/strict';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const testDirectory = path.dirname(fileURLToPath(import.meta.url));
const repo = path.resolve(testDirectory, '../..');
const workspace = path.dirname(repo);
const java = process.env.JAVA_HOME ? path.join(process.env.JAVA_HOME, 'bin/java.exe') : path.join(workspace, 'OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6/jdk-17.0.15+6/bin/java.exe');
const cp = `${repo}/target/classes;${repo}/target/dependency/*`;
const run = randomBytes(5).toString('hex');
const staffEmail=`staff-${run}@integration.invalid`;
const emails=[`student-a-${run}@integration.invalid`,`student-b-${run}@integration.invalid`];
const password=randomBytes(20).toString('hex');
const env={...process.env,QUEUE_DB_CONFIG:`${repo}/config/db.local.properties`,TEST_STAFF_PASSWORD:password};
const fixture=(...args)=>execFileSync(java,['-cp',cp,`${testDirectory}/IntegrationFixtures.java`,...args],{env,encoding:'utf8'}).trim();
const serviceId=Number(fixture('create',`Integration ${run}`,staffEmail));
const base=process.env.QUEUE_FRONTEND_URL || 'http://127.0.0.1:5173';
// Named helper client: read its arguments and return value; callers determine whether it renders UI or performs an action.
function client() {
  let cookie='';
  return async (route,payload) => {
    const response=await fetch(base+route,{method:payload?'POST':'GET',headers:{Accept:'application/json','X-Requested-With':'XMLHttpRequest',Cookie:cookie},body:payload?new URLSearchParams(payload):undefined});
    for(const value of response.headers.getSetCookie())if(value.startsWith('JSESSIONID='))cookie=value.split(';')[0];
    return {status:response.status,body:await response.json()};
  };
}
const a=client(),b=client(),staff=client(),anonymous=client();
let checks=0;
// Named helper check: read its arguments and return value; callers determine whether it renders UI or performs an action.
function check(condition,label){assert.ok(condition,label);checks++;console.log(`PASS ${label}`);}
try {
  check((await anonymous('/api/student/overview')).status===401,'anonymous personal data rejected');
  const first=await a('/api/student/register',{studentName:'Test student A',email:emails[0],password});
  check(first.body.success,'registration creates a real student session');
  const studentId=first.body.data.studentId;
  const second=await b('/api/student/register',{studentName:'Test student B',email:emails[1],password});
  check(second.body.success,'second student registered');
  check((await a('/api/student/session')).body.data.studentId===studentId,'session persists across requests');
  check((await a('/api/student/queue/join',{serviceId})).body.success,'real queue join');
  check((await a('/api/student/queue/join',{serviceId})).status===409,'duplicate active ticket rejected');
  let overview=(await a('/api/student/overview')).body.data;
  let ticket=overview.tickets.find(t=>t.serviceId===serviceId);
  check(ticket.status==='WAITING'&&ticket.ahead===0,'first in line remains WAITING until staff calls');
  check(ticket.wait===0&&!('counterId' in ticket),'team wait calculation and no fabricated counter assignment');
  check((await b(`/api/queue/status?studentId=${studentId}&serviceId=${serviceId}`)).status===403,'legacy queue endpoint rejects another student identity');
  check((await staff('/api/staff/login',{email:staffEmail,password})).body.success,'team staff login');
  check((await staff('/api/queue/call-next',{})).body.success,'team staff calls next student');
  check((await a('/api/student/overview')).body.data.tickets.find(t=>t.serviceId===serviceId).status==='CALLED','student receives staff CALLED state');
  check((await staff('/api/queue/start',{queueId:ticket.queueId})).body.success,'team staff starts service');
  check((await a('/api/student/overview')).body.data.tickets.find(t=>t.serviceId===serviceId).status==='SERVING','student receives SERVING state');
  check((await staff('/api/queue/complete',{queueId:ticket.queueId})).body.success,'team staff completes service');
  check(!(await a('/api/student/overview')).body.data.tickets.some(t=>t.serviceId===serviceId),'completed ticket leaves active student queues');
  const tomorrow=new Date();tomorrow.setDate(tomorrow.getDate()+1);const date=tomorrow.toLocaleDateString('en-CA');
  const slots=(await a(`/api/student/bookings/slots?serviceId=${serviceId}&date=${date}`)).body.data;
  check(slots.includes('09:00:00')&&!slots.includes('13:00:00'),'team fixed slots used including lunch gap');
  const booking=await a('/api/student/bookings/create',{serviceId,bookingDate:date,bookingTime:slots[0]});
  check(booking.body.success,'real booking saved');
  const bookingId=booking.body.data.bookingId;
  check((await a('/api/student/bookings')).body.data.some(item=>item.id===bookingId),'booking loaded from MySQL');
  check((await b('/api/student/bookings/create',{serviceId,bookingDate:date,bookingTime:slots[0]})).status===409,'occupied slot rejected for second student');
  check((await b('/api/student/bookings/cancel',{bookingId})).status===409,'booking ownership enforced');
  check((await a('/api/student/bookings/create',{serviceId,bookingDate:date,bookingTime:'09:15:00'})).status===400,'unlisted slot rejected');
  check((await a('/api/student/bookings/cancel',{bookingId})).body.success,'owner cancels booking');
  check((await a(`/api/student/bookings/slots?serviceId=${serviceId}&date=${date}`)).body.data.includes(slots[0]),'cancelled slot released');
  check((await a('/api/student/bookings/reschedule',{})).status===404,'unsupported reschedule is not invented');
  check((await a('/api/student/logout',{})).body.success,'logout invalidates real session');
  check((await a('/api/student/overview')).status===401,'logged-out personal data rejected');
  check((await a('/api/student/login',{email:emails[0],password:'incorrect'})).status===401,'invalid password rejected');
  check((await a('/api/student/login',{email:emails[0],password})).body.success,'stored account password authenticates');
  console.log(`${checks} integration checks passed through Vite → Tomcat → MySQL.`);
} finally {console.log(fixture('cleanup',String(serviceId),...emails));}
