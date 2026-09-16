-- =====================================================================
-- Schema chuan cho ca nhom - Keyspace: khachsan (Astra DB / Cassandra)
-- Chay 1 lan tren Astra CQL Console hoac cqlsh TRUOC khi chay profile cassandra.
-- Tat ca thanh vien phai dung dung keyspace nay, khong tu tao DB rieng.
-- =====================================================================

CREATE KEYSPACE IF NOT EXISTS khachsan
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE khachsan;

-- Q1: Khach san
CREATE TABLE IF NOT EXISTS hotels (
  hotel_id text PRIMARY KEY,
  hotel_name text,
  city text,
  address text,
  star_rating int,
  phone text,
  email text,
  status text,
  total_rooms int,
  created_at timestamp
);

-- Q2: Phong theo khach san
CREATE TABLE IF NOT EXISTS rooms_by_hotel (
  hotel_id text,
  room_number int,
  room_type text,
  price_per_night decimal,
  status text,
  capacity int,
  floor int,
  created_at timestamp,
  PRIMARY KEY (hotel_id, room_number)
);

-- Khach hang + bang tra cuu SDT
CREATE TABLE IF NOT EXISTS guests (
  guest_id text PRIMARY KEY,
  full_name text,
  phone text,
  email text,
  cccd text,
  address text,
  created_at timestamp
);

CREATE TABLE IF NOT EXISTS guests_by_phone (
  phone text PRIMARY KEY,
  guest_id text,
  full_name text,
  email text,
  created_at timestamp
);

-- Tai khoan + index email
CREATE TABLE IF NOT EXISTS users_by_username (
  username text PRIMARY KEY,
  password_hash text,
  full_name text,
  email text,
  role text,
  enabled boolean,
  created_at timestamp
);

CREATE TABLE IF NOT EXISTS users_by_email (
  email text PRIMARY KEY,
  username text,
  full_name text,
  role text,
  created_at timestamp
);

-- Q3/Q4/Q5: Dat phong (3 bang denormalized) + hoa don
CREATE TABLE IF NOT EXISTS bookings_by_id (
  booking_id uuid PRIMARY KEY,
  guest_id text,
  guest_name text,
  hotel_id text,
  hotel_name text,
  room_number int,
  room_type text,
  check_in_date date,
  check_out_date date,
  nights int,
  total_amount decimal,
  status text,
  notes text,
  created_at timestamp
);

CREATE TABLE IF NOT EXISTS bookings_by_guest (
  guest_id text,
  check_in_date date,
  booking_id uuid,
  hotel_id text,
  hotel_name text,
  guest_name text,
  room_number int,
  room_type text,
  check_out_date date,
  nights int,
  total_amount decimal,
  status text,
  notes text,
  created_at timestamp,
  PRIMARY KEY (guest_id, check_in_date, booking_id)
) WITH CLUSTERING ORDER BY (check_in_date DESC, booking_id DESC);

CREATE TABLE IF NOT EXISTS bookings_by_hotel_date (
  hotel_id text,
  check_in_date date,
  booking_id uuid,
  guest_id text,
  guest_name text,
  room_number int,
  room_type text,
  check_out_date date,
  nights int,
  total_amount decimal,
  status text,
  notes text,
  created_at timestamp,
  PRIMARY KEY (hotel_id, check_in_date, booking_id)
) WITH CLUSTERING ORDER BY (check_in_date ASC, booking_id ASC);

CREATE TABLE IF NOT EXISTS invoices_by_booking (
  booking_id uuid,
  invoice_id uuid,
  guest_id text,
  guest_name text,
  hotel_id text,
  room_charge decimal,
  service_charge decimal,
  tax decimal,
  total_amount decimal,
  payment_status text,
  payment_method text,
  due_date date,
  issued_at timestamp,
  paid_at timestamp,
  PRIMARY KEY (booking_id, invoice_id)
);
