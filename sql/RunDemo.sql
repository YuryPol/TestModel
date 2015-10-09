/* 
  Populates existing initial structured and raw data tables from JSON file
 the process:
 before the start run on cmd prompt:
   java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.36-bin.jar";C:/Users/ypolyako/workspace/TestModel/bin ProcessInputInc
 then luanch the script:
   "C:\Program Files\MySQL\MySQL Server 5.6\bin\mysql.exe" "--defaults-file=C:\ProgramData\MySQL\MySQL Server 5.6\my.ini" "--verbose" "-uroot" "-pIraAnna12" < "C:\Users\ypolyako\workspace\TestModel\sql\RunDemo.sql"
 */
 
USE Demo;

DROP TABLE IF EXISTS raw_inventory;
CREATE TABLE raw_inventory AS 
SELECT basesets, sum(count) as count
   FROM raw_inventory_ex
   GROUP BY basesets;
call PopulateWithNumbers; -- that adds capacities and availabilities to structured_data_inc
DROP TABLE IF EXISTS structured_data_base;
CREATE TABLE structured_data_base AS 
    SELECT set_key as set_key_is -- inventory set's key
    , set_name, capacity, availability, goal, set_key -- effective key
    FROM structured_data_inc;
call AddUnions; -- creates unions of first rank
call PopulateWithNumbers; -- adds capacities and availabilities to structured_data_inc
call EliminateUnions; -- deleats non-overlapping unions creatd by AdUnions
call CompactStructData;
call CompactStructData; -- called twice to compact new nodes of higher rank
