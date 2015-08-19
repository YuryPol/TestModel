----------------------
-- the process:
---------------------
use test_fia;
-- if we want to change raw data
-- om cmd prompt:
-- java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.31-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin GenInput
---------------------
-- run always:
-- on MySql prompt 
-- call GetWeightRawData;
-- call GetStructData;
-- start alotments 
-- select bin(set_map), set_map, full_count, availability, goal from structured_data_inc where BIT_COUNT(set_map)=1;
-- deside on criteria, ammount
-- call GetItems(criteria, ammount);
--  -"- repeat until done
-- java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.31-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin Simulation
-- check results
-- 

CREATE DATABASE Demo;

USE Demo;

-- create raw_inventory table to fill up by impressons' counts
DROP TABLE raw_inventory;
CREATE TABLE raw_inventory(
	basesets BIGINT NOT NULL, 
	count INT NOT NULL,
    PRIMARY KEY (basesets));    
-- populated by ProcessInput.java

--
-- creating structured data 
--
DROP TABLE structured_data_inc;
CREATE TABLE structured_data_inc(
    set_key BIGINT DEFAULT NULL,
	set_name VARCHAR(20) DEFAULT NULL,
	rank INT DEFAULT 0,
    capacity INT NOT NULL DEFAULT 0, 
    availability INT NOT NULL DEFAULT 0, 
    goal INT DEFAULT 0,
	PRIMARY KEY(set_key));
-- initially populated by ProcessInputInc.java with 0-rank records

-- add capacities, availabilities and to records of specified rank
DROP PROCEDURE IF EXISTS PopulateWithNumbers;
DELIMITER //
CREATE PROCEDURE PopulateWithNumbers()
BEGIN
 UPDATE structured_data_inc sd0,
 (SELECT 
	set_key,
	SUM(capacity) as capacity, 
	SUM(availability) as availability
  FROM (	
   SELECT sd.set_key, ri.count as capacity, ri.count as availability
	FROM structured_data_inc sd 
	JOIN raw_inventory ri 
	ON sd.set_key & ri.basesets != 0 ) blownUp
  GROUP BY set_key
 ) comp
 SET sd0.capacity = comp.capacity,
 	 sd0.availability = comp.availability
 WHERE sd0.set_key = comp.set_key
 ;
END //
DELIMITER ;

-- inventroy sets copy to use for adding more unions, created after running PopulateWithNumbers once.
CREATE TABLE structured_data_base AS SELECT * FROM structured_data_inc;

-- ads unions of higher rank 
DROP PROCEDURE IF EXISTS AddUnions;
DELIMITER //
CREATE PROCEDURE AddUnions()
BEGIN
	INSERT IGNORE INTO structured_data_inc
	SELECT sd1.set_key | sd2.set_key, null, 0, 0, 0, 0
	FROM structured_data_inc sd1 
	JOIN structured_data_base sd2 
	;
END //
DELIMITER ;
-- needs PopulateWithNumbers call to complete

DROP PROCEDURE IF EXISTS EliminateUnions;
DELIMITER //
CREATE PROCEDURE EliminateUnions()
BEGIN
	DELETE FROM structured_data_inc sd1
	LEFT JOIN structured_data_base sd2
	ON sd1.set_key
	WHERE sd1.set_key


-- view data
select lpad(bin(set_key), 10, '0') as setkey, set_name, rank, capacity, availability, goal from structured_data_inc;	
select lpad(bin(basesets), 10, '0') as setkey, count from raw_inventory;	

