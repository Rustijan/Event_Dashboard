package com.epam.eventhub.repository.jdbc;

import com.epam.eventhub.model.Registration;
import com.epam.eventhub.model.RegistrationStatus;
import com.epam.eventhub.repository.RegistrationRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
@Repository
public class JdbcRegistrationRepository implements RegistrationRepository {
    private final DataSource dataSource;
    public JdbcRegistrationRepository(DataSource dataSource){
        this.dataSource = dataSource;
    }

    private static final String BASE = """
            SELECT r.id,
                   r.status,
                   r.registered_at,
                   r.session_id,
                   r.participant_id,
                   u.full_name AS participant_name,
                   u.email AS participant_email,
                   e.title AS event_title,
                   s.starts_at AS session_start_time
            FROM registrations AS r
            JOIN users AS u ON r.participant_id = u.id
            JOIN event_sessions AS s ON r.session_id = s.id
            JOIN events AS e ON s.event_id = e.id
            
         
            """;

    private Registration mapper(ResultSet rs) throws SQLException {
        return new Registration(
                rs.getLong("id"),
                RegistrationStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("registered_at").toLocalDateTime(),
                rs.getLong("session_id"),
                rs.getLong("participant_id"),
                rs.getString("participant_name"),
                rs.getString("participant_email"),
                rs.getString("event_title"),
                rs.getTimestamp("session_start_time").toLocalDateTime()
        );
    }

    @Override
    public Optional<Registration> findById(long id){
        String sql = BASE + "WHERE r.id = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            try(ResultSet resultSet  = statement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(mapper(resultSet));
                }
                return Optional.empty();
            }

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't find registration with id " + id, e);
        }

    }


    @Override
    public Optional<Registration> findBySessionAndParticipant(long sessionId, long participantId){
        String sql = BASE + "WHERE r.session_id = ?  AND r.participant_id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, sessionId);
            statement.setLong(2, participantId);
            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapper(rs));
                }
                return Optional.empty();
            }
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't find registration with session id " +
                    sessionId + " and participant " + participantId, e);
        }
    }
    @Override
    public int countActiveBySessionId(long sessionId){
        String sql = """
                SELECT COUNT(*)
                 FROM registrations
                  WHERE session_id = ? AND status ='ACTIVE'
               
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, sessionId);
            try(ResultSet rs = statement.executeQuery()){
                rs.next();
                return rs.getInt(1);
            }
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't count active registrations with session id " + sessionId, e);
        }
    }

    @Override
    public Registration create(long sessionId, long participantId){
        String sql = """
                INSERT INTO registrations (session_id, participant_id, status)
                 VALUES (?, ?, 'ACTIVE')
                """;
        long generatedId;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)){

            statement.setLong(1, sessionId);
            statement.setLong(2, participantId);
            statement.executeUpdate();

            try(ResultSet rs = statement.getGeneratedKeys()){
                if(rs.next()){
                     generatedId = rs.getLong(1);

                }else{
                    throw new SQLException("No generated registration id returned");

                }


            }

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't create registration with id " + sessionId, e);
        }
        return findById(generatedId).orElseThrow(
                () -> new IllegalArgumentException("No registration with id " + generatedId)
        );
    }
    @Override
    public void reactivate(long registrationId){

        String sql = """
                UPDATE registrations
                SET status = 'ACTIVE',
                    registered_at = CURRENT_TIMESTAMP,
                    cancelled_at = NULL
                WHERE id = ? AND status = 'CANCELLED'
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, registrationId);

            statement.executeUpdate();
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't reactivate registration with id "
                    + registrationId, e);
        }

    }
    @Override
    public void cancel(long registrationId){
        String sql = """
                UPDATE registrations
                SET status = 'CANCELLED',
                cancelled_at = CURRENT_TIMESTAMP
                WHERE id = ? AND 
                status = 'ACTIVE'
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, registrationId);
            statement.executeUpdate();

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't cancel registration with id "
                    + registrationId, e);
        }
    }
    @Override
    public void cancelActiveByEventId(long eventId){
        String sql = """
                UPDATE registrations
                SET status = 'CANCELLED',
                cancelled_at = CURRENT_TIMESTAMP
                WHERE status = 'ACTIVE' AND session_id IN(
                    SELECT id
                    FROM event_sessions
                    WHERE event_id = ?
                ) 
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, eventId);
            statement.executeUpdate();

        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't cancel registration for event " + eventId, e);
        }
    }
    @Override

    public List<Registration> findByParticipantId(long participantId){
        String sql = BASE + "WHERE r.participant_id = ? ORDER BY r.registered_at DESC";
        List<Registration> registrations = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, participantId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    registrations.add(mapper(resultSet));
                }
                return registrations;
            }
            }catch(SQLException e){
                throw new DataAccessResourceFailureException("Can't find registrations with id " + participantId, e);
        }
    }
    @Override
    public List<Registration> findActiveBySessionId(long sessionId){
        String sql = BASE + "WHERE r.status = 'ACTIVE' AND r.session_id = ? ORDER BY r.registered_at";
        List<Registration> registrations = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, sessionId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    registrations.add(mapper(resultSet));
                }
                return registrations;
            }
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Can't find registrations with id " + sessionId, e);
        }

    }

}
