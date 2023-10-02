import com.thinking.machine.pojo.*;
import java.util.*;
public class Test
{
	public static void main(String[] args)
	{
		Course course = new Course();
		Student student = new Student();
		DataManager dm = new DataManager();
		student.setFirstName("Juhi");
		student.setLastName("Balwani");
		student.setRollNumber(38);
		student.setCourseId(1);
		student.setDateOfBirth(new java.util.Date(1993-1900,02,17));
		student.setAadharCardNumber("XYZ12346");
		student.setGender("F");
		StudentView stview = new StudentView();
		dm.start();	
		course.setTitle("Computer Networks");
		
		try
		{
			/*dm.save(student);*/
			//dm.save(course);
			//dm.update(student);
			//dm.delete(Student.class,38);
			//dm.update(stview);
			dm.init();
			//dm.save(course);
			//dm.save(stview);
			//dm.delete(StudentView.class,38);
			//List<Course> list = dm.query(Course.class).orderBy("code").fire();
			List<Course>courses = (List<Course>)dm.query(Course.class).fire();
			System.out.println("Size : "+courses.size());
			/*dm.delete(Course.class,1);
			courses = dm.query(Course.class).fire();
			for(int i = 0; i<courses.size(); i++)
			{
				System.out.println(courses.get(i).getTitle());
			}*/
			course.setTitle("Computer Networks");
			course.setCode(5);
			dm.update(course);
			course.setTitle("Data Structures");
			dm.save(course);
			courses = dm.query(Course.class).fire();
			for(int i = 0; i<courses.size(); i++)
			{
				System.out.println(courses.get(i).getTitle());
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}