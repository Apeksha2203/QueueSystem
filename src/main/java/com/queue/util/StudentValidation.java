// VIVA GUIDE: Registration syntax and length rules. Validates name, email, Indian phone and user-requested password policy; normalizes input.
package com.queue.util;
public final class StudentValidation {
 // Operation registration: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
 public static void registration(com.queue.model.Student s) {
  // Trim/validate email syntax and storage length before inserting a student record.
  String email=s.getEmail()==null?"":s.getEmail().trim();
  if(email.length()>100||!email.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+")||email.startsWith(".")||email.contains("..")||email.contains(".@"))throw new IllegalArgumentException("Enter a valid email address.");
  // Accept an Indian mobile number with optional +91; normalize stored phone format below.
  String phone=s.getPhone()==null?"":s.getPhone().trim();
  if(!phone.matches("(?:\\+91)?[6-9][0-9]{9}"))throw new IllegalArgumentException("Enter a valid 10-digit Indian mobile number, optionally prefixed with +91.");
  // Require 6-128 characters with a letter and digit; hashing happens in the DAO/password utility.
  String password=s.getPassword();
  if(password==null||password.length()<6||password.length()>128||password.isBlank()||!password.matches("(?s).*[A-Za-z].*")||!password.matches("(?s).*[0-9].*"))throw new IllegalArgumentException("Use 6 to 128 characters with at least one letter and one number.");
  // Reject blank/overlong names, then normalize accepted profile fields on the model.
  String name=s.getStudentName()==null?"":s.getStudentName().trim();
  if(name.isEmpty()||name.length()>100)throw new IllegalArgumentException("Enter your name (up to 100 characters).");
  s.setEmail(email);s.setStudentName(name);s.setPhone(phone.startsWith("+91")?phone:"+91"+phone);
 }
}
