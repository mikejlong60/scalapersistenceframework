create schema spf;


CREATE OR REPLACE TABLE spf.order (
    id  serial primary key,
    customer_id integer not null,
    description varchar(2000),
    complete numeric(1),
    approved numeric(1) not null,
    order_qty integer,
    created_ts timestamp not null,
    updated_ts timestamp not null
);

drop table spf.address;

CREATE TABLE spf.address (
    id  integer not null primary key,
    name varchar(2000) not null,
    line1 varchar(2000) not null,
    line2 varchar(2000) not null,
    state varchar(2000) not null,
    zipcode varchar(2000) not null,
    created_ts timestamp not null,
    updated_ts timestamp not null
);

CREATE OR REPLACE FUNCTION samplerefcursorfunc() RETURNS refcursor AS 
        'DECLARE mycurs refcursor;
         BEGIN
	       OPEN mycurs FOR SELECT * from spf.order; 
	    RETURN mycurs; 
        END;' 
        language plpgsql
