load;
show;
select * from orders where orderkey=182596;
select * from orders where totalprice > 999500;
insert into orders values (541959,408677,'F',241827.84,'Clerk#000002574','test: check unique');
insert into orders values (541959,408677,'F',241827.84,'Clerk#000002574','test: check unique');
create index custkeyidx on orders (custkey);
delete from orders where custkey > 190000;
drop index custkeyidx;
create index commentsidx on orders (comments);
delete from orders where custkey=150000;
insert into orders values (541959,408677,'F',241827.84,'Clerk#000002574','test: check unique');
insert into orders values (541959,408677,'F',241827.84,'Clerk#000002574','test: check unique');
select * from orders where orderstatus='O' and comments='test: check unique';