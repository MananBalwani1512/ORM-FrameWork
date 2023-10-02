package com.thinking.machine.pojo;
import annotations.*;
@View(name = "student_view")
public class StudentView
{
	public int rollNumber;
public void setRollNumber(int rollNumber)
{
	this.rollNumber = rollNumber;
}
public int getRollNumber()
{
	return this.rollNumber;
}
 public String firstName;
public void setFirstName(String firstName)
{
	this.firstName = firstName;
}
public String getFirstName()
{
	return this.firstName;
}
 public String lastName;
public void setLastName(String lastName)
{
	this.lastName = lastName;
}
public String getLastName()
{
	return this.lastName;
}
 public String aadharCardNumber;
public void setAadharCardNumber(String aadharCardNumber)
{
	this.aadharCardNumber = aadharCardNumber;
}
public String getAadharCardNumber()
{
	return this.aadharCardNumber;
}
	public int courseId;
public void setCourseId(int courseId)
{
	this.courseId = courseId;
}
public int getCourseId()
{
	return this.courseId;
}
 public String gender;
public void setGender(String gender)
{
	this.gender = gender;
}
public String getGender()
{
	return this.gender;
}
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
