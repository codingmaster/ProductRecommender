DROP SCHEMA IF EXISTS $SCHEMA_NAME$ ;
CREATE SCHEMA $SCHEMA_NAME$;

USE $BASE_SCHEMA$;

CREATE TABLE $SCHEMA_NAME$.all_attribute_option as
SELECT distinct
attr.attribute_id,
attr.attribute_code,
attr.backend_type,
opt_val.option_id,
opt_val.value
FROM eav_attribute attr
 JOIN eav_entity_type attr_type on attr.entity_type_id = attr_type.entity_type_id
 LEFT JOIN eav_attribute_option opt on attr.attribute_id = opt.attribute_id
 LEFT JOIN eav_attribute_option_value opt_val on opt.option_id = opt_val.option_id
where attr_type.entity_type_code in ('catalog_product');


CREATE TABLE $SCHEMA_NAME$.attribute_option as
SELECT * from $SCHEMA_NAME$.all_attribute_option
where attribute_code in ($STRUCT_ATTRIBUTES$)
;


create view $SCHEMA_NAME$.visible_products as 
SELECT distinct product_id as entity_id
FROM catalog_category_product as cat_p 
join catalog_category_flat_store_1 as cat
on cat.entity_id = cat_p.category_id
join catalog_product_flat_1 as prod
on cat_p.product_id = prod.entity_id
where 
visibility != 1;


create table $SCHEMA_NAME$.product_tmp as
select 
vis.entity_id, ent.attribute_id, attr.attribute_code, vis.entity_id as option_id , ent.value
 from
$SCHEMA_NAME$.visible_products as vis
join
(
select * from catalog_product_entity_varchar where entity_type_id = 4
union all 
select * from catalog_product_entity_int where entity_type_id = 4
union all 
select * from catalog_product_entity_text where entity_type_id = 4) as ent
on vis.entity_id = ent.entity_id
join eav_attribute as attr on ent.attribute_id = attr.attribute_id
order by vis.entity_id;

create table $SCHEMA_NAME$.product_tmp_struct
as select * from $SCHEMA_NAME$.product_tmp 
WHERE attribute_code IN ($STRUCT_ATTRIBUTES$);

create table $SCHEMA_NAME$.product_tmp_unstruct
as select * from $SCHEMA_NAME$.product_tmp 
WHERE attribute_code IN ($UNSTRUCT_ATTRIBUTES$);

create table $SCHEMA_NAME$.product_tmp_split
as select * from $SCHEMA_NAME$.product_tmp 
WHERE attribute_code IN ($SPLIT_ATTRIBUTES$);


create table $SCHEMA_NAME$.category as 
SELECT distinct cat.entity_id as category_id, parent_id, children_count, level, value, path
FROM catalog_category_entity as cat
JOIN catalog_category_entity_varchar as cat_var
ON cat.entity_id = cat_var.entity_id
where 
-- product_category.product_id = 49 and 
cat_var.attribute_id = 35
order by category_id;
ALTER TABLE $SCHEMA_NAME$.category ADD PRIMARY KEY (category_id) ;


 create table $SCHEMA_NAME$.product_tmp_category as
 select product_id, cat.category_id
from $SCHEMA_NAME$.category as cat
join catalog_category_product as product_category
on cat.category_id = product_category.category_id
order by product_id;
ALTER TABLE $SCHEMA_NAME$.product_tmp_category ADD PRIMARY KEY (product_id, category_id) ;


CREATE TABLE $SCHEMA_NAME$.recommendation (
  product_id int(10) unsigned NOT NULL DEFAULT '0' ,
  linked_product_id int(10) unsigned NOT NULL DEFAULT '0' ,
  type varchar(32) CHARACTER SET utf8 NOT NULL ,
  position int(10) unsigned NOT NULL DEFAULT '0',
  DTYPE varchar(255) NOT NULL DEFAULT '',
  score double DEFAULT NULL,
  relative_score double DEFAULT NULL,
  PRIMARY KEY (product_id,type,position)
);



INSERT $SCHEMA_NAME$.recommendation
SELECT product_id, linked_product_id, code as type, link_id as position, 'SystemRecommendation' as DTYPE, null as score, null as relative_score 
FROM $BASE_SCHEMA$.catalog_product_link as l
join $BASE_SCHEMA$.catalog_product_link_type as lt 
on l.link_type_id = lt.link_type_id
join $SCHEMA_NAME$.visible_products as vis
on l.linked_product_id = vis.entity_id;



USE $SCHEMA_NAME$;

CREATE TABLE numbers (
  n INT PRIMARY KEY);

INSERT INTO numbers VALUES 
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),
(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),
(51),(52),(53),(54),(55),(56),(57),(58),(59),(60);

CREATE TABLE splited_opts as
SELECT
  prod.entity_id, prod.attribute_id, 
  SUBSTRING_INDEX(SUBSTRING_INDEX(prod.value, ',', numbers.n), ',', -1) option_id
FROM
  numbers 
  INNER JOIN product_tmp_struct as prod
  ON CHAR_LENGTH(prod.value)
     -CHAR_LENGTH(REPLACE(prod.value, ',', ''))>= numbers.n-1
;

create table product_unstruct as
SELECT 'unstruct' as type, entity_id, attribute_id, attribute_code, option_id, value
FROM product_tmp_unstruct;

create table product_struct as
SELECT 'struct' as type, entity_id, split.attribute_id, attribute_code, CAST(split.option_id AS SIGNED) as option_id, opts.value  
FROM splited_opts as split
left join
all_attribute_option as opts 
on split.attribute_id = opts.attribute_id and split.option_id = opts.option_id
where opts.value != "" and opts.value is not null
order by entity_id;     
     
CREATE TABLE product_options as
SELECT entity_id, opt.attribute_code, opt.option_id, value FROM splited_opts as split
JOIN all_attribute_option as opt on split.option_id = opt.option_id
;

CREATE TABLE product_split as
select 'split' as type, entity_id, attribute_id, attribute_code, n as option_id, 
replace(replace(replace(replace(trim(value), '-',''), ')', ''), '(',''), '*','') as value
from
(
SELECT
  prod.entity_id, numbers.n, prod.attribute_id, prod.attribute_code,
  SUBSTRING_INDEX(SUBSTRING_INDEX(prod.value, ',', numbers.n), ',', -1) value
FROM
  numbers 
  INNER JOIN product_tmp_split as prod
  ON CHAR_LENGTH(prod.value)
     -CHAR_LENGTH(REPLACE(prod.value, ',', ''))>= numbers.n-1
 where prod.attribute_code in ($SPLIT_ATTRIBUTES$)) as t
where value is not null and value != "" and value REGEXP '^[A-Za-z0-9 ]+$'
;

create table product_category
as
select 'cat' as type, prod_cat.product_id as entity_id, 207 as attribute_id, 'category' as attribute_code, cat.category_id as option_id, CONCAT_WS('_', cat.category_id, cat.value) as value 
FROM category as cat
join product_tmp_category as prod_cat
on cat.category_id = prod_cat.category_id
where cat.category_id > 2 and cat.children_count = 0 and (cat.path like '1/2/4/%' or cat.path like '1/2/15%');

create table product_img
as
SELECT 'img' as type, entity_id, 37 as attribute_id, 'img' as attribute_code, gal.value_id as option_id, value
FROM 
$BASE_SCHEMA$.catalog_product_entity_media_gallery as gal
join $BASE_SCHEMA$.catalog_product_entity_media_gallery_value as val 
on gal.value_id = val.value_id
where value like '%.jpg';


create table product as 
SELECT * FROM
(
SELECT * FROM product_struct
UNION
SELECT * FROM product_split
UNION
SELECT * FROM product_unstruct
UNION 
SELECT * FROM product_category
UNION
SELECT * FROM product_img
) as t
where entity_id in (select entity_id from visible_products)
order by entity_id;

ALTER TABLE product ADD PRIMARY KEY (entity_id, attribute_code, option_id) ;

