/* 
  Populates existing initial structured and raw data tables from JSON file
 the process:
 before the start run on cmd prompt:
   java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.36-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin ProcessInputInc
 then luanch the script:
   "C:\Program Files\MySQL\MySQL Server 5.6\bin\mysql.exe" "--defaults-file=C:\ProgramData\MySQL\MySQL Server 5.6\my.ini" "--verbose" "-uroot" "-pIraAnna12" < "C:\Users\ypolyako\workspace\TestModel\sql\RunDemo.sql"
 */
 
USE Demo;

-- create temporary table to insert next rank rows
DROP /*TEMPORARY*/ TABLE IF EXISTS unions_last_rank;
CREATE /*TEMPORARY*/ TABLE unions_last_rank(
    set_key BIGINT DEFAULT NULL,
    set_name VARCHAR(20) DEFAULT NULL,
--    rank INT DEFAULT NULL,
    capacity INT DEFAULT NULL, 
    availability INT DEFAULT NULL, 
    goal INT DEFAULT 0,
    PRIMARY KEY(set_key))
;

DROP /*TEMPORARY*/ TABLE IF EXISTS unions_next_rank;
CREATE /*TEMPORARY*/ TABLE unions_next_rank(
    set_key BIGINT DEFAULT NULL,
    set_name VARCHAR(20) DEFAULT NULL,
--    rank INT DEFAULT NULL,
    capacity INT DEFAULT NULL, 
    availability INT DEFAULT NULL, 
    goal INT DEFAULT 0,
    PRIMARY KEY(set_key))
;

DROP TABLE IF EXISTS raw_inventory;
CREATE TABLE raw_inventory AS 
SELECT basesets, sum(count) as count
   FROM raw_inventory_ex
   GROUP BY basesets; -- adds up multiple records in raw_inventory_ex with the same key
   
-- that adds capacities and availabilities to structured data
UPDATE structured_data_base sdb,
  (SELECT set_key, SUM(ri.count) AS capacity, SUM(ri.count) AS availability
   FROM structured_data_base
   JOIN raw_inventory ri
   ON set_key & ri.basesets != 0
   GROUP BY set_key) comp
 SET sdb.capacity = comp.capacity,
     sdb.availability = comp.availability
 WHERE sdb.set_key = comp.set_key;

-- populate inventory sets table
TRUNCATE structured_data_inc;
INSERT INTO structured_data_inc 
   SELECT set_key  -- inventory set's key
   , set_name, capacity, availability, goal
   FROM structured_data_base
   WHERE capacity IS NOT NULL;
    
-- start union_next_rank table
TRUNCATE unions_next_rank;
INSERT INTO unions_next_rank 
   SELECT *
   FROM structured_data_inc;

-- start unions_prev_rank table
-- INSERT INTO unions_prev_rank 
--    SELECT BIT_OR(set_key)  -- highest rank union set's key
--    , NULL, NULL, NULL, NULL, 0
--    FROM structured_data_inc;

-- SET max_sp_recursion_depth=255;
CALL AddUnions; -- creates unions
-- call PopulateWithNumbers; -- adds capacities and availabilities to structured_data_inc
-- call EliminateUnions; -- deleats non-overlapping unions creatd by AdUnions

-- call CompactStructData;
-- call CompactStructData; -- called twice to compact new nodes of higher rank
