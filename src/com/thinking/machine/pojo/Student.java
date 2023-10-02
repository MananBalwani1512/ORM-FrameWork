package com.thinking.machine.pojo;
import annotations.*;
@Table(name="student")
public class Student
{
@Column(name="roll_number")
@PrimaryKey
	public int rollNumber;
public void setRollNumber(int rollNumber)
{
	this.rollNumber = rollNumber;
}
public int getRollNumber()
{
	return this.rollNumber;
}
@Column(name="first_name")
 public String firstName;
public void setFirstName(String firstName)
{
	this.firstName = firstName;
}
public String getFirstName()
{
	return this.firstName;
}
@Column(name="last_name")
 public String lastName;
public void setLastName(String lastName)
{
	this.lastName = lastName;
}
public String getLastName()
{
	return this.lastName;
}
@Column(name="aadhar_card_number")
 public String aadharCardNumber;
public void setAadharCardNumber(String aadharCardNumber)
{
	this.aadharCardNumber = aadharCardNumber;
}
public String getAadharCardNumber()
{
	return this.aadharCardNumber;
}
@Column(name="course_id")
@ForeignKey(parent = "course",column="code")
	public int courseId;
public void setCourseId(int courseId)
{
	this.courseId = courseId;
}
public int getCourseId()
{
	return this.courseId;
}
@Column(name="gender")
 public String gender;
public void setGender(String gender)
{
	this.gender = gender;
}
public String getGender()
{
	return this.gender;
}
@Column(name="date_of_birth")
public java.util.Date dateOfBirth;
public void setDateOfBirth(java.util.Date dateOfBirth)
{
	this.dateOfBirth = dateOfBirth;
}
public java.util.Date getDateOfBirth()
{
	return this.dateOfBirth;
}
}