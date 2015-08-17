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

--
-- creating structured data 
--
DROP TABLE structured_data;
CREATE TABLE structured_data(
    set_key BIGINT DEFAULT NULL,
	set_name VARCHAR(20) DEFAULT NULL,
    capacity INT NOT NULL DEFAULT 0, 
    availability INT NOT NULL DEFAULT 0, 
    goal INT DEFAULT 0,
	PRIMARY KEY(set_key));
-- populated by ProcessInput.java except capacity and availability

DROP TABLE IF EXISTS FullStructData;
CREATE TABLE FullStructData(
	set_key BIGINT DEFAULT NULL,
	set_name VARCHAR(20) DEFAULT NULL,
	capacity INT DEFAULT 0, 
	availability INT DEFAULT 0, 
	goal INT DEFAULT 0,
	PRIMARY KEY(set_key));
-- first populated by ProcessInput.java with stor-proc CreateFullStructData(highBit)

DROP PROCEDURE IF EXISTS CreateFullStructData;
DELIMITER //
CREATE PROCEDURE CreateFullStructData(IN highBit INT)
BEGIN
   DECLARE skey INT DEFAULT 1;
   DELETE from FullStructData;
   WHILE skey & POW(2, highBit) = 0 DO
     INSERT IGNORE INTO FullStructData VALUES(skey, NULL, 0, 0, 0);
     SET skey = skey + 1;
   END WHILE
   ;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS AddUnionsToStructData;
DELIMITER //
CREATE PROCEDURE AddUnionsToStructData()
BEGIN
	INSERT IGNORE INTO structured_data
	SELECT fsd.set_key, null, 0, 0, 0
	FROM structured_data sd 
	JOIN FullStructData fsd 
	ON (fsd.set_key & sd.set_key 
	AND fsd.set_key >= sd.set_key)
	;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS PopulateStructDataWithNumbers;
DELIMITER //
CREATE PROCEDURE PopulateStructDataWithNumbers()
BEGIN
 SELECT 
	set_key,
	set_name,
	SUM(capacity), 
	SUM(availability), 
	goal
 FROM (
	SELECT fsd.set_key, fsd.set_name, ri.count as capacity, ri.count as availability, fsd.goal
	FROM FullStructData fsd 
	JOIN raw_inventory ri 
	ON fsd.set_key & ri.basesets != 0 ) blownUp
 GROUP BY set_key, set_name
 ;
END //
DELIMITER ;




