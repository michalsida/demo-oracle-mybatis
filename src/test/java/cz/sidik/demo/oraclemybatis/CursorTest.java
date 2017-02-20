package cz.sidik.demo.oraclemybatis;

import cz.sidik.demo.oraclemybatis.bo.EmployeeDao;
import cz.sidik.demo.oraclemybatis.bo.GetEmployeesDao;
import cz.sidik.demo.oraclemybatis.mapper.CursorMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CursorTest extends BaseMapperTest {

    static final long IT_DEPARTMENT = 60L;

    @Autowired
    private CursorMapper cursorMapper;

    @Test
    public void testGetClients() {
        Map<String, Collection<EmployeeDao>> output = new HashMap<>();
        cursorMapper.getEmployees(IT_DEPARTMENT, output);
        Collection<EmployeeDao> resultSet = output.get(CursorMapper.RESULT_SET_OUT_PARAM);
        testEmployeeResultSet(resultSet);
    }

    @Test
    public void testGetClientsDao() {
        GetEmployeesDao query = new GetEmployeesDao();
        query.departmentId = IT_DEPARTMENT;
        cursorMapper.getEmployeesDao(query);
        testEmployeeResultSet(query.employees);
    }

    @Test
    public void testGetClientsOut() {
        Map<String, Collection<EmployeeDao>> output = new HashMap<>();
        cursorMapper.getEmployeesOut(IT_DEPARTMENT, output);
        Collection<EmployeeDao> resultSet = output.get(CursorMapper.RESULT_SET_OUT_PARAM);
        testEmployeeResultSet(resultSet);
    }

    @Test
    public void testGetClientsDaoOut() {
        GetEmployeesDao query = new GetEmployeesDao();
        query.departmentId = IT_DEPARTMENT;
        cursorMapper.getEmployeesDaoOut(query);
        testEmployeeResultSet(query.employees);
    }

    @Test
    public void testGetClientsCursor() {
        GetEmployeesDao employees = cursorMapper.getEmployeesCursor(IT_DEPARTMENT);
        testEmployeeResultSet(employees.employees);
    }

    static void testEmployeeResultSet(Collection<EmployeeDao> resultSet) {
        Assert.assertNotNull(resultSet);
        Assert.assertEquals(5, resultSet.size());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.employeeId == 103 &&
                        employeeDao.firstName.equals("Alexander") && employeeDao.lastName.equals("Hunold"))
                .findAny().isPresent());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.employeeId == 104 &&
                        employeeDao.firstName.equals("Bruce") && employeeDao.lastName.equals("Ernst"))
                .findAny().isPresent());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.employeeId == 105 &&
                        employeeDao.firstName.equals("David") && employeeDao.lastName.equals("Austin"))
                .findAny().isPresent());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.employeeId == 106 &&
                        employeeDao.firstName.equals("Valli") && employeeDao.lastName.equals("Pataballa"))
                .findAny().isPresent());

        Assert.assertTrue(resultSet.stream()
                .filter(employeeDao -> employeeDao.employeeId == 107 &&
                        employeeDao.firstName.equals("Diana") && employeeDao.lastName.equals("Lorentz"))
                .findAny().isPresent());
    }

}
