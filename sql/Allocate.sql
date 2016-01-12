-- this try works.
call GetItemsFromSD(1,400000);
call GetItemsFromSD(2,600000);
call GetItemsFromSD(4,10000);
call GetItemsFromSD(8,280000);
call GetItemsFromSD(16,10000);
call GetItemsFromSD(64,10000);

-- another try with error
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(2,10000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(4,80000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(64,50000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(8,200000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(2,50000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(1,200000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(16,50000);
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
select lpad(bin(set_key), 10, '0') as set_key, set_name, capacity, availability, goal from structured_data_inc;
select set_key_is, set_name, capacity, availability, goal from structured_data_base;
call GetItemsFromSD(64, 230000);

     UPDATE structured_data_inc 
        SET availability = availability - 230000 
        WHERE (set_key & 64) = 64;

  GetItemsFromSD(1,200000);      
+------------+-------------+----------+--------------+------+
| set_key    | set_name    | capacity | availability | goal |
+------------+-------------+----------+--------------+------+
| 0000000100 | Eeasterners |   180000 |       100000 |    0 |
| 0000001000 | young women |   280000 |        80000 |    0 |
| 0000100010 | NULL        |   600000 |       540000 |    0 |
| 0000110010 | NULL        |   610000 |       550000 |    0 |

| 0001001000 | NULL        |   480000 |       230000 |    0 |
| 0101001101 | NULL        |   710000 |       180000 |    0 |

| 0101111111 | NULL        |  1310000 |       720000 |    0 |
+------------+-------------+----------+--------------+------+



        