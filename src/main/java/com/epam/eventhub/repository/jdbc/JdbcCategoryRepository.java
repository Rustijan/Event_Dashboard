package com.epam.eventhub.repository.jdbc;

import com.epam.eventhub.model.Category;
import com.epam.eventhub.repository.CategoryRepository;
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
public class JdbcCategoryRepository implements CategoryRepository {
    private DataSource dataSource;
    public JdbcCategoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Category> findAll(){
        String sql = "SELECT id, name FROM categories ORDER BY name";
        List<Category> list= new ArrayList<>();

        try(Connection connection = dataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                try(ResultSet resultSet = statement.executeQuery()){
                    while(resultSet.next()){
                        list.add(new Category(
                                resultSet.getLong("id"),
                                resultSet.getString("name")
                        ));

                    }
                    return list;
                }
            }
        }catch(SQLException e){
            throw new DataAccessResourceFailureException("Failed to retrieve categories", e);
        }

    }


}
