package xlike.top.nettydemo.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import xlike.top.nettydemo.enums.MessageType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Administrator
 */
@MappedTypes(MessageType.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class MessageTypeHandler extends BaseTypeHandler<MessageType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MessageType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public MessageType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Integer code = rs.getObject(columnName, Integer.class);
        return code == null ? null : getMessageTypeByCode(code);
    }

    @Override
    public MessageType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer code = rs.getObject(columnIndex, Integer.class);
        return code == null ? null : getMessageTypeByCode(code);
    }

    @Override
    public MessageType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Integer code = cs.getObject(columnIndex, Integer.class);
        return code == null ? null : getMessageTypeByCode(code);
    }

    private MessageType getMessageTypeByCode(Integer code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
