package db.hibernate;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.type.StandardBasicTypes;

/**
 * Custom Hibernate Dialect since Sql Server Driver does not have a mapping for NVARCHAR values.
 * 
 * @author Scott Smith
 *
 */
public class SqlServerDriverTypeExtension extends SQLServerDialect {

    public SqlServerDriverTypeExtension() {
        super();
        registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());
        registerHibernateType(Types.LONGVARCHAR, StandardBasicTypes.TEXT.getName());
    }
}
