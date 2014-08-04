CREATE TABLE RAW_DATA (criteia BIGINT NOT NULL, 
    count INT,
    PRIMARY KEY (criteia));

mysql> select * from raw_data;
+---------+-------+
| criteia | count |
+---------+-------+
|       0 |     5 |
|       1 |    12 |
|       2 |    13 |
|       3 |     3 |
|       4 |     0 |
|       5 |    12 |
|       6 |    14 |
|       7 |     1 |
|       8 |     5 |
|       9 |    15 |
|      10 |     8 |
|      11 |     8 |
|      12 |     3 |
|      13 |     7 |
|      14 |     0 |
|      15 |    10 |
+---------+-------+
16 rows in set (0.00 sec)

DROP TABLE IF EXISTS STRUCT_DATA;
-- creating structured data 
CREATE TABLE STRUCT_DATA (set_map BIGINT, 
    full_count INT);
-- and filling them raw_data
INSERT INTO STRUCT_DATA
SELECT set_map, SUM(full_count) FROM (
SELECT r1.criteia AS set_map, r2.count AS full_count
FROM RAW_DATA r1
LEFT OUTER JOIN RAW_DATA r2
ON r1.criteia & r2.criteia) tmp
WHERE full_count IS NOT NULL
GROUP BY set_map
;

mysql> select * from struct_data;
+---------+------------+
| set_map | full_count |
+---------+------------+
|       1 |         68 |
|       2 |         57 |
|       3 |        103 |
|       4 |         47 |
|       5 |         85 |
|       6 |         79 |
|       7 |        106 |
|       8 |         56 |
|       9 |         84 |
|      10 |         87 |
|      11 |        111 |
|      12 |         83 |
|      13 |         98 |
|      14 |         99 |
|      15 |        111 |
+---------+------------+
15 rows in set (0.00 sec)

mysql> select sum(count) from raw_data
    -> where criteia!=0;
+------------+
| sum(count) |
+------------+
|        111 |
+------------+
1 row in set (0.02 sec)

-- updating structured data with rule #2. Do we need it ????
DELIMITER //
CREATE FUNCTION AdjustCount (p_set_map BIGINT, p_count INT)
  RETURNS INT
BEGIN
  DECLARE ret INT;
  SET ret = (select min(sd.full_count) from struct_data sd where (p_set_map & sd.set_map) = sd.set_map);
  RETURN ret;
END; //
DELIMITER ;

-- getting amount of items from structured data
 DROP PROCEDURE IF EXISTS GetItems;
 DELIMITER //
 CREATE PROCEDURE GetItems(IN iset BIGINT, IN amount INT)
   BEGIN
    DECLARE cnt INT;
   
    SELECT full_count INTO cnt FROM struct_data WHERE set_map = iset;
 
    IF cnt > amount THEN
	 SELECT 'ok';
     UPDATE struct_data set full_count=full_count-amount WHERE (set_map & iset)>0;
     DROP TABLE IF EXISTS struct_data_tmp;
     CREATE TABLE struct_data_tmp SELECT * FROM struct_data;
     UPDATE struct_data_tmp sdt SET full_count = (SELECT min(sd.full_count) FROM struct_data sd 
       WHERE (sd.set_map & sdt.set_map) = sdt.set_map AND sdt.set_map <= sd.set_map);
     DROP TABLE IF EXISTS struct_data_final;
     CREATE TABLE struct_data_final AS 
         SELECT sd.set_map, IF(sd.full_count > sdt.full_count, sdt.full_count, sd.full_count) as full_count
         FROM struct_data sd JOIN struct_data_tmp sdt ON sdt.set_map = sd.set_map;     
     -- UPDATE struct_data sd set full_count = IF (
     --    SELECT min(sdt.full_count) AS full_count_tmp FROM struct_data_tmp sdt WHERE (sd.set_map & sdt.set_map) = sd.set_map AND sd.set_map < sdt.set_map 
     --    < sd.full_count, sd.full_count, full_count_tmp);
	ELSE
     SELECT 'not';
    END IF;
   END //
 DELIMITER ;
 
 select bin(set_map), full_count from struct_data;

select * from struct_data;
DROP TABLE struct_data;
CREATE TABLE struct_data SELECT * FROM struct_data_tmp;
SELECT min(sd.full_count) AS full_count_tmp FROM struct_data_tmp sd WHERE (set_map & sd.set_map) = set_map AND set_map < sd.set_map;
