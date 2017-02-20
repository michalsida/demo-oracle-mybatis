package cz.sidik.demo.oraclemybatis;

import cz.sidik.demo.oraclemybatis.bo.EmployeeOutDao;
import cz.sidik.demo.oraclemybatis.mapper.OutputParamMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class OutputParamTest extends BaseMapperTest {

    @Autowired
    private OutputParamMapper outputParamMapper;


    @Test
    public void testSetEmployeeName() {
        Map<String, String> output = new HashMap<>();
        outputParamMapper.setEmployeeName(100L, "JRR", "Tolkien", output);
        Assert.assertNotNull(output.get(OutputParamMapper.OLD_NAME_OUT_PARAM));
        Assert.assertTrue(output.get(OutputParamMapper.OLD_NAME_OUT_PARAM) instanceof String);
        Assert.assertEquals("Steven King", output.get(OutputParamMapper.OLD_NAME_OUT_PARAM));
    }

    @Test
    public void testSetEmployeeNameDao() {
        EmployeeOutDao employee = new EmployeeOutDao();
        employee.employeeId = 100L;
        employee.firstName = "JRR";
        employee.lastName = "Tolkien";
        outputParamMapper.setEmployeeNameDao(employee);
        Assert.assertEquals("Steven King", employee.oldName);
    }

    @Test
    public void testSetEmployeeNameOut() {
        Map<String, String> output = new HashMap<>();
        outputParamMapper.setEmployeeNameOut(100L, "JRR", "Tolkien", output);
        Assert.assertNotNull(output.get(OutputParamMapper.OLD_NAME_OUT_PARAM));
        Assert.assertTrue(output.get(OutputParamMapper.OLD_NAME_OUT_PARAM) instanceof String);
        Assert.assertEquals("Steven King", output.get(OutputParamMapper.OLD_NAME_OUT_PARAM));
    }

    @Test
    public void testSetEmployeeNameDaoOut() {
        EmployeeOutDao employee = new EmployeeOutDao();
        employee.employeeId = 100L;
        employee.firstName = "JRR";
        employee.lastName = "Tolkien";
        outputParamMapper.setEmployeeNameDaoOut(employee);
        Assert.assertEquals("Steven King", employee.oldName);
    }

    @Test
    public void testGetEmployeeName() {
        String oldName = outputParamMapper.getEmployeeName(100L);
        Assert.assertEquals("Steven King", oldName);
    }

}
