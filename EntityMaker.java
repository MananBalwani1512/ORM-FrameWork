import javax.swing.text.html.parser.Entity;
import javax.tools.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import com.google.gson.*;
import java.nio.file.*;
import annotations.*;
public class EntityMaker
{
	public static Configuration config;
	private Connection connection;
	public Connection getConnection()
	{
		Connection connection = null;
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
			String dbName = config.getConnectionUrl();
			dbName = dbName.substring(dbName.lastIndexOf('/')+1);
			config.setDatabaseName(dbName);
			Class.forName(config.getJdbcDriver());
			connection = DriverManager.getConnection(config.getConnectionUrl(),config.getUsername(),config.getPassword());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return connection;
	}
	public String createEntityFile(DatabaseMetaData dbmetadata, String tableName)
	{
		char first = tableName.charAt(0);
		first = Character.toUpperCase(first);
		String table = first+tableName.substring(1);
		String packageName = config.getPackageName();
		String absolutePath = new File("").getAbsolutePath();
		absolutePath = absolutePath.replace("\\","/");
		String packageFolder = packageName.replace('.','/');
		try
		{
			File file = null;
			File directory = new File("");
			if(packageName != null)
			{
				directory = new File(absolutePath+"/src/"+packageFolder);
				absolutePath = absolutePath+"/src/"+packageFolder+"/";
			}
			else
			{
				directory = new File(absolutePath+"/src");
				absolutePath = absolutePath+"/src/";
			}
			if(!directory.exists())
			{
				directory.mkdirs();
			}
			file = new File(absolutePath+table+".java");
			file.createNewFile();
			FileWriter writer = new FileWriter(absolutePath+table+".java");
			if(packageName != null)
			{
				writer.write("package "+packageName+";\n");
			}
			writer.write("import annotations.*;\n");
			writer.write("@Table(name=\""+tableName+"\")\n");
			writer.write("public class "+table+"\n");
			writer.write("{\n");
			ResultSet columnInfo = dbmetadata.getColumns(config.getDatabaseName(),null,tableName,null);
			HashSet<String>primaryKeys = new HashSet();
			HashSet<String>foreignKeys = new HashSet();
			List<String>exportedKeys = new ArrayList();
			getImportedKeys(dbmetadata,tableName,foreignKeys,primaryKeys,exportedKeys);
			while(columnInfo.next())
			{
				writer.write("@Column(name=\""+columnInfo.getString("COLUMN_NAME")+"\")\n");
				String dataType = "";
				if(columnInfo.getString("IS_AUTOINCREMENT").equals("YES"))
					writer.write("@AutoIncrement\n");
				if(primaryKeys.contains(columnInfo.getString("COLUMN_NAME")))
					writer.write("@PrimaryKey\n");
				if(foreignKeys.contains(columnInfo.getString("COLUMN_NAME")))
					writer.write("@ForeignKey(parent = \""+exportedKeys.get(1)+"\",column=\""+exportedKeys.get(0)+"\")\n");
				if(columnInfo.getString("TYPE_NAME").equals("INT"))
				{
					dataType = "int";
					writer.write("	public int ");
				}
				else if(columnInfo.getString("TYPE_NAME").equals("VARCHAR"))
				{
					dataType = "String";
					writer.write(" public String ");
				}
				else if(columnInfo.getString("TYPE_NAME").equals("DATE"))
				{
					dataType = "java.util.Date";
					writer.write("public java.util.Date ");
				}
				String columnName = columnInfo.getString("COLUMN_NAME");
				writer.write(changeToCamelCase(columnName)+";\n");
				String name = changeToCamelCase(columnName);
				writer.write("public void set"+Character.toUpperCase(name.charAt(0))+name.substring(1)+"("+dataType+" "+name+")\n");
				writer.write("{\n");
				writer.write("	this."+name+" = "+name+";\n");
				writer.write("}\n");
				writer.write("public "+dataType+" get"+Character.toUpperCase(name.charAt(0))+name.substring(1)+"()\n");
				writer.write("{\n");
				writer.write("	return this."+name+";\n");
				writer.write("}\n");
			}
			writer.write("}");
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
		return absolutePath;
	}
	public static String changeToCamelCase(String word)
	{
		String[] words = word.split("_");
		if(words.length == 1)
		{
			return words[0].toLowerCase();
		}
		String ans = words[0].toLowerCase();
		for(int i = 1; i<words.length; i++)
		{
			ans = ans+Character.toUpperCase(words[i].charAt(0))+words[i].substring(1).toLowerCase();
		}
		return ans;
	}
	public static void getImportedKeys(DatabaseMetaData dbmetadata,String tableName,HashSet<String>foreignKeys,HashSet<String>primaryKeys,List<String>exportedKeys)
	{
		try
		{
			ResultSet keys = dbmetadata.getPrimaryKeys(config.getDatabaseName(),null,tableName);
			while(keys.next())
			{
				primaryKeys.add(keys.getString(4));
			}
			keys = dbmetadata.getImportedKeys(config.getDatabaseName(),null,tableName);
			while(keys.next())
			{
				foreignKeys.add(keys.getString("FKCOLUMN_NAME"));
				exportedKeys.add(keys.getString("PKCOLUMN_NAME"));
				exportedKeys.add(keys.getString("PKTABLE_NAME"));
			}
			keys = dbmetadata.getExportedKeys(config.getDatabaseName(),null,tableName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String createViewFile(String viewName,DatabaseMetaData dbmetadata)
	{
		String view = changeToCamelCase(viewName);
		char first = view.charAt(0);
		first = Character.toUpperCase(first);
		String table = first+view.substring(1);
		String packageName = config.getPackageName();
		String absolutePath = new File("").getAbsolutePath();
		absolutePath = absolutePath.replace("\\","/");
		String packageFolder = packageName.replace('.','/');
		try
		{
			File file = null;
			File directory = new File("");
			if(packageName != null)
			{
				directory = new File(absolutePath+"/src/"+packageFolder);
				absolutePath = absolutePath+"/src/"+packageFolder+"/";
			}
			else
			{
				directory = new File(absolutePath+"/src");
				absolutePath = absolutePath+"/src/";
			}
			if(!directory.exists())
			{
				directory.mkdirs();
			}
			file = new File(absolutePath+table+".java");
			file.createNewFile();
			FileWriter writer = new FileWriter(absolutePath+table+".java");
			if(packageName != null)
			{
				writer.write("package "+packageName+";\n");
			}
			System.out.println(viewName);
			writer.write("import annotations.*;\n");
			writer.write("@View(name = \""+viewName+"\")\n");
			writer.write("public class "+table+"\n");
			writer.write("{\n");
			ResultSet columnInfo = dbmetadata.getColumns(config.getDatabaseName(),null,viewName,null);
			while(columnInfo.next())
			{
				String dataType = "";
				if(columnInfo.getString("TYPE_NAME").equals("INT"))
				{
					dataType = "int";
					writer.write("	public int ");
				}
				else if(columnInfo.getString("TYPE_NAME").equals("VARCHAR"))
				{
					dataType = "String";
					writer.write(" public String ");
				}
				else if(columnInfo.getString("TYPE_NAME").equals("DATE"))
				{
					dataType = "java.util.Date";
					writer.write("public java.util.Date ");
				}
				String columnName = columnInfo.getString("COLUMN_NAME");
				writer.write(changeToCamelCase(columnName)+";\n");
				String name = changeToCamelCase(columnName);
				writer.write("public void set"+Character.toUpperCase(name.charAt(0))+name.substring(1)+"("+dataType+" "+name+")\n");
				writer.write("{\n");
				writer.write("	this."+name+" = "+name+";\n");
				writer.write("}\n");
				writer.write("public "+dataType+" get"+Character.toUpperCase(name.charAt(0))+name.substring(1)+"()\n");
				writer.write("{\n");
				writer.write("	return this."+name+";\n");
				writer.write("}\n");
			}
			writer.write("}\n");
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return absolutePath+table;
	}
	public static void main(String args[])
	{
		EntityMaker entityMaker = new EntityMaker();
		entityMaker.connection = entityMaker.getConnection();
		try
		{
			DatabaseMetaData dbmetadata = entityMaker.connection.getMetaData();
			ResultSet tableInfo = dbmetadata.getTables(config.getDatabaseName(),null,null,new String[] {"TABLE"});
			String fileName = "";
			while(tableInfo.next())
			{
				fileName = entityMaker.createEntityFile(dbmetadata,tableInfo.getString("TABLE_NAME"));
			}
			ResultSet viewInfo = dbmetadata.getTables(config.getDatabaseName(),null,null,new String[]{"VIEW"});
			while(viewInfo.next())
			{
				entityMaker.createViewFile(viewInfo.getString("TABLE_NAME"),dbmetadata);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
class EntityCompiler
{
	public void compile(String path)
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String classPath = new File("").getAbsolutePath();
		try
		{
			Process process = Runtime.getRuntime().exec("javac -classpath "+classPath+";. *.java",null,new File(path));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void createJarFile(String path, Configuration config)
	{
		try
		{
			Process process = Runtime.getRuntime().exec("jar -cf "+config.getJarName()+".jar *",null,new File(path+"/src"));
			File file = new File(path+"/disk");
			if(!file.exists())
				file.mkdirs();
			File jar = new File(path+"\\src\\"+config.getJarName()+".jar");
			File dest = new File(path+"\\disk\\"+config.getJarName()+".jar");
			if(dest.exists())
				dest.delete();
			Files.copy(jar.toPath(),dest.toPath());
			System.out.println(jar.delete());
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		EntityMaker entityMaker = new EntityMaker();
		entityMaker.getConnection();
		Configuration config = EntityMaker.config;
		String packageName = config.getPackageName();
		packageName = packageName.replace('.','/');
		String path = new File("").getAbsolutePath();
		EntityCompiler entityCompiler = new EntityCompiler();
		entityCompiler.compile(path+"/src/"+packageName);
		entityCompiler.createJarFile(path,config);
	}
}