package edu.cloud_computing.webapp.dao;

import edu.cloud_computing.webapp.entity.Image;
import edu.cloud_computing.webapp.entity.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ImageDao {
    public static void createImage(Image image) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(image);
        tx.commit();
        session.close();
    }

    public static List<Image> getImageByProductId(int productId) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from Image where productId = :productId");
        query.setParameter("productId", productId);
        List<Image> list = query.list();
        tx.commit();
        session.close();
        return list;
    }

    public static Image getImageByImageId(int imageId) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from Image where imageId = :imageId");
        query.setParameter(imageId, imageId);
        List<Image> list = query.list();
        tx.commit();
        session.close();
        return list.get(0);
    }

    public static boolean checkProductIdExists(int productId) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("select count (*) from Image where productId=:productId");
        query.setParameter("productId", productId);
        int result = Long.valueOf((long) query.list().get(0)).intValue();
        tx.commit();
        session.close();
        return result != 0;
    }

    public static boolean checkImageIdExists(int imageId) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("select count (*) from Image where imageId=:imageId");
        query.setParameter("imageId", imageId);
        int result = Long.valueOf((long) query.list().get(0)).intValue();
        tx.commit();
        session.close();
        return result != 0;
    }
    public static void deleteImage(Image image) {
        SessionFactory sf = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        session.remove(image);
        tx.commit();
        session.close();
    }
}