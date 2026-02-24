-- Script to reset database for development
-- Run this in MySQL: source reset_db.sql

SET FOREIGN_KEY_CHECKS = 0;

-- Drop all tables if they exist
DROP TABLE IF EXISTS flyway_schema_history;
DROP TABLE IF EXISTS booking_participants;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS court_pricing;
DROP TABLE IF EXISTS courts;
DROP TABLE IF EXISTS venues;
DROP TABLE IF EXISTS venue_staff_permissions;
DROP TABLE IF EXISTS venue_staff;
DROP TABLE IF EXISTS referees;
DROP TABLE IF EXISTS registration_requests;
DROP TABLE IF EXISTS seasons;
DROP TABLE IF EXISTS venue_owners;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- Now Flyway will recreate all tables on next app start
SELECT 'Database reset complete. Restart the application to run migrations.' as message;
