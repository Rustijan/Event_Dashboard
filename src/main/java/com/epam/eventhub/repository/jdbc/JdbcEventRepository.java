package com.epam.eventhub.repository.jdbc;

import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.dto.EventSearchCriteria;
import com.epam.eventhub.model.Category;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventStatus;
import com.epam.eventhub.model.Location;
import com.epam.eventhub.repository.EventRepository;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Repository
public class JdbcEventRepository implements EventRepository {
    private final DataSource dataSource;
    public JdbcEventRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private String baseSelect(){
        return """
                SELECT
                    e.id,
                    e.title,
                    e.description,
                    e.status,
                    e.organizer_id,
                    c.id AS category_id,
                    c.name AS category_name,
                    l.id AS location_id,
                    l.name AS location_name,
                    l.city,
                    l.address,
                    l.capacity AS location_capacity,
                    u.full_name AS organizer_name
               
                FROM events AS e
                JOIN categories AS c ON e.category_id = c.id
                JOIN locations AS l ON e.location_id = l.id
                JOIN users AS u ON e.organizer_id = u.id
                """;
    }
    private Event mapToEvent(ResultSet rs) throws SQLException {
        Category category = new Category(
                rs.getLong("category_id"),
                rs.getString("category_name")
        );
        Location location = new Location(
                rs.getLong("location_id"),
                rs.getString("location_name"),
                rs.getString("city"),
                rs.getString("address"),
                rs.getInt("location_capacity")
        );

        return new Event(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                EventStatus.valueOf(rs.getString("status")),
                category,
                location,
                rs.getLong("organizer_id"),
                rs.getString("organizer_name")
        );
    }

    @Override
    public Optional<Event> findById(long id){

        String sql = baseSelect() + "WHERE e.id = ?";
        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setLong(1, id);
                try(ResultSet resultSet  = statement.executeQuery() ){
                    if(resultSet.next()){
                        return Optional.of(mapToEvent(resultSet));
                    }
                    return Optional.empty();
                }
            }
        }catch(SQLException e){
            throw new UncategorizedSQLException(
                    "Find event by ID",
                    sql,
                    e
            );
        }
    }

    @Override
    public List<Event> findByOrganizerId(long organizerId){
        String sql = baseSelect() + "WHERE e.organizer_id = ?";
        List<Event> list = new ArrayList<>();
        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setLong(1, organizerId);
                try(ResultSet resultSet  = statement.executeQuery() ){
                    while(resultSet.next()){
                        list.add(mapToEvent(resultSet));
                    }
                    return list;
                }
            }
        }catch(SQLException e){
            throw new UncategorizedSQLException(
                    "Find event by ID",
                    sql,
                    e
            );
        }
    }

    @Override
    public List<Event> findPublished(EventSearchCriteria criteria){
        StringBuilder sql = new StringBuilder(baseSelect());
        sql.append(" WHERE e.status = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(EventStatus.PUBLISHED);

        if(criteria != null){
            String query = criteria.getQuery();

            if(query != null && !query.isBlank()){
                sql.append("""
                        AND (
                            LOWER(e.title) LIKE LOWER(?)
                            OR LOWER(e.description) LIKE LOWER(?)
                        )
                        """);
                String pattern = "%" + query.trim() + "%";
                parameters.add(pattern);
                parameters.add(pattern);


            }
            if(criteria.getCategoryId()!=null){
                sql.append(" AND (e.category_id = ?)");
                parameters.add(criteria.getCategoryId());
            }

            String city = criteria.getCity();

            if(city != null && !city.isBlank()){
                sql.append(" AND LOWER(l.city) = LOWER(?)");
                parameters.add(city.trim());


            }



        }
        sql.append(" ORDER BY e.id DESC");
        try(Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString())){
            for(int i =0 ; i<parameters.size(); i++){
                statement.setObject(i+1, parameters.get(i));
            }
            List<Event> events = new ArrayList<>();
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    events.add(mapToEvent(resultSet));
                }
            }
            return events;
        }catch(SQLException e){
            throw new UncategorizedSQLException(
                    "Find published events",
                    sql.toString(),
                    e
            );
        }

    }


    @Override
    public Event save(EventForm form, long organizerId){

        String sql = """
                INSERT INTO events ( title, description, category_id, location_id, 
                organizer_id, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement stm =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stm.setString(1, form.getTitle());
            stm.setString(2, form.getDescription());
            stm.setLong(3, form.getCategoryId());
            stm.setLong(4, form.getLocationId());
            stm.setLong(5, organizerId);
            stm.setString(6, EventStatus.DRAFT.name());

            stm.executeUpdate();
            try(ResultSet resultSet = stm.getGeneratedKeys()){
                if(resultSet.next()){
                    long id = resultSet.getLong(1);

                    return findById(id).orElseThrow(
                            () -> new IllegalStateException(
                                    "Saved event could not be retrieved: " + id
                            )
                    );
                }
                throw new SQLException("No generated event ID returned");
            }


        }catch(SQLException e){
            throw new UncategorizedSQLException("Save event failed" , sql, e);
        }

    }


    @Override
    public void update(long eventId, EventForm form){
        String sql = """
                UPDATE events
                SET title = ?,
                 description = ?,
                 category_id = ?, 
                 location_id = ?
                 WHERE id = ?
            
                """;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, form.getTitle());
            statement.setString(2, form.getDescription());
            statement.setLong(3, form.getCategoryId());
            statement.setLong(4, form.getLocationId());
            statement.setLong(5, eventId);

            int updated = statement.executeUpdate();
            if(updated != 1){
                throw new IllegalArgumentException("Event not found: " + eventId);
            }


        }catch(SQLException e){
            throw new UncategorizedSQLException("Update event failed" , sql, e);
        }





    }

    @Override
    public void updateStatus(long eventId, EventStatus status){
        String sql = """
                UPDATE events
                SET status = ?
                WHERE id = ?
                """;

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ){
            statement.setString(1, status.name());
            statement.setLong(2, eventId);

            int rowsUpdated = statement.executeUpdate();
            if(rowsUpdated != 1){
                throw new IllegalArgumentException("Event not found: " + eventId);
            }
        }catch(SQLException e){
            throw new UncategorizedSQLException("Update event status failed" , sql, e);
        }
    }




}
