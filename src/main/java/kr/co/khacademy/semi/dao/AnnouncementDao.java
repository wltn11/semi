package kr.co.khacademy.semi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.khacademy.semi.common.DataSource;
import kr.co.khacademy.semi.model.Announcement;
import kr.co.khacademy.semi.model.Criteria;

public class AnnouncementDao {
    private static final AnnouncementDao instance = new AnnouncementDao();

    private static final String INSERT_SQL = "INSERT INTO announcement VALUES (default,default,?, ?,default)";
    private static final String SELECT_SQL = "SELECT * FROM announcement";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM announcement WHERE id = ?";
    private static final String SELECT_BOUND_SQL = "SELECT * FROM(SELECT announcement.*, row_number() over(ORDER BY id DESC) rn FROM announcement)rrn WHERE rn BETWEEN ? AND ?";
    private static final String SELECT_TITLE_SQL = "SELECT COUNT(*) FROM announcement WHERE title LIKE ?";
    private static final String UPDATE_BY_ID_SQL = "UPDATE announcement SET title = ?, contents = ? WHERE id = ?";
    private static final String DELETE_BY_ID_SQL = "DELETE FROM announcement WHERE id =?";

    public static AnnouncementDao getInstance() {
        return instance;
    }

    public void create(Announcement announcement) throws SQLException {
        try (Connection connection = DataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)){
                preparedStatement.setString(1, announcement.getTitle());
                preparedStatement.setString(2, announcement.getContents());

                if (preparedStatement.executeUpdate() == 0) {
                    throw new SQLException();
                }
                connection.commit();
            }
        }
    }

    public List<Announcement> read() throws SQLException{
        List<Announcement> AnnouncementList = new ArrayList<>();
        try (Connection connection = DataSource.getConnection()){
            try (
                    PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SQL);
                    ResultSet resultSet = preparedStatement.executeQuery();){
                while (resultSet.next()) {
                    Announcement announcement = Announcement.of(resultSet);
                    AnnouncementList.add(announcement);
                }
                return Collections.unmodifiableList(AnnouncementList);
            }
        }
    }

    public Announcement read(Long id) throws SQLException {
        try (Connection connection = DataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)){
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    if (resultSet.next()) {
                        return Announcement.of(resultSet);
                    }
                    throw new SQLException();
                }
            }
        }
    }

    public List<Announcement> read(Long start, Long end) throws SQLException{
        List<Announcement> announcements = new ArrayList<>();
        try (Connection connection = DataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BOUND_SQL)){
                preparedStatement.setLong(1, start);
                preparedStatement.setLong(2, end);
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()) {
                        Announcement announcement = Announcement.of(resultSet);
                        announcements.add(announcement);
                    }
                    return Collections.unmodifiableList(announcements);
                }
            }
        }
    }

    public Long getRecordCount(String search) throws SQLException {
        try (Connection connection = DataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TITLE_SQL)){
                preparedStatement.setString(1, "%"+search+"%");
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    resultSet.next();
                    return resultSet.getLong(1);
                }
            }
        }
    }

    public List<String> getPageNavi(Criteria criteria) throws SQLException{
        
        Long recordTotalCount = getRecordCount(criteria.getKeyword());

        Long pageNumber = criteria.getPageNumber();
        Long recordCountPerPage = criteria.getAmount();
        Long naviCountPerPage = 10L;

        Long pageTotalCount;

        if (recordTotalCount % recordCountPerPage > 0) {
            pageTotalCount = recordTotalCount / recordCountPerPage + 1;
        } else {
            pageTotalCount = recordTotalCount / recordCountPerPage;
        }
        
        if (pageNumber < 1) {
            pageNumber = 1L;
        } else if (pageNumber > pageTotalCount) {
            pageNumber = pageTotalCount;
        }
        Long startNavi = (((pageNumber - 1) / naviCountPerPage) * naviCountPerPage) + 1;
        Long endNavi = startNavi + (naviCountPerPage-1);
        
        if (endNavi > pageTotalCount) {
            endNavi = pageTotalCount;
        }
        Boolean needPrev = true;
        Boolean needNext = true;
        
        if (startNavi == 1) {needPrev = false;}
        if (endNavi == pageTotalCount) {needNext = false;}
        
        List<String> list = new ArrayList<>();
        
        if (needPrev) {
            list.add("<");
        }
        for (Long i = startNavi; i <= endNavi; i++) {
            list.add(""+i);
        }
        if (needNext) {
            list.add(">");
        }
        return list;
    }

    public void update(Announcement announcement) throws SQLException {
        try (Connection connection = DataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BY_ID_SQL)){
                preparedStatement.setString(1, announcement.getTitle());
                preparedStatement.setString(2, announcement.getContents());
                preparedStatement.setLong(3, announcement.getId());

                if (preparedStatement.executeUpdate() == 0){
                    throw new SQLException();
                }
                connection.commit();
            }
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection connection = DataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BY_ID_SQL)){
                preparedStatement.setLong(1, id);
                
                if (preparedStatement.executeUpdate() == 0) {
                    throw new SQLException();
                }
                connection.commit();
            }
        }
    }
}
