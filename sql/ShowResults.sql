select lpad(bin(set_key_new), 10, '0') as setkey_new, lpad(bin(set_key_old), 10, '0') as setkey_old from fully_included_sets;

select lpad(bin(set_key), 10, '0') as set_key, set_name, rank, capacity, availability, goal from structured_data_inc;
select set_key_is, lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_base;
-- call GetItemsFromSD();

select lpad(bin(basesets), 10, '0') as set_key, count from raw_inventory;	
