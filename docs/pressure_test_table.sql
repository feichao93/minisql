create table orders(
orderkey int,
custkey int unique,
orderstatus	char(1),
totalprice	float,
clerk char(15),
comments char(79) unique,
primary key(orderkey) );