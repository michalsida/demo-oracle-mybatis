package cz.sidik.demo.oraclemybatis.mapper;

import cz.sidik.demo.oraclemybatis.bo.EmployeeOutDao;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * Demonstration of mapping functions and procedures with output parameters
 */
@MyBatisMapper
public interface OutputParamMapper {

    String OLD_NAME_OUT_PARAM = "oldName";

    /**
     * Demonstrate a call to Oracle function, whose return value is inserted into passed map.
     * <p/>
     * It update first name and last name of employee in HR.EMPLOYEES table.
     *
     * @param employeeId       is ID of employee
     * @param firstName        is a new first name of employee
     * @param lastName         is a new last name of employee
     * @param outputParameters is map which is stored old first name concatenated with old last name under key {@link #OLD_NAME_OUT_PARAM}
     */
    void setEmployeeName(@Param("employeeId") Long employeeId, @Param("firstName") String firstName, @Param("lastName") String lastName, @Param("outParams") Map<String, String> outputParameters);

    /**
     * Demonstrate a call to Oracle function, whose return value is inserted back into passed DAO object.
     * <p/>
     * It update first name and last name of employee in HR.EMPLOYEES table.
     *
     * @param employee is DAO object with employee ID and new first and last name. OldName property of this object will be
     *                 updated with old first name concatenated with old last name value after this call .
     */
    void setEmployeeNameDao(@Param("employee") EmployeeOutDao employee);

    /**
     * Demonstrate a call to Oracle procedure, whose output parameter value is inserted into passed map.
     * <p/>
     * It update first name and last name of employee in HR.EMPLOYEES table.
     *
     * @param employeeId       is ID of employee
     * @param firstName        is a new first name of employee
     * @param lastName         is a new last name of employee
     * @param outputParameters is map which is stored old first name concatenated with old last name under key {@link #OLD_NAME_OUT_PARAM}
     */
    void setEmployeeNameOut(@Param("employeeId") Long employeeId, @Param("firstName") String firstName, @Param("lastName") String lastName, @Param("outParams") Map<String, String> outputParameters);

    /**
     * Demonstrate a call to Oracle procedure, whose output parameter value is inserted into passed DAO object.
     * <p/>
     * It update first name and last name of employee in HR.EMPLOYEES table.
     *
     * @param employee is DAO object with employee ID and new first and last name. OldName property of this object will be
     *                 updated with old first name concatenated with old last name value after this call .
     */
    void setEmployeeNameDaoOut(@Param("employee") EmployeeOutDao employee);

    /**
     * Demonstrate a call to Oracle function, which does not call any DML statement and which
     * has not any output parameters. This function can be called in select, so its return value
     * can be return as Java function result.
     * <p/>
     * It gets first name and last name of employee in HR.EMPLOYEES table.
     *
     * @param employeeId is ID of employee
     * @return actual first name concatenated with last name
     */
    String getEmployeeName(@Param("employeeId") Long employeeId);

}