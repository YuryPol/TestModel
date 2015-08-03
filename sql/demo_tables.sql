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

-- create raw_inventory table to fill up by impressons' counts
DROP TABLE raw_inventory;
CREATE TABLE raw_inventory(
	basesets BINARY(10) NOT NULL, 
	count INT NOT NULL,
    PRIMARY KEY (basesets));    
-- populated by ProcessInput.java

-- creating structured data 
DROP TABLE structured_data;
CREATE TABLE structured_data(
    set_key BINARY(10) DEFAULT NULL,
	set_name VARCHAR(20) DEFAULT NULL,
    capacity INT NOT NULL DEFAULT 0, 
    availability INT DEFAULT 0, 
    goal INT DEFAULT 0,
	PRIMARY KEY(set_key));
-- populated by ProcessInput.java except capacity and availability
	
select hex(set_key), set_name from structured_data;	
select lpad(CONV(set_key,10,2), 20, '0'), set_name, capacity, availability from structured_data;	
select lpad(CONV(basesets,10,2), 20, '0'), count from raw_inventory;	

-- filling structured_data with capacity and availability from raw_inventory 
DROP PROCEDURE IF EXISTS GetStructData;
 DELIMITER //
 CREATE PROCEDURE GetStructData()
   BEGIN
    UPDATE structured_data 
	LEFT JOIN (
		SELECT set_map, SUM(full_count) FROM (
			SELECT r1.basesets AS set_map, r2.count AS full_count
			FROM raw_inventory r1
			LEFT OUTER JOIN raw_inventory r2
			ON r1.criteia & r2.criteia) tmp
		WHERE full_count IS NOT NULL
		GROUP BY set_map
	) tmp2
	ON (set_key )
	SET capacity=full_count, availability=full_count
    ;
   END //
 DELIMITER ;

-- getting amount of items from structured data
 DROP PROCEDURE IF EXISTS GetItems;
 DELIMITER //
 CREATE PROCEDURE GetItems(IN iset BIGINT, IN amount INT)
   BEGIN
    DECLARE cnt INT;
   
    SELECT availability INTO cnt FROM structured_data WHERE set_map = iset;
 
    IF cnt >= amount AND amount > 0 AND BIT_COUNT(iset)=1
    THEN
	 SELECT 'ok';
     UPDATE structured_data set availability=availability-amount WHERE (set_map & iset)>0;
     UPDATE structured_data set goal=goal+amount WHERE set_map = iset;
     DROP TABLE IF EXISTS struct_data_tmp;
     CREATE TABLE struct_data_tmp SELECT * FROM structured_data;
     UPDATE structured_data sd SET availability = (SELECT min(sdt.availability) FROM struct_data_tmp sdt
       WHERE (sd.set_map & sdt.set_map) = sd.set_map AND sd.set_map <= sdt.set_map);
	ELSE
     SELECT 'not ok';
    END IF;
   END //
 DELIMITER ;
 
select bin(set_map), set_map, full_count, availability, goal 
from structured_data;

select * from structured_data;

-- create weighted data table for run-time testing
create table raw_data_weighted(criteia BIGINT NOT NULL, 
    count INT, 
    weight INT,
    primary key (weight));

 DROP PROCEDURE IF EXISTS GetWeightRawData;
 DELIMITER //
 CREATE PROCEDURE GetWeightRawData()
   BEGIN
    -- clear weighted data table
    delete from raw_data_weighted; 
    -- populate weighted data table
    insert into raw_data_weighted
        select r1.criteia, r1.count, sum(r2.count) as weight
        from raw_inventory r1
        join raw_inventory r2
        where r1.criteia >= r2.criteia
        and r1.count > 0
        group by r1.criteia, r1.count
    ;
   END //
 DELIMITER ;
    
-- clear result data table
delete from result_data;

select bin(criteia), criteia, count, weight from raw_data_weighted;
select bin(criteia), criteia, count from raw_inventory;

-- create result data table for run-time testing
create table result_data (set_map BIGINT NOT NULL, 
    count INT, 
    primary key (set_map));

select bin(set_map), set_map, count from result_data;

create table misses (criteia BIGINT NOT NULL, 
    count INT,
    primary key (criteia));
    

