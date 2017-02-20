package cz.sidik.demo.oraclemybatis.mapper;

import cz.sidik.demo.oraclemybatis.bo.EmployeeDao;
import cz.sidik.demo.oraclemybatis.bo.GetEmployeesDao;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Map;

/**
 * Demonstration of mapping functions and procedures with result/output parameters of type
 * SYS_REFCURSOR, which represents a select-like result set.
 */
@MyBatisMapper
public interface CursorMapper {

    String RESULT_SET_OUT_PARAM = "resultSet";

    /**
     * Demonstrate a call to Oracle function, which is returning SYS REFCURSOR with select-like result set.
     * Its return value is converted to collection of Dao objects and inserted into passed map.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     *
     * @param departmentId     is ID of department
     * @param outputParameters is map where collection of employee records is stored under key {@link #RESULT_SET_OUT_PARAM}
     */
    void getEmployees(@Param("departmentId") Long departmentId, @Param("outParams") Map<String, Collection<EmployeeDao>> outputParameters);

    /**
     * Demonstrate a call to Oracle function, whose return value is inserted back into passed DAO object.
     * Return value is SYS REFCURSOR with select-like result set converted to collection of Dao objects.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     *
     * @param getClientsQuery is DAO object with asked department ID. Employees property of this object will be
     *                        updated with employee records after this call .
     */
    void getEmployeesDao(@Param("getClientsQuery") GetEmployeesDao getClientsQuery);

    /**
     * Demonstrate a call to Oracle procedure, whose output parameter value is inserted into passed map.
     * Output parameter value is SYS REFCURSOR with select-like result set, is converted to collection of Dao objects
     * and inserted into passed map.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     *
     * @param departmentId     is ID of department
     * @param outputParameters is map where collection of employee records is stored under key {@link #RESULT_SET_OUT_PARAM}
     */
    void getEmployeesOut(@Param("departmentId") Long departmentId, @Param("outParams") Map<String, Collection<EmployeeDao>> outputParameters);

    /**
     * Demonstrate a call to Oracle procedure, whose output parameter value is inserted back into passed DAO object.
     * Output parameter value is SYS REFCURSOR with select-like result set and is converted to collection of Dao objects.
     * <p/>
     * It returns an employee collection from the specified department in HR.EMPLOYEES table.
     *
     * @param getClientsQuery is DAO object with asked department ID. Employees property of this object will be
     *                        updated with employee records after this call.
     */
    void getEmployeesDaoOut(@Param("getClientsQuery") GetEmployeesDao getClientsQuery);

    /**
     * Demonstrate a call to Oracle function, which does not call any DML statement and which
     * has not any output parameters. This function can be called in select, so its return value
     * can be return as Java function result.
     * <p/>
     * The function returns a single-row result set with one column containing another DB cursor. I do not know any
     * procedure which would transforms this cursor to POJO with use of MyBatis result maps. So a custom
     * type handler {@link cz.sidik.demo.oraclemybatis.adapter.EmployeeDaoCursorTypeHandler} is used
     * to manual transformation of nested cursor collection.
     * <p/>
     * Next way, how to process data from cursor is demonstrated in {@link UserDataObjectsMapper#getEmployeesPipedRows(Long)}.
     *
     * @param departmentId is ID of department
     * @return DAO object with filled {@link GetEmployeesDao#employees} field by employees collection retrieved from
     * returned cursor
     */
    GetEmployeesDao getEmployeesCursor(@Param("departmentId") Long departmentId);

}