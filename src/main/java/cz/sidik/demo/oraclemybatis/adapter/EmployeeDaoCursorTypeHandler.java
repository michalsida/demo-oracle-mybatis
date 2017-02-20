package cz.sidik.demo.oraclemybatis.adapter;

import cz.sidik.demo.oraclemybatis.bo.EmployeeDao;
import oracle.jdbc.OracleResultSet;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Custom type handler, which translates Oracle Cursor nested inside a SQL result set
 * to collection of target objects. Unfortunately I do not know syntax of MyBatis
 * result map, which would work the same way.
 * <p/>
 * Only variants returning data from SQL result set is supported, sending Cursor as a IN parameter
 * does not make a sence and retrieving Cursor from OUT parameter can be handled directly with
 * use of {@code resultMap} in MyBatis parameter mapping.
 */
public class EmployeeDaoCursorTypeHandler extends BaseTypeHandler<Collection<EmployeeDao>> {

    @Override
    public Collection<EmployeeDao> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getEmployeeDaos(((OracleResultSet) rs).getCursor(columnName));
    }

    @Override
    public Collection<EmployeeDao> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getEmployeeDaos(((OracleResultSet) rs).getCursor(columnIndex));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Collection<EmployeeDao> parameter, JdbcType jdbcType) throws SQLException {
        throw new RuntimeException("not-implemented");
    }

    @Override
    public Collection<EmployeeDao> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        throw new RuntimeException("not-implemented");
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
