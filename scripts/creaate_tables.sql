use test_fia;

-- create raw_data table to fill up by 
CREATE TABLE RAW_DATA (criteia BIGINT NOT NULL, 
    count INT,
    PRIMARY KEY (criteia));

DROP TABLE IF EXISTS STRUCT_DATA;
-- creating structured data 
CREATE TABLE STRUCT_DATA (set_map BIGINT, 
    full_count INT);
-- and filling them with RAW_DATA
INSERT INTO STRUCT_DATA
SELECT set_map, SUM(full_count) FROM (
SELECT r1.criteia AS set_map, r2.count AS full_count
FROM RAW_DATA r1
LEFT OUTER JOIN RAW_DATA r2
ON r1.criteia & r2.criteia) tmp
WHERE full_count IS NOT NULL
GROUP BY set_map
;


-- getting amount of items from structured data
 DROP PROCEDURE IF EXISTS GetItems;
 DELIMITER //
 CREATE PROCEDURE GetItems(IN iset BIGINT, IN amount INT)
   BEGIN
    DECLARE cnt INT;
   
    SELECT full_count INTO cnt FROM struct_data WHERE set_map = iset;
 
    IF cnt >= amount AND amount > 0 AND BIT_COUNT(iset)=1
    THEN
	 SELECT 'ok';
     UPDATE struct_data set full_count=full_count-amount WHERE (set_map & iset)>0;
     DROP TABLE IF EXISTS struct_data_tmp;
     CREATE TABLE struct_data_tmp SELECT * FROM struct_data;
     UPDATE struct_data sd SET full_count = (SELECT min(sdt.full_count) FROM struct_data_tmp sdt
       WHERE (sd.set_map & sdt.set_map) = sd.set_map AND sd.set_map <= sdt.set_map);
     -- DROP TABLE IF EXISTS struct_data_final;
     -- CREATE TABLE struct_data_final AS 
     --    SELECT sd.set_map, IF(sd.full_count > sdt.full_count, sdt.full_count, sd.full_count) as full_count
     --    FROM struct_data sd JOIN struct_data_tmp sdt ON sdt.set_map = sd.set_map;     
	ELSE
     SELECT 'not ok';
    END IF;
   END //
 DELIMITER ;
 
select bin(set_map), set_map, full_count from struct_data;

select * from struct_data;

-- create weighted data table for run-time testing
create table raw_data_weighted(criteia BIGINT NOT NULL, 
    count INT, 
    weight INT,
    primary key (weight));

-- clear weighted datga table
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
