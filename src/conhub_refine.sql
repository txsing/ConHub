\c postgres
drop database consql;
create database consql;
\c consql

create table layers(
        layerid varchar(64) primary key,
        parent varchar(64) references layers(layerid) On Delete Cascade
);


create table users(
        userid varchar(24) primary key,
        passwd varchar(12) not null,
        email  varchar(36)
)


create table images(
	imageid varchar(64) unique not null,
	parentimageid varchar(64),
	dockerfileid varchar(12),
	size numeric(10),
	author varchar(24),
        foreign key (imageid) references layers(layerid)
);


create table containers (
	conid varchar(64) primary key,
	imageid varchar(64) references images(imageid) On Delete Restrict,
	command text,
	createdate varchar(36),
	ports varchar(24),
	name varchar(24),
	builder varchar(24)
);


create table dockerfiles(
        fileid varchar(24) primary key,
        author varchar(24) references users(userid)
);


create table fileToImage(
        imageid varchar(64) references images(imageid),
        fileid  varchar(64) references dockerfiles(fileid),
        builder varchar(24) references users(userid)
);


create table registries(
	regname varchar(24) primary key,
	url varchar(64) unique not null,
	description text
);
insert into registries values('dockerhub','docker.io/library','default docker registry');


create table repositories (
	repoid varchar(18) primary key,
	reponame varchar(24),
	regname varchar(24) references registries(regname) On Delete Cascade,

	unique(reponame, regname)
);

create table tags(
	tag varchar(24),
	imageid varchar(64) references images(imageid) On Delete Cascade,
	repoid varchar(18) references repositories(repoid) On Delete Cascade,

	primary key(tag, repoid)
);

create table labels(
        label varchar(24),
        id    varchar(64)
);


