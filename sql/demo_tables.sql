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
-- select bin(set_map), set_map, full_count, availability, goal from structured_data where BIT_COUNT(set_map)=1;
-- deside on criteria, ammount
-- call GetItems(criteria, ammount);
--  -"- repeat until done
-- java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.31-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin Simulation
-- check results
-- 

CREATE DATABASE Demo;

USE Demo;

-- view data
select lpad(bin(set_key), 20, '0'), set_name, capacity, availability from structured_data;	
select lpad(bin(basesets), 20, '0'), count from raw_inventory;	
select lpad(bin(set_key), 20, '0'), capacity from structured_data_counts;	

-- create raw_inventory table to fill up by impressons' counts
DROP TABLE raw_inventory;
CREATE TABLE raw_inventory(
	basesets BIGINT NOT NULL, 
	count INT NOT NULL,
    PRIMARY KEY (basesets));    
-- populated by ProcessInput.java

-- creating structured data 
DROP TABLE structured_data;
CREATE TABLE structured_data(
    set_key BIGINT DEFAULT NULL,
	set_name VARCHAR(20) DEFAULT NULL,
    capacity INT NOT NULL DEFAULT 0, 
    availability INT DEFAULT 0, 
    goal INT DEFAULT 0,
	PRIMARY KEY(set_key));
-- populated by ProcessInput.java except capacity and availability

-- creating structured data 
DROP TABLE structured_data_counts;
CREATE TABLE structured_data_counts(
    set_key BIGINT DEFAULT NULL,
	set_name VARCHAR(20) DEFAULT NULL,
    capacity INT NOT NULL DEFAULT 0, 
    availability INT DEFAULT 0, 
    goal INT DEFAULT 0,
	PRIMARY KEY(set_key));
-- populated by SetIsAvail with capacity and availability

-- populate capacity and availability
DROP PROCEDURE IF EXISTS SetIsAvail;
 DELIMITER //
 CREATE PROCEDURE SetIsAvail()
   BEGIN
    delete from structured_data_counts;    
    INSERT INTO structured_data_counts 
    SELECT set_key, SUM(full_count), SUM(full_count) FROM (
		SELECT sd.basesets AS set_key, r2.count AS full_count
		FROM structured_data sd
		JOIN raw_inventory r2
		ON sd.basesets & r2.basesets
	) tmp
    WHERE full_count IS NOT NULL
    GROUP BY set_key
    ;
   END //
 DELIMITER ;

	
DROP PROCEDURE IF EXISTS GetStructData;
 DELIMITER //
 CREATE PROCEDURE GetStructData()
   BEGIN
    delete from structured_data_counts;    
    INSERT INTO structured_data_counts
    SELECT set_key, SUM(full_count) FROM (
		SELECT r1.basesets | r2.basesets AS set_key, r2.count AS full_count
		FROM raw_inventory r1
		JOIN raw_inventory r2
		ON r1.basesets & r2.basesets AND r1.basesets != r2.basesets
	) tmp
    WHERE full_count IS NOT NULL
    GROUP BY set_key
    ;
   END //
 DELIMITER ;


-- staff that could be usefull
DROP PROCEDURE IF EXISTS CreateBaseStructData;
DELIMITER //
CREATE PROCEDURE CreateBaseStructData()
BEGIN
   DECLARE set_key INT;
   SET set_key = 0;
   label1: REPEAT
     SET set_key = set_key + 1;
	 -- insert in the table
   UNTIL set_key >= POW(2, 20) 
   END REPEAT label1;
END; //
DELIMITER ;

-- filling structured_data with capacity and availability from raw_inventory 
-- creating structured data 
DROP TABLE structured_data_counts;
CREATE TABLE structured_data_counts(
    set_key BIGINT DEFAULT NULL,
    capacity BIGINT NOT NULL DEFAULT 0, 
	PRIMARY KEY(set_key));
-- populated by ProcessInput.java except capacity and availability


