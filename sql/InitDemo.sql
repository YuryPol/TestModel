/* 
  Creates needed tables and stored procs
  
  the process:
  to start om cmd prompt run sql script
  "C:\Program Files\MySQL\MySQL Server 5.6\bin\mysql.exe" "--defaults-file=C:\ProgramData\MySQL\MySQL Server 5.6\my.ini" "--verbose" "-uroot" "-pIraAnna12" < "C:\Users\ypolyako\workspace\TestModel\sql\InitDemo.sql"
 */
 
CREATE DATABASE IF NOT EXISTS Demo;

USE Demo;

--
-- setting up the tables
--

-- create raw_inventory_ex table to fill up by impressons' counts
DROP TABLE IF EXISTS raw_inventory_ex;
CREATE TABLE raw_inventory_ex(
    basesets BIGINT NOT NULL, 
    count INT NOT NULL
    -- , PRIMARY KEY (basesets)
    )
    ;

-- create structured data table
DROP TABLE IF EXISTS structured_data_inc;
CREATE TABLE structured_data_inc(
    set_key BIGINT DEFAULT NULL,
    set_name VARCHAR(20) DEFAULT NULL,
    rank INT DEFAULT NULL,
    capacity INT DEFAULT NULL, 
    availability INT DEFAULT NULL, 
    goal INT DEFAULT 0,
    PRIMARY KEY(set_key));
-- it will be initially populated by ProcessInputInc.java with 0-rank records

-- create inventroy sets tabel
DROP TABLE IF EXISTS structured_data_base;
CREATE TABLE structured_data_base AS 
    SELECT set_key as set_key_is -- inventory set's key
    , set_name, capacity, availability, goal, set_key -- effective key
    FROM structured_data_inc;
--  it will be populated after executing ProcessInputInc.java and call PopulateWithNumbers;

--
-- setting up stored procs
--

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
    ON sd.set_key & ri.basesets != 0 
    WHERE sd.capacity is NULL
    ) blownUp
  GROUP BY set_key
 ) comp
 SET sd0.capacity = comp.capacity,
     sd0.availability = comp.availability
 WHERE sd0.set_key = comp.set_key
 ;
END //
DELIMITER ;

-- adds unions of a higher rank for all nodes
DROP PROCEDURE IF EXISTS AddUnions;
DELIMITER //
CREATE PROCEDURE AddUnions()
BEGIN
    INSERT IGNORE INTO structured_data_inc
    SELECT sd1.set_key | sd2.set_key, NULL, NULL, NULL, NULL, 0
    FROM structured_data_inc sd1 
    JOIN structured_data_base sd2 
    ;
END //
DELIMITER ;
-- needs PopulateWithNumbers call to complete

-- deletes non-overlapping unions and 0 availability nodes
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
    OR structured_data_inc.availability IS NULL -- get rid from 0 availability nodes in structured data but keep them in the base   
;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS CompactStructData;
DELIMITER //
CREATE PROCEDURE CompactStructData()
BEGIN
-- finds fully inclusing sets
DROP TABLE IF EXISTS fully_included_sets;
CREATE TEMPORARY TABLE fully_included_sets AS
SELECT BIT_OR(set_key_new) as set_key_new, set_key_old
FROM (
    SELECT 
        s1.set_key as set_key_new, s2.set_key as set_key_old, s1.availability      
        FROM structured_data_inc s1 
        INNER JOIN structured_data_inc s2
        ON s1.set_key & s2.set_key = s2.set_key AND s1.set_key > s2.set_key AND s1.availability = s2.availability
    ) tmp
-- group matching fully inclusing sets into new union of higher rank
GROUP BY set_key_old
;
-- substitutes key in structured_data_inc for fully inclusing sets with union's key
/*
UPDATE structured_data_inc si, fully_included_sets fi
SET si.set_key = fi.set_key_new
WHERE si.set_key = fi.set_key_old
; -- TODO: this doesn't work.
*/
-- deletes fully inclusing sets' records
DELETE FROM structured_data_inc 
USING structured_data_inc INNER JOIN fully_included_sets
WHERE structured_data_inc.set_key=fully_included_sets.set_key_old
;
-- add new unions of higher rank
INSERT IGNORE INTO structured_data_inc
SELECT fully_included_sets.set_key_new, NULL, NULL, NULL, NULL, 0
FROM fully_included_sets 
LEFT OUTER JOIN structured_data_inc 
ON fully_included_sets.set_key_new = structured_data_inc.set_key
WHERE structured_data_inc.set_key IS NULL
;
call PopulateWithNumbers;
-- substitutes key in structured_data_base for fully inclusing sets with union's key
UPDATE structured_data_base sb, fully_included_sets fi
SET sb.set_key = fi.set_key_new
  , sb.availability = (SELECT si.availability FROM structured_data_inc si
                       WHERE si.set_key = fi.set_key_new)
WHERE sb.set_key = fi.set_key_old
;
END //
DELIMITER ;

-- book an amount of items
DROP FUNCTION IF EXISTS BookItemsFromIS;
DELIMITER //
CREATE FUNCTION BookItemsFromIS(iset BIGINT, amount INT)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE cnt INT;   
    SELECT availability INTO cnt FROM structured_data_base WHERE set_key_is = iset; 
    IF cnt >= amount AND amount > 0
    THEN
     UPDATE structured_data_base 
     SET availability=availability-amount, goal=goal+amount 
     WHERE set_key_is = iset;
     RETURN TRUE;
    ELSE
     RETURN FALSE;
    END IF;
END //
DELIMITER ;

-- getting an amount of items from structured data
DROP PROCEDURE IF EXISTS GetItemsFromSD;
DELIMITER //
CREATE PROCEDURE GetItemsFromSD(IN iset BIGINT, IN amount INT)
BEGIN
   IF BookItemsFromIS(iset, amount)
   THEN     
     UPDATE structured_data_inc 
     SET availability=availability-amount WHERE (set_key & iset)>0;
     DROP TABLE IF EXISTS struct_data_tmp;
     CREATE TABLE struct_data_tmp SELECT * FROM structured_data_inc;
     UPDATE structured_data_inc sd 
       SET availability = (SELECT min(sdt.availability) FROM struct_data_tmp sdt
         WHERE (sd.set_key & sdt.set_key) = sd.set_key AND sd.set_key <= sdt.set_key);
     call CompactStructData;
     call CompactStructData; -- called twice to compact new nodes of higher rank
     SELECT 'passed';
   ELSE
     SELECT 'failed';
   END IF;
END //
DELIMITER ;

--
-- testing aids
--
DROP PROCEDURE IF EXISTS TestGetItemsFromSD;
-- we shouldn't do it as it modifies the data
--DELIMITER //
--CREATE PROCEDURE TestGetItemsFromSD(IN iset BIGINT, IN amount INT)
--BEGIN
--   IF BookItemsFromIS(iset, amount)
--   THEN     
--     SELECT 'passed';
--   ELSE
--     SELECT 'failed';
--   END IF;
--END //
--DELIMITER ;

DROP PROCEDURE IF EXISTS GetTotalAvailability;
DELIMITER //
CREATE PROCEDURE GetTotalAvailability()
BEGIN
  SELECT 
    SUM(count) as total_capacity
  FROM raw_inventory; 
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS GetTotalGoals;
DELIMITER //
CREATE PROCEDURE GetTotalGoals()
BEGIN
  SELECT 
    SUM(goal) as total_goals
  FROM structured_data_base; 
END //
DELIMITER ;
