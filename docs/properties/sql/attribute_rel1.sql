USE $SCHEMA_NAME$;
drop table if exists attribute_pairs;
create table attribute_pairs as
SELECT prod1.attribute_code as attribute_code, prod1.value as val1, prod2.value as val2, count(prod2.value) as number 
FROM product_tmp_struct_vals as prod1
JOIN product_tmp_struct_vals as prod2
on prod1.entity_id = prod2.entity_id
and prod2.attribute_code = prod1.attribute_code and prod2.value <= prod1.value
where prod1.attribute_code = 'color'
group by prod1.attribute_code, prod1.option_id, prod2.option_id, prod1.value, prod2.value;

drop table if exists attribute_pairs_freq;
create table attribute_pairs_freq 
as
 select t1.attribute_code, t1.val1, t1.val2, t1.number as val1Val2Count, t2.number as val1Count, t3.number as val2Count

 from
(
-- get number of attribute values occur together
SELECT attribute_code as attribute_code, val1, val2, number 
FROM attribute_pairs
) as t1
join
(
-- get overall number of attribute_codegeschmacksrichtung, value1 pair
select prod.attribute_code as attribute_code, prod.value as val1, count(prod.value) as number
from product_tmp_struct_vals as prod
group by attribute_code, prod.value
) as t2
on t1.val1 = t2.val1
and t1.attribute_code = t2.attribute_code
join
(
-- get overall number of attribute_code, valproductue2 pair
select prod.attribute_code as attribute_code, prod.value as val2, count(prod.value) as number
from product_tmp_struct_vals as prod
group by attribute_code, prod.value
) as t3
on t1.val2 = t3.val2
and t1.attribute_code = t3.attribute_code
order by t1.val1;

drop table if exists attribute_rel1;
create table attribute_rel1
as
select attribute_code, val1, val2, (2*val1Val2Count)/(val1Count+val2Count) as freq from attribute_pairs_freq;

select * from attribute_rel1;