public class Configuration
{
	private String jdbcDriver;
	private String connectionUrl;
	private String username;
	private String password;
	private String databaseName;
	private String packageName;
	private String jarName;
	public void setJdbcDriver(String jdbcDriver)
	{
		this.jdbcDriver = jdbcDriver;
	}
    public String getJdbcDriver()
    {
        return this.jdbcDriver;
    }
	public void setConnectionUrl(String connectionUrl)
	{
		this.connectionUrl = connectionUrl;
	}
	public String getConnectionUrl()
	{
		return this.connectionUrl;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	public String getUsername()
	{
		return this.username;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public String getPassword()
	{
		return this.password;
	}
	public void setDatabaseName(String databaseName)
	{
		this.databaseName = databaseName;
	}
	public String getDatabaseName()
	{
		return this.databaseName;
	}
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}
	public String getPackageName()
	{
		return this.packageName;
	}
	public void setJarName(String jarName)
	{
		this.jarName = jarName;
	}
	public String getJarName()
	{
		return this.jarName;
	}
}