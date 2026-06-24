package cn.edu.bjfu.nekocafe.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler：将 Java String/Object 与 PostgreSQL jsonb 类型互转。
 * <p>
 * 注册后，在 Mapper XML 中对 jsonb 列使用
 * {@code typeHandler=cn.edu.bjfu.nekocafe.config.JsonbTypeHandler}
 * 即可自动完成 CAST，无需在 SQL 中手写 ::jsonb。
 * </p>
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(Object.class)
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final String JSONB_TYPE = "jsonb";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject pg = new PGobject();
        pg.setType(JSONB_TYPE);
        pg.setValue(parameter.toString());
        ps.setObject(i, pg);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        PGobject pg = (PGobject) rs.getObject(columnName);
        return pg == null ? null : pg.getValue();
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        PGobject pg = (PGobject) rs.getObject(columnIndex);
        return pg == null ? null : pg.getValue();
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        PGobject pg = (PGobject) cs.getObject(columnIndex);
        return pg == null ? null : pg.getValue();
    }
}
