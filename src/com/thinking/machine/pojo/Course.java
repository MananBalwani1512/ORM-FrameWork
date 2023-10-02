package com.thinking.machine.pojo;
import annotations.*;
@Cacheable
@Table(name="course")
public class Course
{
@Column(name="code")
@AutoIncrement
@PrimaryKey
	public int code;
public void setCode(int code)
{
	this.code = code;
}
public int getCode()
{
	return this.code;
}
@Column(name="title")
 public String title;
public void setTitle(String title)
{
	this.title = title;
}
public String getTitle()
{
	return this.title;
}
}