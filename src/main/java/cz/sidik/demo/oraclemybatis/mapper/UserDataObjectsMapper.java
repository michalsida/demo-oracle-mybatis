package cz.sidik.demo.oraclemybatis.mapper;

import cz.sidik.demo.oraclemybatis.bo.EmployeeDao;
import cz.sidik.demo.oraclemybatis.bo.GetEmployeesDao;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Map;

/**
 * Demonstrates of mapping User Data Types - Oracle's object/record/array types defined by user.
 */
@MyBatisMapper
public interface UserDataObjectsMapper {

    String RESULT_SET_OUT_PARAM = "resultSet";

    /**
     * Demonstrate a call of procedure with input parameter of type array of objects. For value passing is used custom
     * MyBatis type handler {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler}, which can convert {@link EmployeeDao}
     * or collection of {@link EmployeeDao} into corresponding defined DB types {@code employee_structtype} and {@code employee_arraytype}.
     *
     * @param employees    is collection of employees data to insert
     * @param departmentId is ID of department, under which they should be stored
     */
    void insertEmployees(@Param("employees") Collection<EmployeeDao> employees, @Param("departmentId") Long departmentId);

    /**
     * The same demonstration as in {@link #insertEmployees(Collection, Long)}, but parameters are passed in one encapsulating
     * data structure.
     *
     * @param query contains collection of employees data to insert and ID of department, under which they should be stored
     */
    void insertEmployeesDao(@Param("query") GetEmployeesDao query);

    /**
     * Demonstrate a call to Oracle function, which is returning data in user collection of user objects.
     * Its return value is converted to collection of Dao objects and inserted into passed map.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     * <p/>
     * For value retrieving is used MyBatis type handler {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler},
     * which converts DB array type {@code employee_arraytype} of DB struct type {@code employee_structtype} into
     * corresponding Java POJO {@link EmployeeDao}.
     *
     * @param departmentId     is ID of department
     * @param outputParameters is map where collection of employee records is stored under key {@link #RESULT_SET_OUT_PARAM}
     */
    void getEmployees(@Param("departmentId") Long departmentId, @Param("outParams") Map<String, Collection<EmployeeDao>> outputParameters);

    /**
     * Demonstrate a call to Oracle function, whose return value is inserted back into passed DAO object.
     * Its return value is array of struct type.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     * <p/>
     * For value retrieving is used MyBatis type handler {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler},
     * which converts DB array type {@code employee_arraytype} of DB struct type {@code employee_structtype} into
     * corresponding Java POJO {@link EmployeeDao}.
     *
     * @param query is DAO object with asked department ID. Employees property of this object will be
     *              updated with employee records after this call.
     */
    void getEmployeesDao(@Param("query") GetEmployeesDao query);

    /**
     * Demonstrate a call to Oracle procedure, whose output parameter value is inserted into passed map.
     * Output parameter value is array of struct type.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     * <p/>
     * For value retrieving is used MyBatis type handler {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler},
     * which converts DB array type {@code employee_arraytype} of DB struct type {@code employee_structtype} into
     * corresponding Java POJO {@link EmployeeDao}.
     *
     * @param departmentId     is ID of department
     * @param outputParameters is map where collection of employee records is stored under key {@link #RESULT_SET_OUT_PARAM}
     */
    void getEmployeesOut(@Param("departmentId") Long departmentId, @Param("outParams") Map<String, Collection<EmployeeDao>> outputParameters);

    /**
     * Demonstrate a call to Oracle procedure, whose output parameter value is inserted back into passed DAO object.
     * Output parameter value is array of struct type.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     * <p/>
     * For value retrieving is used MyBatis type handler {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler},
     * which converts DB array type {@code employee_arraytype} of DB struct type {@code employee_structtype} into
     * corresponding Java POJO {@link EmployeeDao}.
     *
     * @param query is DAO object with asked department ID. Employees property of this object will be
     *              updated with employee records after this call .
     */
    void getEmployeesDaoOut(@Param("query") GetEmployeesDao query);

    /**
     * Demonstrate a call to Oracle function, which does not call any DML statement and which
     * has not any output parameters. This function can be called in {@code SELECT}, so its return value
     * can be return as Java function result.
     * <p/>
     * The function returns a single-row result set with one column containing DB User Data Type -
     * array {@code employee_arraytype} of structs {@code employee_structtype}. Custom type handler
     * {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoTypeHandler} is used for converting to collection
     * of {@link EmployeeDao}. But in this case can not be returned this collection directly as result of mapper function,
     * but it must be returned as some property of returned single POJO, {@link GetEmployeesDao} is used for this purpose.
     * <p/>
     * Disadvantage: the function result is computed completely at first and passed into select as one big batch after that.
     *
     * @param departmentId is ID of department
     * @return DAO object with filled {@link GetEmployeesDao#employees} field by employees collection retrieved from
     * returned collection type
     */
    GetEmployeesDao getEmployeesArray(@Param("departmentId") Long departmentId);

    /**
     * Demonstrate a call to Oracle function, which does not call any DML statement and which
     * has not any output parameters. This function can be called in {@code FROM} clause of {@code SELECT}
     * with use of Oracle {@code TABLE} function. So the returned collection is table source for the {@code SELECT}
     * command and the result set can be processed by the standard {@code SELECT} mapping.
     * <p/>
     * Disadvantage: the function result is computed completely at first and passed into select as one big batch after that,
     * so all rows are computed even {@code SELECT} would limit their amount.
     *
     * @param departmentId is ID of department
     * @return Employees collection retrieved from returned collection type
     */
    Collection<EmployeeDao> getEmployeesRows(@Param("departmentId") Long departmentId);

    /**
     * Returns the same data as {@link #getEmployeesRows(Long)} or {@link CursorMapper#getEmployeesCursor(Long)}.
     * It uses the cursor from {@link CursorMapper#getEmployeesCursor(Long)} and use a Oracle's feature {@code PIPE ROW}
     * with use of {@code TABLE} function in {@code SELECT}. So the mapping is as easy as in case of {@link #getEmployeesRows(Long)}
     * and processing of SQL {@code SELECT} goes row by row as in case of {@link CursorMapper#getEmployeesCursor(Long)}.
     *
     * @param departmentId is ID of department
     * @return Employees collection retrieved from returned cursor converted into {@code PIPED} table source
     */
    Collection<EmployeeDao> getEmployeesPipedRows(@Param("departmentId") Long departmentId);

}