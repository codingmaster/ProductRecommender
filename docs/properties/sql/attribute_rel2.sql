use $SCHEMA_NAME$;
use melovely_test;

drop view if exists attribute_source;

create view attribute_source
as 
select * from product_tmp_struct_vals 
		union 
		select * from product_tmp_category;

drop table if exists attribute;
create table attribute as 
select t1.value as val1, t1.attribute_code as attribute_code1, t2.value as val2, t2.attribute_code as attribute_code2, t2.option_id as valid2, count(t1.value) as val1Val2Count from
    (SELECT entity_id, value, attribute_code
    FROM attribute_source
    where attribute_code = 'gender'
) as t1
	join
    (select option_id, entity_id, attribute_code, value from attribute_source) as t2 
	ON t1.entity_id = t2.entity_id
        and t1.value != t2.value
group by t1.value , t2.value, t2.option_id
;

drop table if exists attribute_rel2;
create table attribute_rel2
as
SELECT t1.attribute_code1 as attribute_code, t1.val1, t1.val2, (2*t1.sumVal1Val2)/(t2.sumVal1 + t3.sumVal2) as rel,
t1.sumVal1Val2/t2.sumVal1 as semrel
FROM
(SELECT 
attr1.attribute_code1,
attr1.val1,
attr2.val1 as val2,
sum(attr1.val1Val2Count * attr2.val1Val2Count) as sumVal1Val2
FROM attribute as attr1 
join attribute as attr2
on attr1.val2 = attr2.val2 
and attr1.valid2 = attr2.valid2
and attr1.attribute_code1 = attr2.attribute_code1
group by val1, val2) as t1
JOIN
(SELECT val1, sum(val1Val2Count * val1Val2Count) as sumVal1 FROM attribute group by val1) as t2 on t1.val1 = t2.val1
JOIN
(SELECT val1, sum(val1Val2Count * val1Val2Count) as sumVal2 FROM attribute group by val1) as t3 on t1.val2 = t3.val1
order by val1, rel desc;

select * from attribute_rel2 limit 60000;
