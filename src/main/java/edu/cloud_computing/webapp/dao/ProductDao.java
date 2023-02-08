package edu.cloud_computing.webapp.dao;


import edu.cloud_computing.webapp.entity.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class ProductDao {
    public static void createProduct(Product product) {
        product.setDateAdded(Timestamp.from(Instant.now()));
        product.setDateLastUpdated(Timestamp.from(Instant.now()));
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(product);
        tx.commit();
        session.close();
    }

    public static Product getProductById(int id) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from Product where id = :id");
        query.setParameter("id", id);
        List<Product> list = query.list();
        tx.commit();
        session.close();
        return list.get(0);
    }

    public static Product getProductBySku(String sku) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from Product where sku = :sku");
        query.setParameter("sku", sku);
        List<Product> list = query.list();
        tx.commit();
        session.close();
        return list.get(0);
    }

    public static boolean checkSkuExists(String sku) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("select count (*) from Product where sku=:sku");
        query.setParameter("sku", sku);
        int result = Long.valueOf((long) query.list().get(0)).intValue();
        tx.commit();
        session.close();
        return result != 0;
    }

    public static boolean checkIdExists(int id) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("select count (*) from Product where id=:id");
        query.setParameter("id", id);
        int result = Long.valueOf((long) query.list().get(0)).intValue();
        tx.commit();
        session.close();
        return result != 0;
    }

    public static void updateProduct(Product product) {
        product.setDateLastUpdated(Timestamp.from(Instant.now()));
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.merge(product);
        tx.commit();
        session.close();
    }

    public static void deleteProduct(Product product) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.remove(product);
        tx.commit();
        session.close();
    }

//    public static void main(String[] args) {
//        Product product = new Product();
//        product.setFirstName("Jun");
//        product.setLastName("Liang");
//        product.setProductname("liang.jun1@northeastern.edu");
//        product.setPassword("123456");
//        createProduct(product);
//    }
}