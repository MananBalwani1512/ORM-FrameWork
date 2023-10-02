import java.sql.*;
import java.lang.reflect.*;
import java.util.*;
public class Query
{
	public String tableName;
	public Class className;
	public Connection connection;
	public int quantity;
	public String conditionColumn;
	public boolean condition;
	public boolean orderBy;
	public String orderByColumn;
	public boolean lt;
	public boolean gt;
	public boolean eq;
	public List result;
	private String query;
	public Query(Connection connection)
	{
		query = "select * from ";
		this.connection = connection;
	}
	public Query gt(int quantity)
	{
		this.gt = true;
		this.quantity = quantity;
		return this;
	}
	public Query lt(int quantity)
	{
		this.lt = true;
		this.quantity = quantity;
		return this;
	}
	public Query eq(int quantity)
	{
		this.eq = true;
		this.quantity = quantity;
		return this;
	}
	public Query where(String conditionColumn)
	{
		this.condition = true;
		this.conditionColumn = conditionColumn;
		return this;
	}
	public Query orderBy(String columnName)
	{
		this.orderBy = true;
		this.orderByColumn = columnName;
		return this;
	}
	public List fire()
	{
		if(this.result != null)
			return result;
		String query = this.query;
		query = query+this.tableName;
		if(this.condition)
		{
			query = query+" WHERE "+this.conditionColumn;
			if(this.lt)
			{
				query = query+" < ?";
			}
			else if(this.gt)
			{
				query = query+" > ?";
			}
			else
			{
				query = query+" = ?";
			}	
		}
		else if(this.orderBy == true)
		{
			query = query+" order by "+this.orderByColumn;
		}
		List result = new ArrayList();
		ResultSet resultSet = null;
		try
		{
			PreparedStatement pstatement = this.connection.prepareStatement(query);
			if(condition)
				pstatement.setInt(1,quantity);
			resultSet = pstatement.executeQuery();
			Object obj = className.newInstance();
			while(resultSet.next())
			{
				obj = className.newInstance();
				Field[] fields = className.getFields();
				for(int i = 0; i<fields.length; i++)
				{
					Field field = fields[i];
					String fieldName = field.getName();
					fieldName = Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1);
					String methodName = "set"+fieldName;
					Method method = className.getDeclaredMethod(methodName,field.getType());
					method.invoke(obj,resultSet.getObject(i+1));
				}
				result.add(obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}