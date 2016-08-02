\c postgres
drop database consql;
create database consql;
\c consql
create table images(
	imageid varchar(64) primary key,
	parentimageid varchar(64),
	dockerfileid varchar(12),
	size numeric(10),
	author varchar(24),
	builder varchar(24)
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

create table layercp(
        child varchar(64),
        parent varchar(64)
);