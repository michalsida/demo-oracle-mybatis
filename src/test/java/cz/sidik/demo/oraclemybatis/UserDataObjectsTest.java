package cz.sidik.demo.oraclemybatis;

import cz.sidik.demo.oraclemybatis.bo.EmployeeDao;
import cz.sidik.demo.oraclemybatis.bo.GetEmployeesDao;
import cz.sidik.demo.oraclemybatis.mapper.UserDataObjectsMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserDataObjectsTest extends BaseMapperTest {

    public static final long OPERATIONS_DEPARTMENT = 200L;

    @Autowired
    private UserDataObjectsMapper userDataObjectsMapper;

    @Test
    public void testInsertEmployees() {
        Collection<EmployeeDao> employees = prepareNewEmployees();
        userDataObjectsMapper.insertEmployees(employees, OPERATIONS_DEPARTMENT);

        Map<String, Collection<EmployeeDao>> outParams = new HashMap<>();
        userDataObjectsMapper.getEmployees(OPERATIONS_DEPARTMENT, outParams);
        testInsertedEmployeeResultSet(outParams.get(UserDataObjectsMapper.RESULT_SET_OUT_PARAM));
    }

    @Test
    public void testInsertEmployeesDao() {
        GetEmployeesDao insertQuery = new GetEmployeesDao();
        insertQuery.departmentId = OPERATIONS_DEPARTMENT;
        insertQuery.employees = prepareNewEmployees();
        userDataObjectsMapper.insertEmployeesDao(insertQuery);

        GetEmployeesDao getQuery = new GetEmployeesDao();
        getQuery.departmentId = OPERATIONS_DEPARTMENT;
        userDataObjectsMapper.getEmployeesDao(getQuery);
        testInsertedEmployeeResultSet(getQuery.employees);
    }

    @Test
    public void testInsertEmployeesOut() {
        Collection<EmployeeDao> employees = prepareNewEmployees();
        userDataObjectsMapper.insertEmployees(employees, OPERATIONS_DEPARTMENT);

        Map<String, Collection<EmployeeDao>> outParams = new HashMap<>();
        userDataObjectsMapper.getEmployeesOut(OPERATIONS_DEPARTMENT, outParams);
        testInsertedEmployeeResultSet(outParams.get(UserDataObjectsMapper.RESULT_SET_OUT_PARAM));
    }

    @Test
    public void testInsertEmployeesDaoOut() {
        GetEmployeesDao insertQuery = new GetEmployeesDao();
        insertQuery.departmentId = OPERATIONS_DEPARTMENT;
        insertQuery.employees = prepareNewEmployees();
        userDataObjectsMapper.insertEmployeesDao(insertQuery);

        GetEmployeesDao getQuery = new GetEmployeesDao();
        getQuery.departmentId = OPERATIONS_DEPARTMENT;
        userDataObjectsMapper.getEmployeesDaoOut(getQuery);
        testInsertedEmployeeResultSet(getQuery.employees);
    }

    @Test
    public void testGetEmployeesArray() {
        GetEmployeesDao query = userDataObjectsMapper.getEmployeesArray(CursorTest.IT_DEPARTMENT);
        CursorTest.testEmployeeResultSet(query.employees);
    }

    @Test
    public void testGetEmployeesRows() {
        Collection<EmployeeDao> employees = userDataObjectsMapper.getEmployeesRows(CursorTest.IT_DEPARTMENT);
        CursorTest.testEmployeeResultSet(employees);
    }

    @Test
    public void testGetEmployeesPipedRows() {
        Collection<EmployeeDao> employees = userDataObjectsMapper.getEmployeesPipedRows(CursorTest.IT_DEPARTMENT);
        CursorTest.testEmployeeResultSet(employees);
    }

    static Collection<EmployeeDao> prepareNewEmployees() {
        Collection<EmployeeDao> employees = new ArrayList<>();
        EmployeeDao employeeDao = new EmployeeDao();
        employeeDao.firstName = "Bob";
        employeeDao.lastName = "Marley";
        employees.add(employeeDao);
        employeeDao = new EmployeeDao();
        employeeDao.firstName = "Jimi";
        employeeDao.lastName = "Hendrix";
        employees.add(employeeDao);
        employeeDao = new EmployeeDao();
        employeeDao.firstName = "Kurt";
        employeeDao.lastName = "Cobain";
        employees.add(employeeDao);
        return employees;
    }

    static void testInsertedEmployeeResultSet(Collection<EmployeeDao> resultSet) {
        Assert.assertNotNull(resultSet);
        Assert.assertEquals(3, resultSet.size());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.firstName.equals("Bob") && employeeDao.lastName.equals("Marley"))
                .findAny().isPresent());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.firstName.equals("Jimi") && employeeDao.lastName.equals("Hendrix"))
                .findAny().isPresent());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.firstName.equals("Kurt") && employeeDao.lastName.equals("Cobain"))
                .findAny().isPresent());

    }
}
