----------------------
-- the process:
---------------------
use test_fia;
-- if we want to change raw data
-- om cmd prompt:
-- java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.31-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin GenInput
-- on MySql prompt:
--
-- run always:
-- call GetWeightRawData;
-- call GetStructData;
-- start alotments 
-- select bin(set_map), set_map, full_count, availability, goal from struct_data;
-- deside on criteria, ammount
-- call GetItems(criteria, ammount);
--  -"- repeat until done
-- java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.31-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin Simulation


-- create raw_data table to fill up by 
CREATE TABLE raw_data (criteia BIGINT NOT NULL, 
    count INT,
    PRIMARY KEY (criteia));    
-- populated by GenInput.java

DROP TABLE IF EXISTS struct_data;

-- creating structured data 
CREATE TABLE struct_data (
    set_map BIGINT, 
    full_count INT, 
    availability INT, 
    goal INT);
    
-- and filling them with raw_data
DROP PROCEDURE IF EXISTS GetStructData;
 DELIMITER //
 CREATE PROCEDURE GetStructData()
   BEGIN
    delete from struct_data;    
    INSERT INTO struct_data
    SELECT set_map, SUM(full_count), SUM(full_count), 0 FROM (
    SELECT r1.criteia AS set_map, r2.count AS full_count
    FROM raw_data r1
    LEFT OUTER JOIN raw_data r2
    ON r1.criteia & r2.criteia) tmp
    WHERE full_count IS NOT NULL
    GROUP BY set_map
    ;
   END //
 DELIMITER ;

-- getting amount of items from structured data
 DROP PROCEDURE IF EXISTS GetItems;
 DELIMITER //
 CREATE PROCEDURE GetItems(IN iset BIGINT, IN amount INT)
   BEGIN
    DECLARE cnt INT;
   
    SELECT availability INTO cnt FROM struct_data WHERE set_map = iset;
 
    IF cnt >= amount AND amount > 0 AND BIT_COUNT(iset)=1
    THEN
	 SELECT 'ok';
     UPDATE struct_data set availability=availability-amount WHERE (set_map & iset)>0;
     UPDATE struct_data set goal=goal+amount WHERE set_map = iset;
     DROP TABLE IF EXISTS struct_data_tmp;
     CREATE TABLE struct_data_tmp SELECT * FROM struct_data;
     UPDATE struct_data sd SET availability = (SELECT min(sdt.availability) FROM struct_data_tmp sdt
       WHERE (sd.set_map & sdt.set_map) = sd.set_map AND sd.set_map <= sdt.set_map);
	ELSE
     SELECT 'not ok';
    END IF;
   END //
 DELIMITER ;
 
select bin(set_map), set_map, full_count, availability, goal 
from struct_data;

select * from struct_data;

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
        from raw_data r1
        join raw_data r2
        where r1.criteia >= r2.criteia
        and r1.count > 0
        group by r1.criteia, r1.count
    ;
   END //
 DELIMITER ;
    
-- clear result data table
delete from result_data;

select bin(criteia), criteia, count, weight from raw_data_weighted;
select bin(criteia), criteia, count from raw_data;

-- create result data table for run-time testing
create table result_data (set_map BIGINT NOT NULL, 
    count INT, 
    primary key (set_map));

select bin(set_map), set_map, count from result_data;

create table misses (criteia BIGINT NOT NULL, 
    count INT,
    primary key (criteia));
    

