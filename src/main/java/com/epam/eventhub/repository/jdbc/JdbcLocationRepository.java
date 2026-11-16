package com.epam.eventhub.repository.jdbc;

import com.epam.eventhub.model.Location;
import com.epam.eventhub.repository.LocationRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

@Repository
public class JdbcLocationRepository implements LocationRepository {
    DataSource  dataSource;
    public JdbcLocationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Location> findAll(){
        String sql = """
             SELECT id, name, city, address, capacity
             FROM locations
             ORDER BY name ASC
        """;
        List<Location> list = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery()){
            while(resultSet.next()){
                list.add(new Location(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("city"),
                        resultSet.getString("address"),
                        resultSet.getInt("capacity")
                ));
            }
            return list;
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to retrieve locations", e);
        }

    }


    @Override
    public List<String> findCities(){
        String sql = """
                SELECT DISTINCT city 
                FROM locations
                """;
        List<String> list =new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()){
            while(resultSet.next()){
                list.add(resultSet.getString("city"));
            }
            return list;
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to retrieve cities", e);
        }

    }

}
