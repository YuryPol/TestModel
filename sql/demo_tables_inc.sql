----------------------
-- the process:
---------------------
-- to start om cmd prompt:
-- 	java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.36-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin ProcessInputInc
-- 		that creates initial structured and raw data tables from JSON file
-- call PopulateWithNumbers; that adds capacities and availabilities to structured_data_inc
-- create structured_data_base table from structured_data_inc
-- call AdUnions; creates unions of first rank
-- call PopulateWithNumbers; adds capacities and availabilities to structured_data_inc
-- call EliminateUnions; deleats non-overlapping unions creatd by AdUnions
---------------------

CREATE DATABASE Demo;

USE Demo;

-- setting up the tables

-- create raw_inventory table to fill up by impressons' counts
DROP TABLE raw_inventory;
CREATE TABLE raw_inventory(
	basesets BIGINT NOT NULL, 
	count INT NOT NULL,
    PRIMARY KEY (basesets));    
-- populated by ProcessInputInc.java

-- create structured data 
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

-- create inventroy sets
DROP TABLE structured_data_base;
CREATE TABLE structured_data_base AS 
	SELECT set_key as set_key_is -- inventory set's key
	, set_name, capacity, availability, goal, set_key -- effective key
	FROM structured_data_inc;


-- setting up stored procs

-- adds capacities, availabilities
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

-- adds unions of a higher rank 
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

DELETE FROM t1, t2 USING t1 INNER JOIN t2 INNER JOIN t3
WHERE t1.id=t2.id AND t2.id=t3.id;

-- deletes non-overlapping unions
DROP PROCEDURE IF EXISTS EliminateUnions;
DELIMITER //
CREATE PROCEDURE EliminateUnions()
BEGIN
	DELETE FROM structured_data_inc
	USING structured_data_inc INNER JOIN structured_data_base sb1 INNER JOIN structured_data_base sb2
	WHERE (structured_data_inc.set_key & sb1.set_key = sb1.set_key AND structured_data_inc.set_key > sb1.set_key 
	AND structured_data_inc.set_key & sb2.set_key = sb2.set_key AND structured_data_inc.set_key > sb2.set_key
	AND sb1.set_key != sb2.set_key
	AND structured_data_inc.availability = sb1.availability + sb2.availability)
;
END //
DELIMITER ;

--
-- view data
--
select lpad(bin(set_key), 10, '0') as setkey, set_name, rank, capacity, availability, goal from structured_data_inc;
	
select lpad(bin(basesets), 10, '0') as setkey, count from raw_inventory;	

