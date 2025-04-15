package kz.talgat.daos;

import java.sql.Connection;
import java.sql.SQLException;

public interface WithConnection {
    void execute(Connection var1) throws SQLException, Exception;
}
