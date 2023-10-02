import java.sql.*;
import java.lang.reflect.*;
import java.util.*;
import annotations.*;
public class SQLMaker
{
	private String tableName;
	private PreparedStatement insert;
	private PreparedStatement update;
	private PreparedStatement delete;
	private List<Method> insertDataMethods;
	private List<Method> updateDataMethods;
	private List<ForeignKey> foreignKeys;
	private List<Method> primaryKeys;
	private List<Method> foreignKeysMethods;
	private List<String> primaryKeyColumnName;
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
	public String getTableName()
	{
		return this.tableName;
	}
	public void setInsert(PreparedStatement insert)
	{
		this.insert = insert;
	}
	public PreparedStatement getInsert()
	{
		return this.insert;
	}
	public void setUpdate(PreparedStatement update)
	{
		this.update = update;
	}
	public PreparedStatement getUpdate()
	{
		return this.update;
	}
	public void setDelete(PreparedStatement delete)
	{
		this.delete = delete;
	}
	public PreparedStatement getDelete()
	{
		return this.delete;
	}
	public void setInsertDataMethods(List<Method> insertDataMethods)
	{
		this.insertDataMethods = insertDataMethods;
	}
	public List<Method> getInsertDataMethods()
	{
		return this.insertDataMethods;
	}
	public void setUpdateDataMethods(List<Method> updateDataMethods)
	{
		this.updateDataMethods = updateDataMethods;
	}
	public List<Method> getUpdateDataMethods()
	{
		return this.updateDataMethods;
	}
	public void setPrimaryKeys(List<Method> primaryKeys)
	{
		this.primaryKeys = primaryKeys;
	}
	public List<Method> getPrimaryKeys()
	{
		return this.primaryKeys;
	}
	public void setForeignKeysMethods(List<Method>foreignKeysMethods)
	{
		this.foreignKeysMethods = foreignKeysMethods;
	}
	public List<Method> getForeignKeysMethods()
	{
		return this.foreignKeysMethods;
	}
	public void setForeignKeys(List<ForeignKey> foreignKeys)
	{
		this.foreignKeys = foreignKeys;
	}
	public List<ForeignKey> getForeignKeys()
	{
		return this.foreignKeys;
	}
	public void setPrimaryKeyColumnName(List<String>primaryKeyColumnName)
	{
		this.primaryKeyColumnName = primaryKeyColumnName;
	}
	public List<String> getPrimaryKeyColumnName()
	{
		return this.primaryKeyColumnName;
	}
}