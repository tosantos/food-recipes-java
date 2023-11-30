package org.olivetree.recipes.repository.exception;

import java.sql.SQLException;

public class RepositoryException extends RuntimeException {
    public RepositoryException(String msg, SQLException e) {
        super(msg, e);
    }
}
