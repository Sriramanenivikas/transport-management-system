
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS bids CASCADE;
DROP TABLE IF EXISTS loads CASCADE;
DROP TABLE IF EXISTS truck_capacity CASCADE;
DROP TABLE IF EXISTS transporters CASCADE;

CREATE TABLE transporters (
    transporter_id SERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    rating DECIMAL(2,1) CHECK (rating >= 1.0 AND rating <= 5.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

CREATE TABLE truck_capacity (
    id SERIAL PRIMARY KEY,
    transporter_id INTEGER NOT NULL,
    truck_type VARCHAR(50) NOT NULL,
    count INTEGER NOT NULL CHECK (count >= 0),
    version INTEGER DEFAULT 0,
    FOREIGN KEY (transporter_id) REFERENCES transporters(transporter_id) ON DELETE CASCADE,
    UNIQUE(transporter_id, truck_type)
);

CREATE TABLE loads (
    load_id SERIAL PRIMARY KEY,
    shipper_id VARCHAR(100) NOT NULL,
    loading_city VARCHAR(100) NOT NULL,
    unloading_city VARCHAR(100) NOT NULL,
    loading_date TIMESTAMP NOT NULL,
    product_type VARCHAR(100) NOT NULL,
    weight DECIMAL(10,2) NOT NULL,
    weight_unit VARCHAR(10) NOT NULL CHECK (weight_unit IN ('KG', 'TON')),
    truck_type VARCHAR(50) NOT NULL,
    no_of_trucks INTEGER NOT NULL CHECK (no_of_trucks > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED' CHECK (status IN ('POSTED', 'OPEN_FOR_BIDS', 'BOOKED', 'CANCELLED')),
    date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

CREATE INDEX idx_loads_shipper ON loads(shipper_id);
CREATE INDEX idx_loads_status ON loads(status);
CREATE INDEX idx_loads_loading_date ON loads(loading_date);

CREATE TABLE bids (
    bid_id SERIAL PRIMARY KEY,
    load_id INTEGER NOT NULL,
    transporter_id INTEGER NOT NULL,
    proposed_rate DECIMAL(10,2) NOT NULL,
    trucks_offered INTEGER NOT NULL CHECK (trucks_offered > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (load_id) REFERENCES loads(load_id) ON DELETE CASCADE,
    FOREIGN KEY (transporter_id) REFERENCES transporters(transporter_id) ON DELETE CASCADE
);

CREATE INDEX idx_bids_load ON bids(load_id);
CREATE INDEX idx_bids_transporter ON bids(transporter_id);
CREATE INDEX idx_bids_status ON bids(status);

CREATE TABLE bookings (
    booking_id SERIAL PRIMARY KEY,
    load_id INTEGER NOT NULL,
    bid_id INTEGER NOT NULL,
    transporter_id INTEGER NOT NULL,
    allocated_trucks INTEGER NOT NULL CHECK (allocated_trucks > 0),
    final_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED', 'COMPLETED', 'CANCELLED')),
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (load_id) REFERENCES loads(load_id) ON DELETE CASCADE,
    FOREIGN KEY (bid_id) REFERENCES bids(bid_id) ON DELETE CASCADE,
    FOREIGN KEY (transporter_id) REFERENCES transporters(transporter_id) ON DELETE CASCADE
);

CREATE INDEX idx_bookings_load ON bookings(load_id);
CREATE INDEX idx_bookings_transporter ON bookings(transporter_id);
CREATE INDEX idx_bookings_status ON bookings(status);

INSERT INTO transporters (company_name, rating) VALUES
('VRL Logistics Ltd', 4.7),
('Blue Dart Express', 4.9),
('Delhivery Freight', 4.6),
('Mahindra Logistics', 4.5),
('Safexpress Pvt Ltd', 4.3),
('XpressBees Logistics', 4.4),
('Ecom Express', 4.1),
('Shadowfax Technologies', 4.2),
('DHL Supply Chain India', 4.8),
('FedEx India', 4.7),
('Rivigo Services', 4.4),
('BlackBuck Fleet', 4.6),
('Porter Logistics', 4.0),
('Gati-KWE', 4.5),
('DTDC Express', 4.3),
('TCI Freight', 4.4),
('Agarwal Packers', 4.2),
('All Cargo Logistics', 4.6),
('Aegis Logistics', 4.5),
('Transport Corporation of India', 4.7);

INSERT INTO truck_capacity (transporter_id, truck_type, count) VALUES
(1, 'TATA-407', 45),
(1, 'CONTAINER-20FT', 35),
(1, 'CONTAINER-40FT', 25),
(1, 'FLATBED-TRAILER', 15),
(1, 'CLOSED-BODY', 20),
(2, 'TATA-407', 60),
(2, 'REFRIGERATED-VAN', 30),
(2, 'CONTAINER-20FT', 40),
(2, 'CLOSED-BODY', 25),
(3, 'CONTAINER-20FT', 50),
(3, 'CONTAINER-40FT', 40),
(3, 'FLATBED-TRAILER', 20),
(3, 'TATA-407', 35),
(4, 'CONTAINER-40FT', 45),
(4, 'FLATBED-TRAILER', 30),
(4, 'REFRIGERATED-VAN', 20),
(4, 'TANKER', 15),
(4, 'CLOSED-BODY', 25),
(5, 'TATA-407', 40),
(5, 'CONTAINER-20FT', 30),
(5, 'CONTAINER-40FT', 20),
(5, 'REFRIGERATED-VAN', 15),
(6, 'TATA-407', 70),
(6, 'CONTAINER-20FT', 30),
(6, 'CLOSED-BODY', 25),
(7, 'TATA-407', 80),
(7, 'CONTAINER-20FT', 25),
(7, 'CLOSED-BODY', 30),
(8, 'TATA-407', 55),
(8, 'REFRIGERATED-VAN', 20),
(8, 'CONTAINER-20FT', 25),
(9, 'CONTAINER-20FT', 60),
(9, 'CONTAINER-40FT', 50),
(9, 'REFRIGERATED-VAN', 30),
(9, 'FLATBED-TRAILER', 25),
(10, 'CONTAINER-20FT', 65),
(10, 'CONTAINER-40FT', 55),
(10, 'CLOSED-BODY', 35),
(11, 'CONTAINER-20FT', 70),
(11, 'CONTAINER-40FT', 60),
(11, 'FLATBED-TRAILER', 30),
(12, 'TATA-407', 100),
(12, 'CONTAINER-20FT', 80),
(12, 'CONTAINER-40FT', 60),
(12, 'FLATBED-TRAILER', 40),
(13, 'TATA-407', 120),
(13, 'CONTAINER-20FT', 35),
(14, 'CONTAINER-20FT', 45),
(14, 'CONTAINER-40FT', 35),
(14, 'TATA-407', 50),
(15, 'TATA-407', 55),
(15, 'CONTAINER-20FT', 30),
(15, 'CLOSED-BODY', 25),
(16, 'CONTAINER-40FT', 70),
(16, 'FLATBED-TRAILER', 50),
(16, 'TANKER', 25),
(17, 'CONTAINER-20FT', 40),
(17, 'CLOSED-BODY', 35),
(17, 'TATA-407', 45),
(18, 'CONTAINER-20FT', 55),
(18, 'CONTAINER-40FT', 45),
(18, 'REFRIGERATED-VAN', 25),
(19, 'TANKER', 40),
(19, 'CONTAINER-40FT', 30),
(19, 'FLATBED-TRAILER', 20),
(20, 'CONTAINER-20FT', 60),
(20, 'CONTAINER-40FT', 50),
(20, 'TATA-407', 40),
(20, 'REFRIGERATED-VAN', 30);

INSERT INTO loads (shipper_id, loading_city, unloading_city, loading_date, product_type, weight, weight_unit, truck_type, no_of_trucks, status) VALUES
('SHIP001', 'Mumbai', 'Delhi', '2025-12-05 08:00:00', 'Electronics', 5000.00, 'KG', 'CONTAINER-20FT', 2, 'POSTED'),
('SHIP002', 'Bangalore', 'Chennai', '2025-12-06 09:00:00', 'Textiles', 8.50, 'TON', 'CONTAINER-40FT', 3, 'POSTED'),
('SHIP003', 'Delhi', 'Kolkata', '2025-12-07 07:30:00', 'FMCG Goods', 3500.00, 'KG', 'TATA-407', 1, 'POSTED'),
('SHIP004', 'Pune', 'Jaipur', '2025-12-08 09:00:00', 'Auto Parts', 12.00, 'TON', 'CONTAINER-40FT', 2, 'POSTED'),
('SHIP005', 'Chennai', 'Kolkata', '2025-12-09 07:00:00', 'Pharmaceuticals', 2500.00, 'KG', 'REFRIGERATED-VAN', 1, 'POSTED'),
('SHIP006', 'Hyderabad', 'Mumbai', '2025-12-10 06:30:00', 'Machinery', 18.50, 'TON', 'CONTAINER-40FT', 3, 'POSTED'),
('SHIP007', 'Ahmedabad', 'Bangalore', '2025-12-11 08:00:00', 'Chemicals', 8.00, 'TON', 'CONTAINER-20FT', 2, 'POSTED'),
('SHIP008', 'Delhi', 'Lucknow', '2025-12-12 10:00:00', 'Consumer Goods', 3500.00, 'KG', 'TATA-407', 2, 'POSTED'),
('SHIP009', 'Surat', 'Indore', '2025-12-13 07:30:00', 'Textiles', 6.50, 'TON', 'CONTAINER-20FT', 1, 'POSTED'),
('SHIP010', 'Nagpur', 'Bhopal', '2025-12-14 09:00:00', 'Steel Coils', 22.00, 'TON', 'FLATBED-TRAILER', 4, 'POSTED'),
('SHIP011', 'Jaipur', 'Ahmedabad', '2025-12-15 08:30:00', 'Handicrafts', 1200.00, 'KG', 'TATA-407', 1, 'POSTED'),
('SHIP012', 'Kochi', 'Mumbai', '2025-12-16 06:00:00', 'Spices', 800.00, 'KG', 'REFRIGERATED-VAN', 1, 'POSTED'),
('SHIP013', 'Coimbatore', 'Bangalore', '2025-12-17 07:00:00', 'Cotton Bales', 15.00, 'TON', 'CONTAINER-40FT', 3, 'POSTED'),
('SHIP014', 'Indore', 'Delhi', '2025-12-18 09:30:00', 'Furniture', 4000.00, 'KG', 'CLOSED-BODY', 2, 'POSTED'),
('SHIP015', 'Lucknow', 'Varanasi', '2025-12-19 08:00:00', 'Agricultural Produce', 10.00, 'TON', 'CONTAINER-20FT', 2, 'POSTED');

INSERT INTO loads (shipper_id, loading_city, unloading_city, loading_date, product_type, weight, weight_unit, truck_type, no_of_trucks, status) VALUES
('SHIP016', 'Mumbai', 'Pune', '2025-12-03 11:00:00', 'Consumer Electronics', 4500.00, 'KG', 'CONTAINER-20FT', 1, 'OPEN_FOR_BIDS'),
('SHIP017', 'Delhi', 'Chandigarh', '2025-12-04 06:00:00', 'Food Grains', 15.00, 'TON', 'CONTAINER-40FT', 3, 'OPEN_FOR_BIDS'),
('SHIP018', 'Bangalore', 'Mysore', '2025-12-05 08:30:00', 'Furniture', 2800.00, 'KG', 'TATA-407', 1, 'OPEN_FOR_BIDS'),
('SHIP019', 'Kolkata', 'Patna', '2025-12-06 07:00:00', 'Paper Products', 9.00, 'TON', 'CONTAINER-20FT', 2, 'OPEN_FOR_BIDS'),
('SHIP020', 'Chennai', 'Coimbatore', '2025-12-07 05:30:00', 'Dairy Products', 1800.00, 'KG', 'REFRIGERATED-VAN', 1, 'OPEN_FOR_BIDS'),
('SHIP021', 'Hyderabad', 'Visakhapatnam', '2025-12-08 08:00:00', 'IT Equipment', 3200.00, 'KG', 'CONTAINER-20FT', 1, 'OPEN_FOR_BIDS'),
('SHIP022', 'Pune', 'Nashik', '2025-12-09 07:30:00', 'Wine Bottles', 5000.00, 'KG', 'CLOSED-BODY', 2, 'OPEN_FOR_BIDS'),
('SHIP023', 'Ahmedabad', 'Rajkot', '2025-12-10 09:00:00', 'Ceramics', 7.50, 'TON', 'CONTAINER-20FT', 2, 'OPEN_FOR_BIDS'),
('SHIP024', 'Surat', 'Mumbai', '2025-12-11 06:30:00', 'Diamond Shipment', 500.00, 'KG', 'TATA-407', 1, 'OPEN_FOR_BIDS'),
('SHIP025', 'Jaipur', 'Delhi', '2025-12-12 08:00:00', 'Marble Slabs', 20.00, 'TON', 'FLATBED-TRAILER', 4, 'OPEN_FOR_BIDS'),
('SHIP026', 'Lucknow', 'Kanpur', '2025-12-13 07:00:00', 'Leather Goods', 2500.00, 'KG', 'TATA-407', 1, 'OPEN_FOR_BIDS'),
('SHIP027', 'Nagpur', 'Raipur', '2025-12-14 09:30:00', 'Coal', 35.00, 'TON', 'CONTAINER-40FT', 7, 'OPEN_FOR_BIDS'),
('SHIP028', 'Bhopal', 'Indore', '2025-12-15 08:00:00', 'Packaged Food', 4000.00, 'KG', 'CONTAINER-20FT', 2, 'OPEN_FOR_BIDS'),
('SHIP029', 'Chandigarh', 'Amritsar', '2025-12-16 07:30:00', 'Wheat', 12.00, 'TON', 'CONTAINER-40FT', 2, 'OPEN_FOR_BIDS'),
('SHIP030', 'Kochi', 'Trivandrum', '2025-12-17 06:00:00', 'Seafood', 1500.00, 'KG', 'REFRIGERATED-VAN', 1, 'OPEN_FOR_BIDS'),
('SHIP031', 'Coimbatore', 'Madurai', '2025-12-18 08:30:00', 'Machinery Parts', 6000.00, 'KG', 'CONTAINER-20FT', 2, 'OPEN_FOR_BIDS'),
('SHIP032', 'Visakhapatnam', 'Vijayawada', '2025-12-19 07:00:00', 'Steel Rods', 25.00, 'TON', 'FLATBED-TRAILER', 5, 'OPEN_FOR_BIDS'),
('SHIP033', 'Indore', 'Bhopal', '2025-12-20 09:00:00', 'Electrical Goods', 3800.00, 'KG', 'CONTAINER-20FT', 2, 'OPEN_FOR_BIDS'),
('SHIP034', 'Varanasi', 'Allahabad', '2025-12-21 08:00:00', 'Handicrafts', 900.00, 'KG', 'TATA-407', 1, 'OPEN_FOR_BIDS'),
('SHIP035', 'Guwahati', 'Shillong', '2025-12-22 07:30:00', 'Tea Chests', 5.00, 'TON', 'CONTAINER-20FT', 1, 'OPEN_FOR_BIDS');

INSERT INTO loads (shipper_id, loading_city, unloading_city, loading_date, product_type, weight, weight_unit, truck_type, no_of_trucks, status) VALUES
('SHIP036', 'Mumbai', 'Ahmedabad', '2025-11-25 08:00:00', 'Cotton Bales', 20.00, 'TON', 'CONTAINER-40FT', 4, 'BOOKED'),
('SHIP037', 'Chennai', 'Bangalore', '2025-11-26 09:00:00', 'IT Equipment', 3000.00, 'KG', 'CONTAINER-20FT', 1, 'BOOKED'),
('SHIP038', 'Delhi', 'Agra', '2025-11-27 07:00:00', 'Marble Slabs', 35.00, 'TON', 'FLATBED-TRAILER', 7, 'BOOKED'),
('SHIP039', 'Kolkata', 'Guwahati', '2025-11-28 06:00:00', 'Tea Chests', 8.00, 'TON', 'CONTAINER-20FT', 2, 'BOOKED'),
('SHIP040', 'Pune', 'Nashik', '2025-11-29 10:00:00', 'Wine Barrels', 1500.00, 'KG', 'REFRIGERATED-VAN', 1, 'BOOKED'),
('SHIP041', 'Hyderabad', 'Vijayawada', '2025-11-30 09:00:00', 'Cement Bags', 25.00, 'TON', 'CONTAINER-40FT', 5, 'BOOKED'),
('SHIP042', 'Jaipur', 'Delhi', '2025-12-01 08:00:00', 'Handicrafts', 1200.00, 'KG', 'TATA-407', 1, 'BOOKED'),
('SHIP043', 'Lucknow', 'Kanpur', '2025-12-02 10:00:00', 'Electrical Equipment', 5500.00, 'KG', 'CONTAINER-20FT', 1, 'BOOKED'),
('SHIP044', 'Ahmedabad', 'Surat', '2025-12-03 07:30:00', 'Chemicals', 10.00, 'TON', 'TANKER', 2, 'BOOKED'),
('SHIP045', 'Bangalore', 'Hubli', '2025-12-04 08:30:00', 'Electronics', 4200.00, 'KG', 'CONTAINER-20FT', 2, 'BOOKED');

INSERT INTO loads (shipper_id, loading_city, unloading_city, loading_date, product_type, weight, weight_unit, truck_type, no_of_trucks, status) VALUES
('SHIP046', 'Bhopal', 'Raipur', '2025-11-20 07:00:00', 'Fertilizers', 30.00, 'TON', 'CONTAINER-40FT', 6, 'CANCELLED'),
('SHIP047', 'Kochi', 'Trivandrum', '2025-11-21 06:00:00', 'Spices', 800.00, 'KG', 'TATA-407', 1, 'CANCELLED'),
('SHIP048', 'Nagpur', 'Indore', '2025-11-22 08:00:00', 'Cotton', 18.00, 'TON', 'CONTAINER-40FT', 3, 'CANCELLED'),
('SHIP049', 'Chandigarh', 'Shimla', '2025-11-23 07:00:00', 'Building Materials', 12.00, 'TON', 'FLATBED-TRAILER', 3, 'CANCELLED'),
('SHIP050', 'Visakhapatnam', 'Chennai', '2025-11-24 09:00:00', 'Steel Products', 28.00, 'TON', 'FLATBED-TRAILER', 6, 'CANCELLED');

INSERT INTO bids (load_id, transporter_id, proposed_rate, trucks_offered, status) VALUES
(16, 1, 25000.00, 1, 'PENDING'),
(16, 2, 23500.00, 1, 'PENDING'),
(16, 5, 24000.00, 1, 'PENDING'),
(17, 3, 85000.00, 3, 'PENDING'),
(17, 4, 82000.00, 3, 'PENDING'),
(17, 9, 88000.00, 3, 'PENDING'),
(18, 1, 12000.00, 1, 'PENDING'),
(18, 6, 11500.00, 1, 'PENDING'),
(19, 11, 45000.00, 2, 'PENDING'),
(19, 14, 42000.00, 2, 'PENDING'),
(20, 2, 18000.00, 1, 'PENDING'),
(20, 8, 17500.00, 1, 'PENDING'),
(21, 3, 28000.00, 1, 'PENDING'),
(21, 9, 26500.00, 1, 'PENDING'),
(22, 1, 35000.00, 2, 'PENDING'),
(22, 10, 33000.00, 2, 'PENDING'),
(23, 5, 42000.00, 2, 'PENDING'),
(23, 14, 40000.00, 2, 'PENDING'),
(24, 6, 15000.00, 1, 'PENDING'),
(24, 13, 14500.00, 1, 'PENDING'),
(25, 11, 120000.00, 4, 'PENDING'),
(25, 16, 115000.00, 4, 'PENDING'),
(26, 1, 11000.00, 1, 'PENDING'),
(26, 13, 10500.00, 1, 'PENDING'),
(27, 4, 210000.00, 7, 'PENDING'),
(27, 11, 205000.00, 7, 'PENDING'),
(28, 3, 38000.00, 2, 'PENDING'),
(28, 14, 36000.00, 2, 'PENDING'),
(29, 9, 72000.00, 2, 'PENDING'),
(29, 10, 70000.00, 2, 'PENDING'),
(30, 2, 16000.00, 1, 'PENDING'),
(30, 18, 15500.00, 1, 'PENDING'),
(31, 3, 44000.00, 2, 'PENDING'),
(31, 14, 42500.00, 2, 'PENDING'),
(32, 11, 155000.00, 5, 'PENDING'),
(32, 16, 150000.00, 5, 'PENDING'),
(33, 5, 36000.00, 2, 'PENDING'),
(33, 14, 34500.00, 2, 'PENDING'),
(34, 6, 9500.00, 1, 'PENDING'),
(34, 13, 9000.00, 1, 'PENDING'),
(35, 3, 48000.00, 1, 'PENDING'),
(35, 14, 46000.00, 1, 'PENDING'),
(36, 3, 120000.00, 4, 'ACCEPTED'),
(36, 4, 125000.00, 4, 'REJECTED'),
(37, 5, 28000.00, 1, 'ACCEPTED'),
(37, 9, 29000.00, 1, 'REJECTED'),
(38, 11, 210000.00, 7, 'ACCEPTED'),
(38, 16, 215000.00, 7, 'REJECTED'),
(39, 14, 55000.00, 2, 'ACCEPTED'),
(39, 3, 57000.00, 2, 'REJECTED'),
(40, 2, 15000.00, 1, 'ACCEPTED'),
(40, 18, 16000.00, 1, 'REJECTED'),
(41, 4, 150000.00, 5, 'ACCEPTED'),
(41, 11, 155000.00, 5, 'REJECTED'),
(42, 1, 8500.00, 1, 'ACCEPTED'),
(42, 13, 9000.00, 1, 'REJECTED'),
(43, 5, 22000.00, 1, 'ACCEPTED'),
(43, 14, 23000.00, 1, 'REJECTED'),
(44, 19, 75000.00, 2, 'ACCEPTED'),
(44, 16, 78000.00, 2, 'REJECTED'),
(45, 9, 52000.00, 2, 'ACCEPTED'),
(45, 10, 54000.00, 2, 'REJECTED');

INSERT INTO bookings (load_id, bid_id, transporter_id, allocated_trucks, final_rate, status) VALUES
(36, 43, 3, 4, 120000.00, 'CONFIRMED'),
(37, 45, 5, 1, 28000.00, 'COMPLETED'),
(38, 47, 11, 7, 210000.00, 'CONFIRMED'),
(39, 49, 14, 2, 55000.00, 'COMPLETED'),
(40, 51, 2, 1, 15000.00, 'CONFIRMED'),
(41, 53, 4, 5, 150000.00, 'CONFIRMED'),
(42, 55, 1, 1, 8500.00, 'COMPLETED'),
(43, 57, 5, 1, 22000.00, 'CONFIRMED'),
(44, 59, 19, 2, 75000.00, 'CONFIRMED'),
(45, 61, 9, 2, 52000.00, 'CONFIRMED');

