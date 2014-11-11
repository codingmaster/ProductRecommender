USE $SCHEMA_NAME$;
SELECT attribute_code, count(distinct value) / count(attribute_code)
FROM product
GROUP by attribute_code;

SELECT *
FROM product
where attribute_code = 'name'
GROUP BY value
HAVING count(value) > 1;


SELECT *
FROM product
where attribute_code = 'description'
GROUP BY value
HAVING count(value) > 1
order by value;


select t.count as product_count, count(t.count) as number
from
(select product_id, count(product_id) as count
from product_category
group by product_id
order by count desc) as t
group by t.count
order by number desc;

select t.rounded, count(t.rounded) as number
from
(select CONCAT_WS('_', cat.category_id, cat.value) as cat_value, count(product_id) as number
, CONCAT(round(count(product_id) / 100), '00') as rounded
 from product_category as prod_cat
join category as cat on 
prod_cat.category_id = cat.category_id
group by cat.category_id, cat_value) as t

group by t.rounded
order by t.rounded desc 
;


USE magento_melovely_6;
SELECT attr.attribute_id,
attr.entity_type_id,
attr.attribute_code,
opt_val.option_id,
opt_val.value

 FROM eav_attribute as attr 
LEFT JOIN eav_attribute_option as attr_opt
ON attr.attribute_id = attr_opt.attribute_id
LEFT JOIN eav_attribute_option_value as opt_val
ON attr_opt.option_id = opt_val.option_id 
where attr.entity_type_id = 4;
