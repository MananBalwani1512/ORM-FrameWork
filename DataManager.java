import java.sql.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.google.gson.*;
import annotations.*;
public class DataManager
{
	private Connection connection;
	private Configuration config;
	private static HashMap<Class,SQLMaker> sqlData;
	private static HashSet<Class> sqlViews;
	private static HashMap<Class, List>cacheableData;
	public DataManager()
	{
		try
		{
			sqlData = new HashMap();
			sqlViews = new HashSet();
			start();
			/*DataBase ki saari tables ke naam lo aur unki class package se nikalo aur firr unpe operations perform karlo*/
			DatabaseMetaData dbmetadata = connection.getMetaData();
			ResultSet rs = dbmetadata.getTables(config.getDatabaseName(),null,null, new String[] {"TABLE"});
			String packageName = config.getPackageName();
			while(rs.next())
			{
				SQLMaker sqlMaker = new SQLMaker();
				String className = rs.getString("TABLE_NAME");
				sqlMaker.setTableName(className);
				className = Character.toUpperCase(className.charAt(0))+className.substring(1);
				Class cls = Class.forName(packageName+"."+className);
				List<Method>insertData = new ArrayList();
				List<Method> primaryKeys = new ArrayList();
				List<ForeignKey> foreignKeys = new ArrayList();
				List<Method> foreignKeysMethods = new ArrayList();
				List<String> primaryKeyColumnName = new ArrayList();
				sqlMaker.setInsert(setInsertQuery(cls,insertData,primaryKeys,foreignKeys,primaryKeyColumnName,foreignKeysMethods));
				sqlMaker.setInsertDataMethods(insertData);
				sqlMaker.setPrimaryKeys(primaryKeys);
				sqlMaker.setPrimaryKeyColumnName(primaryKeyColumnName);
				sqlMaker.setForeignKeys(foreignKeys);
				sqlMaker.setForeignKeysMethods(foreignKeysMethods);
				List<Method> updateData = new ArrayList();
				sqlMaker.setUpdate(setUpdate(cls,updateData));
				sqlMaker.setUpdateDataMethods(updateData);
				sqlMaker.setDelete(setDelete(cls));
				sqlData.put(cls,sqlMaker);
			}
			ResultSet viewInfo = dbmetadata.getTables(config.getDatabaseName(),null,null,new String[] {"VIEW"});
			while(viewInfo.next())
			{
				String viewName = viewInfo.getString("TABLE_NAME");
				viewName = camelCase(viewName);
				Class viewClass = Class.forName(packageName+"."+viewName);
				sqlViews.add(viewClass);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void init()
	{
		cacheableData = new HashMap();
		try
		{
			DatabaseMetaData dbmetadata = connection.getMetaData();
			ResultSet rs = dbmetadata.getTables(config.getDatabaseName(),null,null, new String[] {"TABLE"});
			String packageName = config.getPackageName();
			while(rs.next())
			{
				SQLMaker sqlMaker = new SQLMaker();
				String className = rs.getString("TABLE_NAME");
				sqlMaker.setTableName(className);
				className = Character.toUpperCase(className.charAt(0))+className.substring(1);
				Class cls = Class.forName(packageName+"."+className);
				if(cls.isAnnotationPresent(Cacheable.class))
				{
					Table table = (Table)cls.getAnnotation(Table.class);
					String name = table.name();
					String query = "SELECT * FROM "+name;
					PreparedStatement pstatement = connection.prepareStatement(query);
					ResultSet resultSet = pstatement.executeQuery();
					List result = new ArrayList();
					Object obj = cls.newInstance();
					while(resultSet.next())
					{
						obj = cls.newInstance();
						Field[] fields = cls.getFields();
						for(int i = 0; i<fields.length; i++)
						{
							Field field = fields[i];
							String fieldName = field.getName();
							fieldName = Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1);
							String methodName = "set"+fieldName;
							Method method = cls.getDeclaredMethod(methodName,field.getType());
							method.invoke(obj,resultSet.getObject(i+1));
						}
						result.add(obj);
					}
					cacheableData.put(cls,result);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public PreparedStatement setInsertQuery(Class cls, List<Method> insertDataMethods,List<Method> primaryKeys,List<ForeignKey> foreignKeys,List<String> primaryKeyColumnName,List<Method> foreignKeysMethods)throws DataException
	{
		PreparedStatement pstatement = null;
		try
		{
			Field fields[] = cls.getFields();
			Table table = (Table)cls.getAnnotation(Table.class);
			String tableName = table.name();
			String query = "INSERT INTO "+tableName+" (";
			int values = 0;
			for(int i = 0; i<fields.length; i++)
			{
				Field field = fields[i];
				String fieldName = field.getName();
				Method m = cls.getDeclaredMethod("get"+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1));
				Column column = field.getAnnotation(Column.class);
				if(field.isAnnotationPresent(PrimaryKey.class))
				{
					primaryKeys.add(m);
					primaryKeyColumnName.add(column.name());
				}
				if(field.isAnnotationPresent(ForeignKey.class))
				{
					ForeignKey fk = field.getAnnotation(ForeignKey.class);
					foreignKeys.add(fk);
					foreignKeysMethods.add(m);
				}
				if(field.isAnnotationPresent(AutoIncrement.class))
					continue;
				insertDataMethods.add(m);
				values++;
				query = query+column.name();
				if(i != fields.length-1)
					query = query+",";
			}
			query = query+") VALUES (";
			for(int i = 0; i<values; i++)
			{
				query = query+"?";
				if(i != values-1)
					query = query+",";
			}
			query = query+")";
			pstatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
		}
		catch(Exception e)
		{
			throw new DataException(e.getMessage());
		}
		return pstatement;
	}
	public PreparedStatement setUpdate(Class cls, List<Method> updateDataMethods)throws DataException
	{
		PreparedStatement pstatement = null;
		try
		{
			Field fields[] = cls.getFields();
			Table table = (Table)cls.getAnnotation(Table.class);
			String tableName = table.name();
			String query = "UPDATE "+tableName+" SET ";
			String condition = " WHERE ";
			Method conditionMethod = null;
			for(int i = 0; i<fields.length; i++)
			{
				Field field = fields[i];
				String name = field.getName();
				Column column = field.getAnnotation(Column.class);
				String columnName = column.name();
				Method method = cls.getDeclaredMethod("get"+Character.toUpperCase(name.charAt(0))+name.substring(1));
				if(field.isAnnotationPresent(PrimaryKey.class) || field.isAnnotationPresent(AutoIncrement.class))
				{
					condition = condition+columnName+"=?";
					conditionMethod = method;
				}
				else
				{
					updateDataMethods.add(method);
					query = query+columnName+" = ?";
					if(i != fields.length-1)
						query = query+",";
				}
			}
			query = query+condition;
			updateDataMethods.add(conditionMethod);
			pstatement = connection.prepareStatement(query);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new DataException(e.getMessage());
		}
		return pstatement;
	}
	public PreparedStatement setDelete(Class cls)throws DataException
	{
		PreparedStatement pstatement = null;
		try
		{
			Field fields[] = cls.getDeclaredFields();
			Table table = (Table)cls.getAnnotation(Table.class);
			String tableName = table.name();
			DatabaseMetaData dbmetadata = connection.getMetaData();
			List<String>tables = new ArrayList();
			List<String>columns = new ArrayList();
	 		ResultSet exportedKeys = dbmetadata.getExportedKeys(config.getDatabaseName(),null,tableName);
			String query = "DELETE FROM "+tableName+" WHERE ";
			for(int i = 0; i<fields.length; i++)
			{
				Field field = fields[i];
				Column column = field.getAnnotation(Column.class);
				String columnName = column.name();
				if(field.isAnnotationPresent(PrimaryKey.class))
				{
					query = query+columnName+" = ?";
					String fieldName = field.getName();
				}
			}
			pstatement = connection.prepareStatement(query);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new DataException(e.getMessage());
		}
		return pstatement;
	}
	public void start()
	{
		try
		{
			FileReader fileReader = new FileReader("conf.json");
			BufferedReader br = new BufferedReader(fileReader);
			String d;
			StringBuffer sb = new StringBuffer();
			while(true)
			{
				d = br.readLine();
				if(d == null)
					break;
				sb.append(d);
			}
			String rawData = sb.toString();
			Gson gson = new Gson();
			config = gson.fromJson(rawData,Configuration.class);
			String databaseName = config.getConnectionUrl();
			databaseName = databaseName.substring(databaseName.lastIndexOf('/')+1);
			config.setDatabaseName(databaseName);
			Class.forName(config.getJdbcDriver());
			connection = DriverManager.getConnection(config.getConnectionUrl(),config.getUsername(),config.getPassword());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public String camelCase(String word)
	{
		String[] words = word.split("_");
		if(words.length == 1)
		{
			return Character.toUpperCase(words[0].charAt(0))+words[0].substring(1).toLowerCase();
		}
		String ans = Character.toUpperCase(words[0].charAt(0))+words[0].substring(1).toLowerCase();
		for(int i = 1; i<words.length; i++)
		{
			ans = ans+Character.toUpperCase(words[i].charAt(0))+words[i].substring(1).toLowerCase();
		}
		return ans;
	}
	public int save(Object obj)throws DataException
	{	
		if(sqlViews.contains(obj.getClass()))
		{
			throw new DataException(obj.getClass().toString()+" is of type 'View' it cannot support insert feature");
		}
		if(cacheableData.containsKey(obj.getClass()))
		{
			List<Object> vals = cacheableData.get(obj.getClass());
			vals.add(obj);
			cacheableData.put(obj.getClass(),vals);
		}
		System.out.println("Caheable MAP : "+cacheableData);
		int generatedKeys = 0;
		try
		{	
			SQLMaker sqlMaker = sqlData.get(obj.getClass());
			List<ForeignKey> foreignKeys = sqlMaker.getForeignKeys();
			List<Method> foreignKeysMethods = sqlMaker.getForeignKeysMethods();
			for(int i = 0; i<foreignKeys.size(); i++)
			{
				Object value = foreignKeysMethods.get(i).invoke(obj);
				String parentTableName = foreignKeys.get(i).parent();
				String parentColumnName = foreignKeys.get(i).column();
				String query = "select "+parentColumnName+" from "+parentTableName+" where "+parentColumnName+" = ?";
				PreparedStatement pstatement = connection.prepareStatement(query);
				pstatement.setObject(1,value);
				ResultSet rs = pstatement.executeQuery();
				if(!rs.next())
				{
					throw new DataException(value+" does not exist in column "+parentColumnName+" of table "+parentTableName);
				}
			}
			PreparedStatement pstatement = sqlMaker.getInsert();
			List<Method> methods = sqlMaker.getInsertDataMethods();
			Object value = null;
			for(int i= 0; i<methods.size(); i++)
			{
				Method method = methods.get(i);
				value = method.invoke(obj);
				if(java.util.Date.class.equals(method.getReturnType()))
				{
					java.util.Date date = (java.util.Date)method.invoke(obj);
					value = new java.sql.Date(date.getTime());
				}
				pstatement.setObject(i+1,value);
				
			}
			pstatement.executeUpdate();
			ResultSet rs = pstatement.getGeneratedKeys();
			if(rs.next())
				generatedKeys = rs.getInt(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		return generatedKeys;
	}
	public void update(Object obj)throws DataException
	{
		if(sqlViews.contains(obj.getClass()))
		{
			throw new DataException(obj.getClass().toString()+" is of type 'View' it cannot support update feature");
		}
		if(cacheableData.containsKey(obj.getClass()))
		{
			List<Object> vals = cacheableData.get(obj.getClass());
			
			cacheableData.put(obj.getClass(),vals);
		}
		try
		{
			SQLMaker sqlMaker = sqlData.get(obj.getClass());
			List<String>primaryKeyColumnName = sqlMaker.getPrimaryKeyColumnName();
			if(obj.getClass().isAnnotationPresent(Cacheable.class))
			{
				String primaryKey = primaryKeyColumnName.get(0);
				primaryKey = camelCase(primaryKey);
				List values = cacheableData.get(obj.getClass());
				for(int i = 0; i<values.size(); i++)
				{
					Method method = obj.getClass().getDeclaredMethod("get"+primaryKey);
					Object val = method.invoke(values.get(i));
					Object check = method.invoke(obj);
					if(val.equals(check))
					{
						System.out.println("Removing Values");
						values.remove(i);
					}
				}
				values.add(obj);
				cacheableData.put(obj.getClass(),values);
			}
			List<Method> primaryKeys = sqlMaker.getPrimaryKeys();
			for(int i = 0; i<primaryKeys.size(); i++)
			{
				Object value = primaryKeys.get(i).invoke(obj);
				String query = "select "+primaryKeyColumnName.get(i)+" from "+sqlMaker.getTableName()+" where "+primaryKeyColumnName.get(i)+" = "+value;
				PreparedStatement pstatement = connection.prepareStatement(query);
				ResultSet rs = pstatement.executeQuery();
				if(!rs.next())
				{
					throw new DataException(value+" cannot be found in column "+primaryKeyColumnName.get(i)+" of table "+sqlMaker.getTableName());
				}
			}
			List<ForeignKey> foreignKeys = sqlMaker.getForeignKeys();
			List<Method> foreignKeysMethods = sqlMaker.getForeignKeysMethods();
			for(int i = 0; i<foreignKeys.size(); i++)
			{
				Object value = foreignKeysMethods.get(i).invoke(obj);
				String parentTableName = foreignKeys.get(i).parent();
				String parentColumnName = foreignKeys.get(i).column();
				String query = "select "+parentColumnName+" from "+parentTableName+" where "+parentColumnName+" = ?";
				PreparedStatement pstatement = connection.prepareStatement(query);
				pstatement.setObject(1,value);
				ResultSet rs = pstatement.executeQuery();
				if(!rs.next())
				{
					throw new DataException(value+" does not exist in column "+parentColumnName+" of table "+parentTableName);
				}
			}
			List<Method>methods = sqlMaker.getUpdateDataMethods();
			PreparedStatement pstatement = sqlMaker.getUpdate();
			for(int i = 0; i<methods.size(); i++)
			{
				pstatement.setObject(i+1,methods.get(i).invoke(obj));
			}
			pstatement.executeUpdate();
		}
		catch(Exception e)
		{
			throw new DataException(e.getMessage());
		}
	}
	public void delete(Class cls, Object obj)throws DataException
	{
		if(sqlViews.contains(cls))
		{
			throw new DataException(cls.toString()+" is of type 'View' it cannot support delete feature");
		}
		
		try
		{
			SQLMaker sqlMaker = sqlData.get(cls);
			List<String>primaryKeyColumnName = sqlMaker.getPrimaryKeyColumnName();
			if(cls.isAnnotationPresent(Cacheable.class))
			{
				String primaryKey = primaryKeyColumnName.get(0);
				primaryKey = camelCase(primaryKey);
				List values = cacheableData.get(cls);
				for(int i = 0; i<values.size(); i++)
				{
					Method method = cls.getDeclaredMethod("get"+primaryKey);
					Object val = method.invoke(values.get(i));
					if(val.equals(obj))
					{
						values.remove(i);
					}
				}
				cacheableData.put(cls,values);
			}
			String query = "select "+primaryKeyColumnName.get(0)+" from "+sqlMaker.getTableName()+" where "+primaryKeyColumnName.get(0)+" = ?";
			PreparedStatement pstatement = connection.prepareStatement(query);
			pstatement.setObject(1,obj);
			ResultSet rs = pstatement.executeQuery();
			if(!rs.next())
			{
				throw new DataException(obj+" cannot be found in column "+primaryKeyColumnName.get(0)+" of table "+sqlMaker.getTableName());
			}
			pstatement = sqlMaker.getDelete();
			pstatement.setObject(1,obj);
			pstatement.executeUpdate();
		}
		catch(Exception e)
		{
			throw new DataException(e.getMessage());
		}
	}
	public Query query(Class cls)
	{	
		Query quer = new Query(connection);
		Table table = (Table)cls.getAnnotation(Table.class);
		if(cls.isAnnotationPresent(View.class))
		{
			View view = (View)cls.getAnnotation(View.class);
			quer.tableName = view.name();
			System.out.println("Name Of View : "+view.name());
		}
		else
		{
			quer.tableName = table.name();
		}
		if(cls.isAnnotationPresent(Cacheable.class))
		{
			quer.result = cacheableData.get(cls);
		}
		quer.className = cls;
		return quer;
	}

}