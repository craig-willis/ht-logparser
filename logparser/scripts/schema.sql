create database hathitrust;

create table pt (
    id numeric,
    ipaddr varchar(50),
    timestamp numeric,
    volume varchar(50),
    q1 varchar(50),
    pn varchar(10),
    seq varchar(10),
    attr varchar(10),
    view varchar(10),
    orient varchar(10),
    page varchar(10),
    size varchar(10),
    start varchar(10),
    skin varchar(10),
    referer varchar(512),
    url varchar(512)
);



create table ls (
    id numeric,
    ipaddr varchar(50),
    session varchar(50),
    timestamp numeric,
    anyall1 varchar(20),
    field1 varchar(20),
    q1 varchar(512),
    op2 varchar(10),
    anyall2 varchar(20),
    field2 varchar(20),
    q2 varchar(512),
    pn varchar(10),
    lmt varchar(10),
    topic varchar(50),
    author varchar(50),
    language varchar(50),
    country varchar(50),
    daterange varchar(50),
    datetrie varchar(50),
    format varchar(50),
    source varchar(50),
    numfound numeric,
    url varchar(512),
    referer varchar(512)
);

create table solr (
    id numeric,
    ipaddr varchar(50),
    session varchar(50),
    timestamp numeric,
    anyall1 varchar(20),
    field1 varchar(20),
    q1 varchar(512),
    op2 varchar(10),
    anyall2 varchar(20),
    field2 varchar(20),
    q2 varchar(512),
    pn varchar(10),
    lmt varchar(10),
    topic varchar(50),
    author varchar(50),
    language varchar(50),
    country varchar(50),
    daterange varchar(50),
    datetrie varchar(50),
    format varchar(50),
    source varchar(50),
    numfound numeric,
    url varchar(512),
    referer varchar(512)
);

create table pt_ls (
    pt_id numeric,
    ls_id numeric
);


create table solr_ls (
    solr_id numeric,
    ls_id numeric
);

create table pt_pt (
    pt_id_1 numeric,
    pt_id_2 numeric
);


load data infile 'pt.out' into table pt fields terminated by '|' lines terminated by '\n';
load data infile 'ls.out' into table ls fields terminated by '|' lines terminated by '\n';
load data infile 'solr.out' into table solr fields terminated by '|' lines terminated by '\n';
load data infile 'pt_ls.out' into table pt_ls fields terminated by ',' lines terminated by '\n';
load data infile 'solr_ls.out' into table solr_ls fields terminated by ',' lines terminated by '\n';
load data infile 'pt_pt.out' into table pt_pt fields terminated by ',' lines terminated by '\n';
