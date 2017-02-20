# Extended mapping to Oracle database with help of MyBatis 

This is a sample project with demonstration of some unusual features of combination MyBatis and Oracle DB. 
List of demonstrated features:

* Calling of procedures and functions with IN and specially OUT parameters
* Calling of functions returning cursor type (Oracle `SYS_REFCURSOR`)
* Mapping of record/object/array types (Oracle UDT) in procedure/function parameters

## Prerequisites

* Installed Maven tool with access to public repositories
* Installed Oracle database with installed demo HR schema. Connection URL and credentials are defined in 
`src/main/resources/cz/sidik/demo/oraclemybatis/jdbc.properties`, you can modify them to fit your settings
* Solved dependency for Oracle JDBC drivers, there is two ways, see links in comments near Oracle JDBC dependency in `pom.xml` 
 
## Usage

Just simple run `mvn clean test` in root of downloaded project.

## Used frameworks

There is used MyBatis of course :-) And Spring framework, but it is there only for basic application and MyBatis configuration 
(data source, transactions, properties, database init). And IMHO for better readability and easier 
scaffolding of new project (= copy-pasting). JUnit and Slf4j is present too.
 
## HR schema

Tests use table structure and data set from standard HR demonstration schema, which is an optional part of Oracle DB 
installation. There are installed some demonstration procedures, functions and data types during test preparation, but 
all added parts are removed after the test finish (see scripts `src/main/resources/cz/sidik/demo/oraclemybatis/oracle_init_script.sql` and 
`src/main/resources/cz/sidik/demo/oraclemybatis/oracle_destroy_script.sql` and their usage in spring context definition - tag `jdbc:initialize-database`). 

All tests run in transaction mode, which will rollback data changes from every
test (see [AbstractTransactionalJUnit4SpringContextTests](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/test/context/junit4/AbstractTransactionalJUnit4SpringContextTests.html))

## Mapping of output parameters in DB procedures and functions

This section demonstrates, how output parameters/function return values can be mapped to Java data structures.

### How to call a simple DB procedure with output parameter

Java does not support output parameters on methods directly, so output parameters must be returned in some container types.
One possibility is return value inside some collection, which is initialized by the caller code. As an useful container
can be used `Map<String, Object>` interface. It can be used for returning of all output parameters in the procedure.
There is one disadvantage, returned parameters must be mapped to some "keys" - hard coded string constants and this binding
is not clear on the first sight. Demonstration from `cz.sidik.demo.oraclemybatis.mapper.OutputParamMapper.setEmployeeNameOut`:

```java
public interface OutputParamMapper {

    String OLD_NAME_OUT_PARAM = "oldName";
    
    void setEmployeeNameOut(@Param("employeeId") Long employeeId, @Param("firstName") String firstName, @Param("lastName") String lastName, @Param("outParams") Map<String, String> outputParameters);
}
````

```xml
<update id="setEmployeeNameOut" statementType="CALLABLE">
    {CALL set_employee_name_out(#{employeeId, jdbcType=NUMERIC, mode=IN},
        #{firstName, jdbcType=VARCHAR, mode=IN},
        #{lastName, jdbcType=VARCHAR, mode=IN},
        #{outParams.oldName, jdbcType=VARCHAR, mode=OUT, javaType=java.lang.String}) }
</update>
```

The method is called from `cz.sidik.demo.oraclemybatis.OutputParamTest.testSetEmployeeNameOut`.

I personally prefer a syntax with inline parameter declaration with use of `@Param` annotation and I will use it in all examples.
Be careful, that `jdbcType` and `mode` is declared for all parameters and the output one has a declaration of `javaType` too.


### Mapping of output parameter back into DAO object

The other possibility is use some Plain Old Java Object with JavaBeans property-style methods, which is supported in MyBatis.
In this case can be output parameters stored directly inside some DAO class. E.g. calling of `INSERT` procedure can fill up
generated record Primary Key directly inside structured DAO object. See: 
`cz.sidik.demo.oraclemybatis.mapper.OutputParamMapper.setEmployeeNameDaoOut`

```java
public interface OutputParamMapper {
    void setEmployeeNameDaoOut(@Param("employee") EmployeeOutDao employee);
}
```

```xml
<update id="setEmployeeNameDaoOut" statementType="CALLABLE">
    {CALL set_employee_name_out(#{employee.employeeId, jdbcType=NUMERIC, mode=IN},
        #{employee.firstName, jdbcType=VARCHAR, mode=IN},
        #{employee.lastName, jdbcType=VARCHAR, mode=IN},
        #{employee.oldName, jdbcType=VARCHAR, mode=OUT, javaType=java.lang.String}) }
</update>
```

The method is called from `cz.sidik.demo.oraclemybatis.OutputParamTest.testSetEmployeeNameDaoOut`.

### Calling of DB function

Generally speaking, function is a special case of procedure with a syntax difference. There is one special output parameter
called return value, which has a special place in SQL command code, but other rules remains the same. See:
`cz.sidik.demo.oraclemybatis.mapper.OutputParamMapper.setEmployeeName` with returning value with help of container `Map` class.

```java
public interface OutputParamMapper {
    String OLD_NAME_OUT_PARAM = "oldName";
    
    void setEmployeeName(@Param("employeeId") Long employeeId, @Param("firstName") String firstName, @Param("lastName") String lastName, @Param("outParams") Map<String, String> outputParameters);
}
```

```xml
<update id="setEmployeeName" statementType="CALLABLE">
    {CALL #{outParams.oldName, jdbcType=VARCHAR, mode=OUT, javaType=java.lang.String}
        := set_employee_name(#{employeeId, jdbcType=NUMERIC, mode=IN},
              #{firstName, jdbcType=VARCHAR, mode=IN},
              #{lastName, jdbcType=VARCHAR, mode=IN}) }
</update>
```

The method is called from `cz.sidik.demo.oraclemybatis.OutputParamTest.testSetEmployeeName`.

You can see that all code is the same with procedure variant, instead of syntax of SQL procedure call. And the variant
with value passing by POJO can be used too (demonstrated in `cz.sidik.demo.oraclemybatis.mapper.OutputParamMapper.setEmployeeNameDao`).

### How to return a function result as a result of Java method in MyBatis mapper

The PL/SQL function return value can not be generally passed as return value of MyBatis Java function. But there is one exception.
In case, that Oracle function [can be called in SQL SELECT command](http://www.toadworld.com/platforms/oracle/w/wiki/1958.function-calling-a-function-inside-sql), 
it is possible to use a trick, where the return value
is passed as a single-column single-row result set and MyBatis maps result set as result value of mapping function. 
See a demonstration in `cz.sidik.demo.oraclemybatis.mapper.OutputParamMapper.getEmployeeName`

```java
public interface OutputParamMapper {
    String getEmployeeName(@Param("employeeId") Long employeeId);
}
```

```xml
<select id="getEmployeeName" resultType="String">
    SELECT get_employee_name(#{employeeId, jdbcType=NUMERIC, mode=IN})
    FROM dual
</select>
```

It is not `CALLABLE` statement, but it is standard `PREPARED` `statementType`. Usage is demonstrated in 
`cz.sidik.demo.oraclemybatis.OutputParamTest.testGetEmployeeName`.

## Mapping of returned cursor

Oracle DB supports a generic data type `SYS_REFCURSOR`, which represents a result set type and can be passed between PL/SQL
procedures/functions. MyBatis supports a mapping of this DB collection type as an output parameter or return value.
It can be mapped to Java `Collection` of something easily.

All four variants of mapping can be used similarly as in previous cases (Map x POJO, procedure x function), so only one 
variant will be described in this documentation and the other variants will be present only in code. Variant for function 
and POJO is in `cz.sidik.demo.oraclemybatis.mapper.CursorMapper.getEmployeesDao`:

```java
public class GetEmployeesDao { // POJO for function call
    public Long departmentId;
    public Collection<EmployeeDao> employees; // POJO for cursor item
}

public interface CursorMapper {
    void getEmployeesDao(@Param("getClientsQuery") GetEmployeesDao getClientsQuery);
}
```

```xml
<resultMap id="getEmployeesResultSet" type="cz.sidik.demo.oraclemybatis.bo.EmployeeDao">
    <result property="employeeId" column="EMPLOYEE_ID"/>
    <result property="firstName" column="FIRST_NAME"/>
    <result property="lastName" column="LAST_NAME"/>
</resultMap>
```

```xml
<select id="getEmployeesDao" statementType="CALLABLE">
    {CALL #{getClientsQuery.employees, jdbcType=CURSOR, mode=OUT, javaType=java.sql.ResultSet, resultMap=getEmployeesResultSet}
        := get_employees(#{getClientsQuery.departmentId, jdbcType=NUMERIC, mode=IN})}
</select>
```

It looks very similarly as standard function call. There is only these differences: 
* parameter inline definition `jdbcType=CURSOR, javaType=java.sql.ResultSet` for correct JDBC/Java type mapping
* parameter inline definition `resultMap=getEmployeesResultSet` and associated `resultMap` definition, which describes
mapping between cursor result-set columns and properties of the target POJO type (`cz.sidik.demo.oraclemybatis.bo.EmployeeDao` in this case)

Be careful that column aliases must exactly match with the ones from the DB cursor (see `cz/sidik/demo/oraclemybatis/oracle_init_script.sql:58`).

Be aware that all data are processed in one chunk. JDBC fetches all records from Oracle DB to JVM, after that MyBatis
do a mapping transformation with help `resultMap` definition and after processing of all records in cursor 
the result will be returned back into calling Java code after all.

`SYS_REFCURSOR` can be declared with help of some record type, so it has strongly typed structure, but this is irrelevant for
MyBatis mapping, same rules may be used in this case.

### How to return a cursor as a result of Java method (Vol. 1)

In case of DB function returning cursor, can be used the same trick as in [previous chapeter](#how-to-return-a-function-result-as-a-result-of-java-method-in-mybatis-mapper)
with the same limitations. A little confusing is that result set is not a content cursor, but result set contains a 
single-column single-row result set, which contains one `CURSOR` value with data. And MyBatis does not support easy
way, how to write mapping for this CURSOR in `SELECT` result set (there is no alternative for `resultMap` parameter
in inline parameter statement). But it is possible to write custom mapping handler between DB cursor type and 
Java collection of target POJOs. See at `cz.sidik.demo.oraclemybatis.mapper.CursorMapper.getEmployeesCursor`:

```java
public class GetEmployeesDao { // POJO for function call
    public Long departmentId;
    public Collection<EmployeeDao> employees; // POJO for cursor item
}

public class EmployeeDaoCursorTypeHandler extends BaseTypeHandler<Collection<EmployeeDao>> {

    // Some overridden methods
    
    @Override
    public Collection<EmployeeDao> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getEmployeeDaos(((OracleResultSet) rs).getCursor(columnName));
    }

    @Override
    public Collection<EmployeeDao> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getEmployeeDaos(((OracleResultSet) rs).getCursor(columnIndex));
    }

    private Collection<EmployeeDao> getEmployeeDaos(ResultSet cursorResultSet) throws SQLException {
        Collection<EmployeeDao> result = new LinkedList<>();
        while (cursorResultSet.next()) {
            EmployeeDao employeeDao = new EmployeeDao();
            employeeDao.employeeId = cursorResultSet.getLong("EMPLOYEE_ID");
            employeeDao.firstName = cursorResultSet.getString("FIRST_NAME");
            employeeDao.lastName = cursorResultSet.getString("LAST_NAME");
            result.add(employeeDao);
        }
        return result;
    }
}

public interface CursorMapper {
    GetEmployeesDao getEmployeesCursor(@Param("departmentId") Long departmentId);
}
```

```xml
<resultMap id="getEmployeesCursorResultSet" type="cz.sidik.demo.oraclemybatis.bo.GetEmployeesDao">
    <result property="employees" column="RESULT_SET" typeHandler="cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoCursorTypeHandler"/>
</resultMap>
```

```xml
<select id="getEmployeesCursor" resultMap="getEmployeesCursorResultSet">
        SELECT get_employees(#{departmentId, jdbcType=NUMERIC, mode=IN}) AS RESULT_SET
        FROM dual
</select>
```

There is simple MyBatis type handler between Oracle cursor type and collection of target POJOs and the rest is straightforward. 

At first sight it looks great, but this solution does not solve a problem, that all data/items/records of cursor are processed
in one chunk, even the `SELECT` clause is used. This setup **does not fetch** cursor records one by one. But there 
is one solution described in next chapters.

Btw. this code is specific for Oracle JDBC driver and works with `oracle.jdbc.OracleResultSet` specific class. 

## Mapping of User Data Objects

Oracle DB has a feature of user defined data types - record (= `OBJECT`) and array (= `TABLE OF`). The definition is easy:

```sql
CREATE OR REPLACE TYPE employee_structtype FORCE AS OBJECT (
  employee_id NUMBER(6),
  first_name  VARCHAR2(20),
  last_name   VARCHAR2(25)
);

CREATE OR REPLACE TYPE employee_arraytype FORCE AS TABLE OF employee_structtype;
```

These types can be combined with each other, so collection of records with collection of records is possible.

MyBatis supports mapping fo these types into Java POJO classes, but this can not be done with some configuration, some
specialized type mapper must be written. There is a sample code, which supports these demonstrated types, but this code 
could be easily customized for any record/collection type. The code is commented in 
`cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler` and will be used in following mappings.

If the mapping type handler is prepared, its usage is quite straightforward and similar to previous cases. Let's demonstrate
it on one case, other can be seen in `userDataObjectsMapper.xml`, `cz.sidik.demo.oraclemybatis.mapper.UserDataObjectsMapper`
and `cz.sidik.demo.oraclemybatis.UserDataObjectsTest`.

```java
public interface UserDataObjectsMapper {
    void insertEmployeesDao(@Param("query") GetEmployeesDao query);
}
```

```xml
<update id="insertEmployeesDao" statementType="CALLABLE">
    {CALL insert_employees(
        #{query.employees, jdbcType=ARRAY, mode=IN, typeHandler=cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler},
        #{query.departmentId, jdbcType=NUMERIC, mode=IN})}
</update>
```

This code demonstrates an usage of passing a collection of records as input parameters (Java collection of POJOs => 
Oracle array of object). The type handler is written to support both direction as **IN**put parameters so **OUT**put parameters.
   
So retrieving of record collection from DB looks very similar (and use the same previous rules for output parameters/return values):

```java
public interface UserDataObjectsMapper {
    void getEmployeesDao(@Param("query") GetEmployeesDao query);
}
```

```xml
<select id="getEmployeesDaoOut" statementType="CALLABLE">
        {CALL get_employees_array_out(#{query.departmentId, jdbcType=NUMERIC, mode=IN},
            #{query.employees, jdbcType=ARRAY, mode=OUT, jdbcTypeName=EMPLOYEE_ARRAYTYPE, typeHandler=cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler}) }
</select>
```

There is one unusual thing. In case of output parameters must be present `jdbcTypeName` parameter in MyBatis paramter inline
definition with the name of according user DB type (see DDL statement for this type).

### How to return a cursor as a result of Java method (Vol. 2)

Would be possible to return array or record type in `SELECT` statement like in the previous cases?

First approach is like the case with cursor:

```java
public interface UserDataObjectsMapper {
    GetEmployeesDao getEmployeesArray(@Param("departmentId") Long departmentId);
}
```

```xml
<resultMap id="getEmployeesArrayResultSet" type="cz.sidik.demo.oraclemybatis.bo.GetEmployeesDao">
    <result property="employees" column="ARRAY_TYPE" typeHandler="cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler"/>
</resultMap>
```

```xml
<select id="getEmployeesArray" resultMap="getEmployeesArrayResultSet">
    SELECT get_employees_array(#{departmentId, jdbcType=NUMERIC, mode=IN}) AS ARRAY_TYPE
    FROM dual
</select>
```

This approach returns single-column single-row result set, which contains value = collection of arrays. And this value
is transformed with help of custom type handler from previous attempts. Disadvantages remains:
* result collection is returned in some POJO container, not as standard collection
* it is processed in one big fetch and mapped as one big chunk of data - same as in previous attempts

Oracle offers one feature for array processing, it allows using arrays as `SELECT FROM` clause source with help of `TABLE` statement. So `SELECT` statement
returns standard result set directly and standard result set mapping can be used:
 
```java
public interface UserDataObjectsMapper {
    Collection<EmployeeDao> getEmployeesRows(@Param("departmentId") Long departmentId);
}
```
 
```xml
<resultMap id="getEmployeesRowsResultSet" type="cz.sidik.demo.oraclemybatis.bo.EmployeeDao">
    <result property="employeeId" column="employee_id" />
    <result property="firstName" column="first_name" />
    <result property="lastName" column="last_name" />
</resultMap>
```
 
```xml
<select id="getEmployeesRows" resultMap="getEmployeesRowsResultSet">
    SELECT *
    FROM TABLE(get_employees_array(#{departmentId, jdbcType=NUMERIC, mode=IN}))
</select>
```
 
So there is DB function returning array of records, array is selected just any other DB table and result set is mapped
to Java as any other MyBatis result set mapping. 

Supports this approach fetching rows by rows? MyBatis/JDBC cooperation supports step by step fetching now - just like
in any other select. But there is problem in Oracle function code. Oracle can not start processing of `SELECT` before
the content of array is completely populated, so all data from cursor must be loaded into DB engine before it will
be fetched by Java code. The reason is, that array (or cursor) is constructed and populated in PL/SQL engine, `SELECT` statement
execution is performed in SQL engine and Oracle must convert complete result set to switch between engines and continue in processing.

This is not ideal, but fortunately Oracle offers other feature: [PIPE ROW](https://docs.oracle.com/cloud/latest/db112/LNPLS/pipe_row_statement.htm)
which can be combined with previous `TABLE` statement:
 
```java
public interface UserDataObjectsMapper {
    Collection<EmployeeDao> getEmployeesPipedRows(@Param("departmentId") Long departmentId);
}
```

```xml
<select id="getEmployeesPipedRows" resultMap="getEmployeesRowsResultSet">
    SELECT *
    FROM TABLE(pipe_employees_cursor(get_employees(#{departmentId, jdbcType=NUMERIC, mode=IN})))
</select>
```

```sql
CREATE OR REPLACE FUNCTION pipe_employees_cursor(p_employees_cursor SYS_REFCURSOR)
  RETURN employee_arraytype PIPELINED IS
  l_employee_record employees%ROWTYPE;
  BEGIN
    LOOP
      FETCH p_employees_cursor INTO l_employee_record;
      EXIT WHEN p_employees_cursor%NOTFOUND;

      PIPE ROW (employee_structtype(l_employee_record.employee_id, l_employee_record.first_name,
                                    l_employee_record.last_name));
    END LOOP;

    RETURN;
  END;
```

The main difference is in called DB function `pipe_employees_cursor`. It does not return array or cursor, but it has 
a special syntax, which "PIPES" rows one by one. In this example it is only a wrapper between opened cursor, which fetchs a 
single record and pass it to SQL code one by one. So partially fetching works from PL/SQL `SELECT` cursor over SQL `SELECT`
statement over JDBC into MyBatis.

There is last limitation, MyBatis does not support some lazy fetch from statement. It convert all returned rows into Java
objects after all. This last demonstration is only for illustrating of Oracle DB possibilities, which can be effectively 
used only with direct JDBC communication now. Or may be in some future MyBatis releases. 

## Conclusion

And that is all I know about mapping between Oracle DB procedures and function with help of MyBatis :-)