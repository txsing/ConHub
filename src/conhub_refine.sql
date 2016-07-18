--> refined
drop database consql;
create database consql;

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
	imageid varchar(64) references images(imageid),
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
	regname varchar(24) references registries(regname),

	unique(reponame, regname)
);

create table tags(
	tag varchar(24),
	imageid varchar(64) references images(imageid),
	repoid varchar(18) references repositories(repoid),

	primary key(tag, repoid)
);

create table cprelation (
	clayer varchar(64);
	player varchar(64);

);
--->original

create table container (
	conid varchar(64) primary key,
	imageid varchar(64) references image(imageid),
	command text,
	createdate varchar(30),
	status varchar(12),
	ports varchar(20),
	name varchar(24),
	builder varchar(12)
);

create table conToImage(
	conid varchar(64),
	imageid varchar(64),
	command text
);

create table convolumes(
	conid varchar(64) references container(conid),
	vid varchar(64),
	path varchar(45)
);

create table conCmdHistory(
	conid varchar(64),
	logid varchar(12)
);

create table commandlog(
	logid varchar(12),
	createdate varchar(20),
	command text
);