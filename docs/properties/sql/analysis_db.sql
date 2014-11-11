DROP SCHEMA IF EXISTS $SCHEMA_NAME$ ;
CREATE SCHEMA $SCHEMA_NAME$;

USE $BASE_SCHEMA$;
CREATE TABLE $SCHEMA_NAME$.attribute_option as
SELECT distinct
attr.attribute_code,
opt_val.option_id,
opt_val.value
FROM eav_attribute attr
 JOIN eav_entity_type attr_type on attr.entity_type_id = attr_type.entity_type_id
 JOIN eav_attribute_option opt on attr.attribute_id = opt.attribute_id
 JOIN eav_attribute_option_value opt_val on opt.option_id = opt_val.option_id
where attr_type.entity_type_code in ('catalog_product') 
-- and attr.attribute_code in ($ATTRIBUTES$);
;

create table $SCHEMA_NAME$.product_img as
SELECT entity_id, value, val.value_id FROM catalog_product_entity_media_gallery as gal
join magento_melovely_6.catalog_product_entity_media_gallery_value as val 
on gal.value_id = val.value_id 
where disabled = 0
group by entity_id 
having min(position);


create table $SCHEMA_NAME$.visible_products as 
SELECT entity_id FROM catalog_product_flat_1 where visibility = 4;


create table $SCHEMA_NAME$.product_tmp as
select distinct
entity_id,
attribute_code,
value
from
(
-- varchar values
 select 
        ent.entity_id AS entity_id,
        ent.value AS value,
        attr.attribute_code AS attribute_code,
        attr.frontend_label AS frontend_label
    from
        catalog_product_entity_varchar ent
        join eav_attribute attr ON (ent.attribute_id = attr.attribute_id)
union all
-- text values
select 
        ent.entity_id AS entity_id,
        ent.value AS value,
        attr.attribute_code AS attribute_code,
        attr.frontend_label AS frontend_label
    from
        catalog_product_entity_text ent
        join eav_attribute attr ON (ent.attribute_id = attr.attribute_id)
) as t

where 
t.entity_id between $MIN_PRODUCT$ and $MAX_PRODUCT$
order by t.entity_id
;

create table $SCHEMA_NAME$.category as 
SELECT distinct cat.entity_id as category_id, parent_id, children_count, level, value, path
FROM catalog_category_entity as cat
JOIN catalog_category_entity_varchar as cat_var
ON cat.entity_id = cat_var.entity_id
where 
-- product_category.product_id = 49 and 
cat_var.attribute_id = 35
order by category_id;

 create table $SCHEMA_NAME$.product_category as
 select product_id, cat.category_id
from $SCHEMA_NAME$.category as cat
join catalog_category_product as product_category
on cat.category_id = product_category.category_id
order by product_id;


create table $SCHEMA_NAME$.product_extended as
select distinct
entity_id,
value,
t.attribute_code,
frontend_label
from

(

-- varchar values
 select 
        ent.entity_id AS entity_id,
        ent.value AS value,
        attr.attribute_code AS attribute_code,
        attr.frontend_label AS frontend_label
    from
        catalog_product_entity_varchar ent
        join eav_attribute attr ON (ent.attribute_id = attr.attribute_id)
union all
-- text values
select 
        ent.entity_id AS entity_id,
        ent.value AS value,
        attr.attribute_code AS attribute_code,
        attr.frontend_label AS frontend_label
    from
        catalog_product_entity_text ent
        join eav_attribute attr ON (ent.attribute_id = attr.attribute_id)
union all
-- int values
 select 
        ent.entity_id AS entity_id,
        ent.value AS value,
        attr.attribute_code AS attribute_code,
        attr.frontend_label AS frontend_label
    from
        catalog_product_entity_int ent
        join eav_attribute attr ON (ent.attribute_id = attr.attribute_id)

) as t
where 
t.entity_id between $MIN_PRODUCT$ and $MAX_PRODUCT$
order by t.entity_id
;

CREATE TABLE  $SCHEMA_NAME$.similar_products AS
select product_id, linked_product_id from magento_melovely_6.catalog_product_link
where link_type_id =5;


USE $SCHEMA_NAME$;

CREATE TABLE numbers (
  n INT PRIMARY KEY);

INSERT INTO numbers VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

CREATE TABLE splited_opts as
SELECT
  prod.entity_id, prod.attribute_code, 
  SUBSTRING_INDEX(SUBSTRING_INDEX(prod.value, ',', numbers.n), ',', -1) option_id
FROM
  numbers 
  INNER JOIN product_extended as prod
  ON CHAR_LENGTH(prod.value)
     -CHAR_LENGTH(REPLACE(prod.value, ',', ''))>= numbers.n-1;

CREATE TABLE product_options as
SELECT entity_id, opt.attribute_code, opt.option_id, value FROM splited_opts as split
JOIN attribute_option as opt on split.option_id = opt.option_id
;

create table product_tmp2 as
select distinct * from (
SELECT 
entity_id,
attribute_code,
option_id,
value
FROM 
product_options
UNION ALL 
SELECT 
entity_id,
attribute_code,
'',
value
FROM product_tmp
UNION ALL
SELECT
entity_id,
'img' as attribute_code,
value_id,
value
FROM product_img) as t
order by t.entity_id;

create table product as 
select 
prod.entity_id,
attribute_code,
option_id,
value
from 
visible_products as vis left join product_tmp2 as prod
on vis.entity_id = prod.entity_id
order by prod.entity_id;






ALTER TABLE product ADD PRIMARY KEY (entity_id, attribute_code, option_id) ;
ALTER TABLE category ADD PRIMARY KEY (category_id) ;
ALTER TABLE product_category ADD PRIMARY KEY (product_id, category_id) ;
ALTER TABLE similar_products ADD PRIMARY KEY (product_id, linked_product_id) ;
