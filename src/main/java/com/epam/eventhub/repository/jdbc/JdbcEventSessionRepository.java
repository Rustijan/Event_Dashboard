package com.epam.eventhub.repository.jdbc;

import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.model.EventSession;
import com.epam.eventhub.repository.EventSessionRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
@Repository
public class JdbcEventSessionRepository implements EventSessionRepository {

    private final DataSource dataSource;

    public JdbcEventSessionRepository(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public Optional<EventSession> findById(long id){
        String sql = """
                SELECT s.id, s.event_id, s.starts_at , ends_at, capacity,
                    (SELECT  COUNT(*)
                     FROM registrations AS r
                      WHERE s.id = r.id 
                      AND r.status = 'ACTIVE') AS activeRegistrations
                FROM event_sessions AS s 
                WHERE s.id = ?; 
                            
                            
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(new EventSession(
                            resultSet.getLong("id"),
                            resultSet.getLong("event_id"),
                            resultSet.getTimestamp("starts_at").toLocalDateTime(),
                            resultSet.getTimestamp("ends_at").toLocalDateTime(),
                            resultSet.getInt("capacity"),
                            resultSet.getInt("activeRegistrations")
                    ));
                }
                return Optional.empty();
            }

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to find a session with ID" + id, e);
        }
    }

    @Override
    public List<EventSession> findByEventId(long eventId){
        String sql = """
                SELECT s.id, s.event_id, s.starts_at , ends_at, capacity,
                    (SELECT  COUNT(*)
                     FROM registrations AS r
                      WHERE s.id = r.id 
                         AND r.status = 'ACTIVE') AS activeRegistrations
                FROM event_sessions AS s 
                WHERE s.event_id = ?
                ORDER BY s.starts_at 
                """;
        List<EventSession> sessions= new ArrayList<>();
        try(Connection connection= dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, eventId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    sessions.add(new EventSession(
                            resultSet.getLong("id"),
                            resultSet.getLong("event_id"),
                            resultSet.getTimestamp("starts_at").toLocalDateTime(),
                            resultSet.getTimestamp("ends_at").toLocalDateTime(),
                            resultSet.getInt("capacity"),
                            resultSet.getInt("activeRegistrations")
                    ));
                }
                return sessions;

            }
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to find a session with Event_ID" + eventId, e);
        }
    }


    @Override
    public EventSession save(long eventId, SessionForm form){
        String sql  = """
                INSERT INTO event_sessions(event_id, starts_at, ends_at, capacity)
                VALUES (?, ?, ?, ?)
                
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            statement.setLong(1, eventId);
            statement.setTimestamp(2,  Timestamp.valueOf(form.getStartsAt()));
            statement.setTimestamp(3, Timestamp.valueOf(form.getEndsAt()));
            statement.setInt(4, form.getCapacity());
            statement.executeUpdate();

            try(ResultSet rs = statement.getGeneratedKeys()){
                if(rs.next()){
                    long id = rs.getLong(1);
                    return findById(id).orElseThrow(
                            () -> new IllegalArgumentException("No event session with id " + id + " exists")

                    );

                }


            }
            throw new SQLException("No generated session ID returned");

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to save session with Event_ID" + eventId, e);
        }


    }
}
