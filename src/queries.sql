WITH RECURSIVE parents(id, visited) AS(
	SELECT parent, 
               ARRAY[]::varchar[]
        FROM layers WHERE layerid = '5'
	UNION
	SELECT layers.parent, 
               (visited || parents.id)
        FROM parents, layers WHERE layers.layerid = parents.id
	AND parents.id NOT IN (SELECT imageid FROM images)
        AND NOT parents.id = ANY(visited)
) SELECT imageid FROM parents, images WHERE images.imageid = parents.id;

WITH RECURSIVE parents(id, visited) AS(
	SELECT parent, 
               ARRAY[]::varchar[]
        FROM layers WHERE layerid = '5'
	UNION
	SELECT layers.parent, 
               (visited || parents.id)
        FROM parents, layers WHERE layers.layerid = parents.id
        AND NOT parents.id = ANY(visited)
) SELECT imageid FROM parents, images WHERE images.imageid = parents.id;


create table layers(
        layerid varchar primary key,
        parent varchar references layers(layerid) On Delete Cascade
);

create table images(
	imageid varchar unique not null,
        foreign key (imageid) references layers(layerid)
);

Insert into layers values('1',null);
Insert into layers values('2','1');
Insert into layers values('3','2');
Insert into layers values('4','3');
Insert into layers values('5','4');
Insert into layers values('6','5');

Insert into images values('1');
Insert into images values('3');
Insert into images values('5');
