package com.epam.eventhub.repository.jdbc;

import com.epam.eventhub.exception.DataAccessException;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.model.User;
import com.epam.eventhub.repository.UserRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
@Repository
public class JdbcUserRepository implements UserRepository {

    private final DataSource dataSource;
    public JdbcUserRepository(DataSource dataSource){
        this.dataSource = dataSource;
    }
    @Override
    public Optional<User> findById(long id){
        String sql = """
                SELECT id, full_name, email, password_hash, role
                FROM users
                WHERE id = ?
                """;

        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setLong(1, id);
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()){
                        return Optional.of(new User(
                                 resultSet.getLong("id"),
                                 resultSet.getString("full_name"),
                                 resultSet.getString("email"),
                                 resultSet.getString("password_hash"),
                                 Role.valueOf(resultSet.getString("role"))
                        ));
                    }
                    return Optional.empty();
                }
            }


        }catch(SQLException exception){
            throw new DataAccessResourceFailureException("Can't find User with id " + id, exception);
        }
    }

    @Override
    public Optional<User> findByEmail(String email){
        String sql = """
                SELECT id, full_name, email, password_hash, role
                FROM users
                WHERE email = ?
        """;
        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, email);
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()){
                        return Optional.of(new User(
                                resultSet.getLong("id"),
                                resultSet.getString("full_name"),
                                resultSet.getString("email"),
                                resultSet.getString("password_hash"),
                                Role.valueOf(resultSet.getString("role"))
                        ));
                    }

                    return Optional.empty();
                }

            }
        }catch(SQLException exception){
            throw new DataAccessResourceFailureException("Can't find User with email " + email, exception);
        }


    }

    @Override
    public boolean existsByEmail(String email){
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM users WHERE email = ?
                    )
                """;
        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, email);
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()){
                        return resultSet.getBoolean(1);
                    }
                    return false;
                }
            }
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to check user email", e);
        }
    }

    @Override
    public User save(User user){
        if(user.getId() != null){
            throw new UnsupportedOperationException("Updating existing users is not implemented yet");
        }

        String sql = """
                INSERT INTO users (id, full_name, email, password_hash, role)
                VALUES (?, ?, ?, ?, ?)
                """;

        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
                statement.setLong(1, user.getId());
                statement.setString(2, user.getFullName());
                statement.setString(3, user.getEmail());
                statement.setString(4, user.getPasswordHash());
                statement.setObject(5, user.getRole());
                statement.executeUpdate();

                try(ResultSet resultSet  = statement.getGeneratedKeys()){
                    if(resultSet.next()){
                        user.setId(resultSet.getLong(1));
                        return user;
                    }
                    throw new SQLException("No generated user ID returned");
                }
            }

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to save user ", e);
        }


    }
}
