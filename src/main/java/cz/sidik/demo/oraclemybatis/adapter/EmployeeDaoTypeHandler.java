package cz.sidik.demo.oraclemybatis.adapter;

import cz.sidik.demo.oraclemybatis.bo.EmployeeDao;
import oracle.jdbc.OracleConnection;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Handles Collection<EmployeeDao> or EmployeeDao into according Oracle object/array types
 * EMPLOYEE_ARRAYTYPE and EMPLOYEE_STRUCTTYPE and vice versa.
 */
@SuppressWarnings("unused")
public class EmployeeDaoTypeHandler extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        Connection connection = ps.getConnection();
        if (parameter instanceof Collection) {
            Collection objects = (Collection) parameter;
            Collection<Struct> oracleStructs = new ArrayList<>(objects.size());

            for (Object object : objects) {
                // Map POJO to DB Struct type
                if (object instanceof EmployeeDao) {
                    oracleStructs.add(convertToDbStruct(connection, (EmployeeDao) object));
                }
            }

            // Map array of DB Struct type to DB Array type
            ps.setObject(i, ((OracleConnection) connection).createARRAY("EMPLOYEE_ARRAYTYPE", oracleStructs.toArray()));

            // This can not be used, throws SQLFeatureNotSupportedException in oracle.jdbc.driver.PhysicalConnection#createArrayOf
            //  ps.setObject(i, connection.createArrayOf("EMPLOYEE_ARRAYTYPE", oracleStructs.toArray()));
        } else if (parameter instanceof EmployeeDao) {
            // Map POJO to DB Struct type
            ps.setObject(i, convertToDbStruct(connection, (EmployeeDao) parameter), Types.STRUCT);
        } else {
            throw new SQLException("Unsupported parameter type: " + parameter.getClass().getCanonicalName()
                    + " for handler " + this.getClass().getCanonicalName());
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertSqlResult(rs.getObject(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertSqlResult(rs.getObject(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertSqlResult(cs.getObject(columnIndex));
    }

    private Object convertSqlResult(Object sqlResult) throws SQLException {
        if (sqlResult instanceof Array) {
            // Converts JDBC Array into collection of target POJOs
            Object[] structs = (Object[]) ((Array) sqlResult).getArray();
            Collection<EmployeeDao> result = new ArrayList<>(structs.length);
            for (Object struct : structs) {
                // Convert JDBC Struct into target POJO
                result.add(convertToEmployeeDao((Struct) struct));
            }
            return result;
        } else if (sqlResult instanceof Struct) {
            // Convert JDBC Struct into target POJO
            return convertToEmployeeDao((Struct) sqlResult);
        } else {
            throw new SQLException("Unsupported parameter type: " + sqlResult.getClass().getCanonicalName()
                    + " for handler " + this.getClass().getCanonicalName());
        }
    }

    private Struct convertToDbStruct(Connection connection, EmployeeDao object) throws SQLException {
        Object[] structFields = new Object[]{object.employeeId, object.firstName, object.lastName};
        return connection.createStruct("EMPLOYEE_STRUCTTYPE", structFields);
    }

    private EmployeeDao convertToEmployeeDao(Struct struct) throws SQLException {
        EmployeeDao result = new EmployeeDao();
        Object[] attributes = struct.getAttributes();
        result.employeeId = ((BigDecimal) attributes[0]).longValue();
        result.firstName = (String) attributes[1];
        result.lastName = (String) attributes[2];
        return result;
    }

}