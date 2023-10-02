# Over View : 
The Repository is of making connection with database and access database from the code without writing any code related to databae.
# Features : 
1. Creates Database Connections and can access data by just calling some functions.
2. Can create Entity Files using EntityMaker class and can create jar file of these classes by using EntityCompiler class.
3. User has not to write any code for connecting to database.
4. User has not to write any code for weriting queries of select insert delete data.
5. User can create in memory database using cacheable annotation which makes accesing of data even more faster.
# Steps Of Usage : 
1. User has to create a conf.json File in which he has to mention all details of the data base and driver url and username and password for the user and package name of entity classes and name of jar file.
2. Then in command Promt he has to call for EntityMaker class.
3. To make in memory database the user should go to src/"packagename" and open java file corresponding to table and write @Cacheable before @Table(name = "tablename") annotation.
4. Now in Command Prompt run the EntityCompiler class this will create a jar file in disk folder.
5. Now create your main backend program file.
6. In this file create an object of DataManager class.
6. Now call for DataManager.init() this will create an in memory database.
7. Now for insertion of data call datamanager.save(Object obj) which accepts object of class which is created by EntityMaker.
8. For Updation call dataManager.update(Object obj).
9. For deletion call dataManager.delete(Class cls, Object obj) where cls id class which is created By EntityMaker and obj is value of primary key of the corresponding table.
10. For select call dm.query(Class cls) where cls is class for whose corresponding table you want to access data.
11. Then call .where to add condition for a column or .orderBy to add order by caluse.
12. For condtion to get > sign in clause add .gt(int val) for < add .lt(int val) and for = add .eq(int val).
13. Now at last to fire the query add .fire().
14. The return type of this statement is list of objects traverse the lost to get all credentials.
# EXAMPLE : 
In this project I have provided the conf.json to show how to give the credentials of database.
And Test.java to show how to use all the CRUD Operations of Data Base.
In src/com/thinking/machine/pojo/Course.java I have given an example of in memory Database.
# Thanks For Viewing My Project