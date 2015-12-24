select lpad(bin(set_key_new), 10, '0') as setkey_new, lpad(bin(set_key_old), 10, '0') as setkey_old from fully_included_sets;

select set_key_is, lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_base;
select lpad(bin(set_key), 10, '0') as set_key, set_name, rank, capacity, availability, goal from structured_data_inc -- order by capacity
limit 30
;
select lpad(bin(set_key), 10, '0') as set_key, set_name, rank, capacity, availability, goal from unions_next_rank; -- order by capacity

-- call GetItemsFromSD();

select lpad(bin(basesets), 10, '0') as set_key, count from raw_inventory;	

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
