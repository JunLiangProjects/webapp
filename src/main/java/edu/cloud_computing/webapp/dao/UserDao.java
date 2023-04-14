package edu.cloud_computing.webapp.dao;

import edu.cloud_computing.webapp.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class UserDao {
    public static void createUser(User user) {
        user.setAccount_created(Timestamp.from(Instant.now()));
        user.setAccount_updated(Timestamp.from(Instant.now()));
        Configuration cfg = new Configuration();
        SessionFactory sf = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
        session.close();
        sf.close();
    }

    public static User getUserById(int userId) {
        Configuration cfg = new Configuration();
        SessionFactory sf = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from User where userId = :userId");
        query.setParameter("userId", userId);
        List<User> list = query.list();
        tx.commit();
        session.close();
        sf.close();
        return list.get(0);
    }

    public static User getUserByUsername(String username) {
        Configuration cfg = new Configuration();
        SessionFactory sf = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from User where username = :username");
        query.setParameter("username", username);
        List<User> list = query.list();
        tx.commit();
        session.close();
        sf.close();
        return list.get(0);
    }

    public static boolean checkUsernameExists(String username) {
        Configuration cfg = new Configuration();
        SessionFactory sf = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("select count (*) from User where username=:username");
        query.setParameter("username", username);
        int result = Long.valueOf((long) query.list().get(0)).intValue();
        tx.commit();
        session.close();
        sf.close();
        return result != 0;
    }

    public static boolean checkIdExists(int userId) {
        Configuration cfg = new Configuration();
        SessionFactory sf = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("select count (*) from User where userId=:userId");
        query.setParameter("userId", userId);
        int result = Long.valueOf((long) query.list().get(0)).intValue();
        tx.commit();
        session.close();
        sf.close();
        return result != 0;
    }

    public static void updateUser(User user) {
        user.setAccount_updated(Timestamp.from(Instant.now()));
        Configuration cfg = new Configuration();
        SessionFactory sf = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.merge(user);
        tx.commit();
        session.close();
        sf.close();
    }

    public static void main(String[] args) {
        User user = new User();
        user.setFirstName("Jun");
        user.setLastName("Liang");
        user.setUsername("liang.jun1@northeastern.edu");
        user.setPassword("123456");
        createUser(user);
    }
}