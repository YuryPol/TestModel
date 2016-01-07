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
    count INT NOT NULL,
    criteria VARCHAR(200) DEFAULT NULL)
;

-- create structured data table
DROP TABLE IF EXISTS structured_data_inc;
CREATE TABLE structured_data_inc(
    set_key BIGINT DEFAULT NULL,
    set_name VARCHAR(20) DEFAULT NULL,
--    rank INT DEFAULT NULL,
    capacity INT DEFAULT NULL, 
    availability INT DEFAULT NULL, 
    goal INT DEFAULT 0,
    PRIMARY KEY(set_key))
;
-- it will be initially populated by ProcessInputInc.java with 0-rank records

-- create structured data table
DROP TABLE IF EXISTS structured_data_inc2;
CREATE TABLE structured_data_inc2(
    set_key BIGINT DEFAULT NULL,
    set_name VARCHAR(20) DEFAULT NULL,
--    rank INT DEFAULT NULL,
    capacity INT DEFAULT NULL, 
    availability INT DEFAULT NULL, 
    goal INT DEFAULT 0,
    PRIMARY KEY(set_key))
;

-- create inventroy sets table
DROP TABLE IF EXISTS structured_data_base;
CREATE TABLE structured_data_base( 
    set_key_is BIGINT DEFAULT NULL,
    set_key BIGINT DEFAULT NULL,
    set_name VARCHAR(20) DEFAULT NULL,
    capacity INT DEFAULT NULL, 
    availability INT DEFAULT NULL, 
    goal INT DEFAULT 0,
    criteria VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY(set_key_is))
;
--  it will be populated after executing ProcessInputInc.java and call PopulateWithNumbers;

--
-- setting up stored procs
--

-- adds capacities, availabilities
DROP PROCEDURE IF EXISTS PopulateWithNumbers;
DELIMITER //
CREATE PROCEDURE PopulateWithNumbers()
BEGIN
 UPDATE structured_data_inc sd,
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
 SET sd.capacity = comp.capacity,
     sd.availability = comp.availability
 WHERE sd.set_key = comp.set_key
 ;
END //
DELIMITER ;

-- adds capacities, availabilities
DROP PROCEDURE IF EXISTS PopulateRankWithNumbers;
DELIMITER //
CREATE PROCEDURE PopulateRankWithNumbers()
BEGIN
 UPDATE unions_next_rank nr0,
 (SELECT 
    set_key,
    SUM(capacity) as capacity, 
    SUM(availability) as availability
  FROM (    
   SELECT nr.set_key, ri.count as capacity, ri.count as availability
    FROM unions_next_rank nr 
    JOIN raw_inventory ri 
    ON nr.set_key & ri.basesets != 0 
    WHERE nr.capacity is NULL
    ) blownUp
  GROUP BY set_key
 ) comp
 SET nr0.capacity = comp.capacity,
     nr0.availability = comp.availability
 WHERE nr0.set_key = comp.set_key
 ;
END //
DELIMITER ;

-- adds unions of higher ranks for all nodes
DROP PROCEDURE IF EXISTS AddUnions;
DELIMITER //
CREATE PROCEDURE AddUnions()
BEGIN
    DECLARE cnt INT;   
    DECLARE cnt_updated INT;
    REPEAT 
    SELECT count(*) INTO cnt FROM structured_data_inc;
    -- save rank table created in previous itteration
    TRUNCATE unions_last_rank;
    INSERT INTO unions_last_rank
	   SELECT * FROM unions_next_rank;
	TRUNCATE unions_next_rank;
	-- build next rank
	INSERT /*IGNORE*/ INTO unions_next_rank
       SELECT sb.set_key_is | lr.set_key, NULL, NULL, NULL, 0
	   FROM unions_last_rank lr
       JOIN structured_data_base sb
	   JOIN raw_inventory ri
           ON  (sb.set_key_is & ri.basesets != 0)
           AND (lr.set_key & ri.basesets) != 0
           AND (sb.set_key_is | lr.set_key) != lr.set_key
       GROUP BY sb.set_key_is | lr.set_key;
    CALL PopulateRankWithNumbers;
    -- delete fully included sets of lower rank
    DELETE FROM structured_data_inc
    WHERE EXISTS (
        SELECT *
        FROM unions_next_rank nr
        WHERE (structured_data_inc.set_key & nr.set_key) = structured_data_inc.set_key
        -- AND structured_data_inc.set_key != nr.set_key
        AND structured_data_inc.capacity = nr.capacity);    
    -- add temp table to sturctured data
    INSERT /*IGNORE*/ INTO structured_data_inc
    SELECT * FROM unions_next_rank;
     -- Continue adding unions of higher ranks
    SELECT count(*) INTO cnt_updated FROM structured_data_inc; 
    UNTIL  (cnt = cnt_updated) 
    END REPEAT;
    -- delete empty sets ???? Not here.
    DELETE FROM structured_data_inc
    WHERE capacity IS NULL;    
    -- link from structured_data_base
    UPDATE structured_data_base, structured_data_inc
    SET structured_data_base.set_key = structured_data_inc.set_key
    WHERE structured_data_base.set_key_is & structured_data_inc.set_key = structured_data_base.set_key_is
    AND structured_data_base.capacity = structured_data_inc.capacity;
END //
DELIMITER ;

-- adds unions of lower ranks for all nodes
-- DROP PROCEDURE IF EXISTS AddUnions1;
-- DELIMITER //
-- CREATE PROCEDURE AddUnions1()
-- BEGIN
--	SELECT BIT_OR(basesets)
--	FROM (
--        SELECT sb.set_key_is, ri.basesets
--        FROM structured_data_base sb
--        JOIN raw_inventory ri
--        ON (sb.set_key_is & ri.basesets) != 0
--    ) tmp;
-- END //
-- DELIMITER ;
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

-- DROP PROCEDURE IF EXISTS CompactStructData;
-- DELIMITER //
-- CREATE PROCEDURE CompactStructData()
-- BEGIN	
-- TRUNCATE struct_data_tmp;
-- INSERT INTO struct_data_tmp SELECT * FROM structured_data_inc;
---- delete nodes of lower rank with the same availability
-- DELETE FROM structured_data_inc
-- WHERE EXISTS (
--    SELECT *
--    FROM struct_data_tmp sdt
--    WHERE (structured_data_inc.set_key & sdt.set_key) = structured_data_inc.set_key
--    AND structured_data_inc.set_key > sdt.set_key
--    AND structured_data_inc.availability = sdt.availability);    
-- -- link from structured_data_base
-- UPDATE structured_data_base, structured_data_inc
--    SET structured_data_base.set_key = structured_data_inc.set_key
--    WHERE structured_data_base.set_key_is & structured_data_inc.set_key = structured_data_base.set_key_is
--    AND structured_data_base.availability = structured_data_inc.availability
--;
-- END //
-- DELIMITER ;

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
-- DROP PROCEDURE IF EXISTS GetItemsFromSD;
-- DELIMITER //
-- CREATE PROCEDURE GetItemsFromSD(IN iset BIGINT, IN amount INT)
-- BEGIN
--   IF BookItemsFromIS(iset, amount)
--   THEN
--     -- update structured data with rule #1
--     UPDATE structured_data_inc 
--        SET availability = availability - amount 
--        WHERE (set_key & iset) = iset;
--     -- update structured data with rule #2
--     TRUNCATE structured_data_inc2;
--     INSERT INTO structured_data_inc2 SELECT * FROM structured_data_inc;
--     UPDATE structured_data_inc sd, structured_data_inc2 sdt  
--       SET sd.availability = (SELECT min(sdt.availability) FROM structured_data_inc2 sdt
--         WHERE (sd.set_key & sdt.set_key) = sd.set_key AND sd.set_key <= sdt.set_key);       
--         -- propagate the changes into base table
--     UPDATE structured_data_base sb, structured_data_inc sd
--     SET sb.availability = LEAST(sb.availability, sd.availability)
--     WHERE sd.set_key & sb.set_key_is = sb.set_key_is;     
--     -- remove unneeded nodes
--     -- call CompactStructData;
--     -- call CompactStructData; -- called twice to compact new nodes of higher rank
--     SELECT 'passed';
--   ELSE
--     SELECT 'failed';
--   END IF;
-- END //
-- DELIMITER ;

-- getting an amount of items from structured data, second try with deleting lower order nodes.
DROP PROCEDURE IF EXISTS GetItemsFromSD;
DELIMITER //
CREATE PROCEDURE GetItemsFromSD(IN iset BIGINT, IN amount INT)
BEGIN
   IF BookItemsFromIS(iset, amount)
   THEN
     -- update structured data with rule #1
     UPDATE structured_data_inc 
        SET availability = availability - amount 
        WHERE (set_key & iset) = iset;
     -- update structured data with rule #2
     DROP TABLE IF EXISTS sdtmp;
     CREATE TABLE sdtmp AS SELECT set_key, availability FROM structured_data_inc WHERE set_key & iset = iset;
     DELETE FROM structured_data_inc WHERE set_key = ANY (
        SELECT sd1.set_key
        FROM sdtmp sd1 JOIN sdtmp sd2
        ON sd2.set_key > sd1.set_key 
        AND sd2.set_key & sd1.set_key = sd1.set_key 
        AND sd1.availability >= sd2.availability);        
     -- propagate the changes into base table
     UPDATE structured_data_base sb, structured_data_inc sd     
     SET sb.availability = LEAST(sb.availability, sd.availability)
     WHERE sd.set_key & sb.set_key_is = sb.set_key_is;     
     SELECT 'passed';
   ELSE
     SELECT 'failed';
   END IF;
END //
DELIMITER ;

--
-- testing aids
--

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
